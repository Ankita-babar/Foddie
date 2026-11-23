const resources = {
    customers: {
        title: 'Customers', path: '/api/customers', id: 'customerId',
        fields: [['name', 'text'], ['email', 'email'], ['phone', 'text'], ['address', 'text']]
    },
    restaurants: {
        title: 'Restaurants', path: '/api/restaurants', id: 'restaurantId',
        fields: [['name', 'text'], ['address', 'text'], ['phone', 'text'], ['rating', 'number']]
    },
    foods: {
        title: 'Food Items', path: '/api/foods', id: 'foodId',
        fields: [['restaurantId', 'number'], ['foodName', 'text'], ['category', 'text'], ['price', 'number'], ['availability', 'checkbox']]
    }
};

const reports = [
    ['available-foods', 'Q1 Available food items'],
    ['orders-with-customers', 'Q2 Orders with customer details'],
    ['orders-per-customer', 'Q3 Orders per customer'],
    ['top-selling-foods', 'Q4 Top selling foods'],
    ['restaurant-revenue', 'Q5 Revenue by restaurant'],
    ['above-average-customers', 'Q6 Customers above average order value'],
    ['monthly-stats', 'Q7 Monthly order statistics'],
    ['restaurants-with-many-dishes', 'Q8 Restaurants with more than 3 dishes']
];

const statuses = ['PLACED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];
const paymentStatuses = ['PENDING', 'PAID', 'FAILED', 'REFUNDED'];

const tabs = document.getElementById('tabs');
const view = document.getElementById('view');
const message = document.getElementById('message');

function show(text, type) {
    message.textContent = text;
    message.className = type;
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
        throw new Error(data.error || 'Request failed');
    }
    return data;
}

async function run(action, success) {
    try {
        await action();
        if (success) {
            show(success, 'ok');
        }
    } catch (e) {
        show(e.message, 'error');
    }
}

function format(value) {
    if (value === null || value === undefined) {
        return '';
    }
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(value)) {
        return value.replace('T', ' ').substring(0, 16);
    }
    return String(value);
}

function table(rows, extraHeader = '', extraCell = null) {
    if (rows.length === 0) {
        return '<p>No records found.</p>';
    }
    const keys = Object.keys(rows[0]);
    let html = '<div class="table-wrap"><table><thead><tr>';
    keys.forEach(k => html += '<th>' + k.replace(/([A-Z])/g, ' $1') + '</th>');
    html += extraHeader ? '<th>' + extraHeader + '</th>' : '';
    html += '</tr></thead><tbody>';
    rows.forEach((row, index) => {
        html += '<tr>';
        keys.forEach(k => {
            const cell = document.createElement('span');
            cell.textContent = format(row[k]);
            html += '<td>' + cell.innerHTML + '</td>';
        });
        if (extraCell) {
            html += '<td>' + extraCell(row, index) + '</td>';
        }
        html += '</tr>';
    });
    return html + '</tbody></table></div>';
}

function selectOptions(values, selected) {
    return values.map(v => '<option' + (v === selected ? ' selected' : '') + '>' + v + '</option>').join('');
}

async function showResource(name) {
    const res = resources[name];
    const rows = await api(res.path);
    let editingId = null;

    let form = '<form id="form">';
    res.fields.forEach(([field, type]) => {
        form += '<label>' + field.replace(/([A-Z])/g, ' $1') + '<input name="' + field + '" type="' + type + '"' +
            (type === 'number' ? ' step="any"' : '') + (type === 'checkbox' ? ' checked' : '') + '></label>';
    });
    form += '<button class="btn" type="submit">Save</button><button class="btn secondary" type="reset" id="reset">Clear</button></form>';

    view.innerHTML = '<h2>' + res.title + '</h2>' + form +
        table(rows, 'Actions', row => '<button class="btn small" data-edit="' + row[res.id] + '">Edit</button> ' +
            '<button class="btn small danger" data-delete="' + row[res.id] + '">Delete</button>' +
            (name === 'foods' ? ' <button class="btn small secondary" data-image="' + row[res.id] + '">Image</button>' : ''));

    const formEl = document.getElementById('form');
    formEl.addEventListener('reset', () => { editingId = null; });
    formEl.addEventListener('submit', event => {
        event.preventDefault();
        const body = {};
        res.fields.forEach(([field, type]) => {
            const input = formEl.elements[field];
            if (type === 'checkbox') {
                body[field] = input.checked;
            } else if (type === 'number') {
                body[field] = input.value === '' ? null : Number(input.value);
            } else {
                body[field] = input.value;
            }
        });
        const path = editingId === null ? res.path : res.path + '/' + editingId;
        const method = editingId === null ? 'POST' : 'PUT';
        run(async () => {
            await api(path, method, body);
            await showResource(name);
        }, editingId === null ? 'Record created' : 'Record updated');
    });

    view.querySelectorAll('[data-edit]').forEach(button => button.addEventListener('click', () => {
        const row = rows.find(r => String(r[res.id]) === button.dataset.edit);
        editingId = row[res.id];
        res.fields.forEach(([field, type]) => {
            const input = formEl.elements[field];
            if (type === 'checkbox') {
                input.checked = row[field];
            } else {
                input.value = row[field];
            }
        });
        window.scrollTo(0, 0);
    }));

    view.querySelectorAll('[data-image]').forEach(button => button.addEventListener('click', () => {
        const picker = document.createElement('input');
        picker.type = 'file';
        picker.accept = 'image/*';
        picker.addEventListener('change', () => {
            if (picker.files.length === 0) {
                return;
            }
            const data = new FormData();
            data.append('file', picker.files[0]);
            run(async () => {
                const response = await fetch('/api/foods/' + button.dataset.image + '/image', { method: 'POST', body: data });
                const result = await response.json();
                if (!response.ok) {
                    throw new Error(result.error || 'Upload failed');
                }
            }, 'Image stored in MongoDB');
        });
        picker.click();
    }));

    view.querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click', () => {
        if (!confirm('Delete this record?')) {
            return;
        }
        run(async () => {
            await api(res.path + '/' + button.dataset.delete, 'DELETE');
            await showResource(name);
        }, 'Record deleted');
    }));
}

