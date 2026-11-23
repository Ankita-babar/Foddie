const state = { customers: [], restaurants: [], foods: [], details: {}, cart: {}, restaurantId: null };

const $ = id => document.getElementById(id);
const money = n => '\u20B9' + Number(n).toFixed(2);

function esc(text) {
    const span = document.createElement('span');
    span.textContent = text;
    return span.innerHTML;
}

function notify(text, type) {
    $('notice').innerHTML = text ? '<p class="' + (type || '') + '">' + esc(text) + '</p>' : '';
}

async function api(path, method = 'GET', body = null) {
    const options = { method, headers: { 'Content-Type': 'application/json' } };
    if (body !== null) {
        options.body = JSON.stringify(body);
    }
    const response = await fetch(path, options);
    if (response.status === 204) {
        return null;
    }
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.error || 'Something went wrong');
    }
    return data;
}

function customerId() {
    return Number($('customer').value);
}

function renderRestaurants() {
    let html = '<h3>Restaurants</h3><button data-restaurant="" class="' + (state.restaurantId === null ? 'active' : '') + '">All restaurants</button>';
    state.restaurants.forEach(r => {
        const active = state.restaurantId === r.restaurantId ? 'active' : '';
        html += '<button data-restaurant="' + r.restaurantId + '" class="' + active + '">' + esc(r.name) +
            '<small>' + r.rating + '</small></button>';
    });
    $('restaurants').innerHTML = html;
}

function control(food) {
    const qty = state.cart[food.foodId] || 0;
    if (!food.availability) {
        return '<button class="add" disabled>Sold out</button>';
    }
    if (qty === 0) {
        return '<button class="add" data-add="' + food.foodId + '">Add</button>';
    }
    return '<div class="stepper"><button data-less="' + food.foodId + '" aria-label="Remove one">\u2212</button><span>' + qty +
        '</span><button data-add="' + food.foodId + '" aria-label="Add one">+</button></div>';
}

function renderMenu() {
    const shown = state.restaurants.filter(r => state.restaurantId === null || r.restaurantId === state.restaurantId);
    let html = '';
    shown.forEach(r => {
        const dishes = state.foods.filter(f => f.restaurantId === r.restaurantId);
        html += '<div class="shop"><div class="shop-head"><h2>' + esc(r.name) + '</h2><span>' + esc(r.address) + ', rated ' + r.rating + '</span></div>';
        dishes.forEach(f => {
            const info = state.details[f.foodId];
            const text = info && info.description ? info.description : f.category;
            html += '<div class="dish' + (f.availability ? '' : ' out') + '"><img class="thumb" src="/api/foods/' + f.foodId +
                '/image" alt="" loading="lazy" onerror="this.style.visibility=\'hidden\'"><div><b>' + esc(f.foodName) + '</b><small>' + esc(text) +
                '</small></div><span class="price">' + money(f.price) + '</span>' + control(f) + '</div>';
        });
        html += '</div>';
    });
    $('menu').innerHTML = html;
}

function cartLines() {
    return Object.keys(state.cart).map(id => {
        const food = state.foods.find(f => f.foodId === Number(id));
        return { food, qty: state.cart[id] };
    });
}

function renderCart() {
    const lines = cartLines();
    let total = 0;
    let html = '<h3>Your order</h3>';
    if (lines.length === 0) {
        html += '<p class="empty">Your order is empty. Add dishes from the menu.</p>';
    }
    lines.forEach(({ food, qty }) => {
        total += qty * food.price;
        html += '<div class="line"><div>' + esc(food.foodName) + '<small>' + qty + ' \u00D7 ' + money(food.price) + '</small></div><b>' + money(qty * food.price) + '</b></div>';
    });
    html += '<div class="total"><span>Total</span><span>' + money(total) + '</span></div>' +
        '<label for="method">Pay with</label><select id="method"><option value="UPI">UPI</option><option value="CARD">Card</option><option value="COD">Cash on delivery</option></select>' +
        '<button class="place" id="place"' + (lines.length === 0 ? ' disabled' : '') + '>Place order</button>';
    const method = $('method') ? $('method').value : 'UPI';
    $('cart').innerHTML = html;
    $('method').value = method;
}

function refresh() {
    renderMenu();
    renderCart();
}