async function showOrders() {
    const rows = await api('/api/orders');
    view.innerHTML = '<h2>Orders</h2>' +
        '<form id="orderForm">' +
        '<label>Customer ID<input name="customerId" type="number" required></label>' +
        '<label>Payment method<select name="paymentMethod"><option>CARD</option><option>UPI</option><option>COD</option></select></label>' +
        '<label>Items (foodId:quantity, ...)<input name="items" type="text" placeholder="1:2, 9:1" size="28" required></label>' +
        '<button class="btn" type="submit">Place Order</button></form>' +
        '<pre id="details" hidden></pre>' +
        table(rows, 'Actions', row =>
            '<select data-status="' + row.orderId + '">' + selectOptions(statuses, row.status) + '</select> ' +
            '<button class="btn small" data-update="' + row.orderId + '">Update</button> ' +
            '<button class="btn small secondary" data-view="' + row.orderId + '">Details</button> ' +
            '<button class="btn small danger" data-delete="' + row.orderId + '">Delete</button>');

    document.getElementById('orderForm').addEventListener('submit', event => {
        event.preventDefault();
        const f = event.target.elements;
        const items = f.items.value.split(',').map(part => {
            const [foodId, quantity] = part.trim().split(':');
            return { foodId: Number(foodId), quantity: Number(quantity) };
        });
        run(async () => {
            await api('/api/orders', 'POST', {
                customerId: Number(f.customerId.value), paymentMethod: f.paymentMethod.value, items
            });
            await showOrders();
        }, 'Order placed');
    });

    view.querySelectorAll('[data-update]').forEach(button => button.addEventListener('click', () => {
        const status = view.querySelector('[data-status="' + button.dataset.update + '"]').value;
        run(async () => {
            await api('/api/orders/' + button.dataset.update + '/status', 'PUT', { status });
            await showOrders();
        }, 'Status updated');
    }));

    view.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => {
        run(async () => {
            const data = await api('/api/orders/' + button.dataset.view);
            const box = document.getElementById('details');
            box.hidden = false;
            box.textContent = JSON.stringify(data, null, 2);
            window.scrollTo(0, 0);
        });
    }));

    view.querySelectorAll('[data-delete]').forEach(button => button.addEventListener('click', () => {
        if (!confirm('Delete this order with its items and payment?')) {
            return;
        }
        run(async () => {
            await api('/api/orders/' + button.dataset.delete, 'DELETE');
            await showOrders();
        }, 'Order deleted');
    }));
}

async function showPayments() {
    const rows = await api('/api/payments');
    view.innerHTML = '<h2>Payments</h2>' + table(rows, 'Actions', row =>
        '<select data-pstatus="' + row.paymentId + '">' + selectOptions(paymentStatuses, row.paymentStatus) + '</select> ' +
        '<button class="btn small" data-pupdate="' + row.paymentId + '">Update</button>');

    view.querySelectorAll('[data-pupdate]').forEach(button => button.addEventListener('click', () => {
        const status = view.querySelector('[data-pstatus="' + button.dataset.pupdate + '"]').value;
        run(async () => {
            await api('/api/payments/' + button.dataset.pupdate, 'PUT', { status });
            await showPayments();
        }, 'Payment updated');
    }));
}

async function showReports() {
    view.innerHTML = '<h2>SQL Reports</h2><form><label>Report<select id="report">' +
        reports.map(([key, label]) => '<option value="' + key + '">' + label + '</option>').join('') +
        '</select></label><button class="btn" type="submit">Run</button></form><div id="result"></div>';

    view.querySelector('form').addEventListener('submit', event => {
        event.preventDefault();
        run(async () => {
            const rows = await api('/api/reports/' + document.getElementById('report').value);
            document.getElementById('result').innerHTML = table(rows);
        });
    });
}

const pages = {
    customers: () => showResource('customers'),
    restaurants: () => showResource('restaurants'),
    foods: () => showResource('foods'),
    orders: showOrders,
    payments: showPayments,
    reports: showReports
};

const labels = {
    customers: 'Customers', restaurants: 'Restaurants', foods: 'Food Items',
    orders: 'Orders', payments: 'Payments', reports: 'SQL Reports'
};

function open(name) {
    show('', '');
    tabs.querySelectorAll('button').forEach(b => b.classList.toggle('active', b.dataset.page === name));
    run(pages[name]);
}

Object.keys(pages).forEach(name => {
    const button = document.createElement('button');
    button.textContent = labels[name];
    button.dataset.page = name;
    button.addEventListener('click', () => open(name));
    tabs.appendChild(button);
});

open('customers');