async function placeOrder() {
    const items = cartLines().map(({ food, qty }) => ({ foodId: food.foodId, quantity: qty }));
    try {
        const result = await api('/api/orders', 'POST', {
            customerId: customerId(), paymentMethod: $('method').value, items
        });
        state.cart = {};
        refresh();
        notify('Order #' + result.order.orderId + ' placed for ' + money(result.order.totalAmount) +
            '. Payment is ' + result.payment.paymentStatus.toLowerCase() + '.');
        await showView('orders');
    } catch (e) {
        notify(e.message, 'error');
    }
}

async function loadOrders() {
    const orders = await api('/api/customers/' + customerId() + '/orders');
    if (orders.length === 0) {
        $('orderList').innerHTML = '<p class="empty">No orders yet. Pick a dish from the menu to place the first one.</p>';
        return;
    }
    $('orderList').innerHTML = orders.map(o =>
        '<details class="order" data-order="' + o.orderId + '"><summary><b>Order #' + o.orderId + '</b><span>' +
        esc(o.orderDate.replace('T', ' ').substring(0, 16)) + '</span><span class="badge ' + o.status + '">' +
        o.status.replace(/_/g, ' ') + '</span><b>' + money(o.totalAmount) + '</b></summary><div class="order-body">Loading...</div></details>'
    ).join('');
}

async function loadOrderBody(box) {
    const id = box.dataset.order;
    const data = await api('/api/orders/' + id);
    let html = data.items.map(i =>
        '<div class="line"><div>' + esc(i.foodName) + '<small>' + i.quantity + ' \u00D7 ' + money(i.unitPrice) + '</small></div><b>' +
        money(i.quantity * i.unitPrice) + '</b></div>').join('');
    if (data.payment) {
        html += '<p class="pay">Paid by ' + data.payment.paymentMethod + ' \u2022 payment ' + data.payment.paymentStatus.toLowerCase() + '</p>';
    }
    if (data.order.status === 'PLACED' || data.order.status === 'PREPARING') {
        html += '<button class="cancel" data-cancel="' + id + '">Cancel order</button>';
    }
    box.querySelector('.order-body').innerHTML = html;
}

async function showView(name) {
    document.querySelectorAll('.bar nav button').forEach(b => b.classList.toggle('active', b.dataset.view === name));
    $('menuView').hidden = name !== 'menu';
    $('ordersView').hidden = name !== 'orders';
    if (name === 'orders') {
        try {
            await loadOrders();
        } catch (e) {
            notify(e.message, 'error');
        }
    }
}

document.addEventListener('click', async event => {
    const t = event.target.closest('button');
    if (!t) {
        return;
    }
    if (t.dataset.add) {
        state.cart[t.dataset.add] = (state.cart[t.dataset.add] || 0) + 1;
        refresh();
    } else if (t.dataset.less) {
        state.cart[t.dataset.less] -= 1;
        if (state.cart[t.dataset.less] <= 0) {
            delete state.cart[t.dataset.less];
        }
        refresh();
    } else if (t.dataset.restaurant !== undefined) {
        state.restaurantId = t.dataset.restaurant === '' ? null : Number(t.dataset.restaurant);
        renderRestaurants();
        renderMenu();
    } else if (t.id === 'place') {
        placeOrder();
    } else if (t.dataset.view) {
        notify('');
        showView(t.dataset.view);
    } else if (t.id === 'refresh') {
        showView('orders');
    } else if (t.dataset.cancel) {
        try {
            await api('/api/orders/' + t.dataset.cancel + '/status', 'PUT', { status: 'CANCELLED' });
            notify('Order #' + t.dataset.cancel + ' cancelled. Any payment made will be refunded.');
            await loadOrders();
        } catch (e) {
            notify(e.message, 'error');
        }
    }
});

document.addEventListener('toggle', event => {
    const box = event.target;
    if (box.matches && box.matches('details.order') && box.open) {
        loadOrderBody(box).catch(e => notify(e.message, 'error'));
    }
}, true);

$('customer').addEventListener('change', () => {
    notify('');
    if (!$('ordersView').hidden) {
        showView('orders');
    }
});

async function start() {
    try {
        const [customers, restaurants, foods] = await Promise.all([api('/api/customers'), api('/api/restaurants'), api('/api/foods')]);
        Object.assign(state, { customers, restaurants, foods });
        const details = await api('/api/food-details').catch(() => []);
        details.forEach(d => { state.details[d._id] = d; });
        $('customer').innerHTML = customers.map(c => '<option value="' + c.customerId + '">' + esc(c.name) + '</option>').join('');
        renderRestaurants();
        refresh();
    } catch (e) {
        notify('Could not load the menu. Check that the backend and MySQL are running. ' + e.message, 'error');
    }
}

start();
