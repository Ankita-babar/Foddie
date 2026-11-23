#!/bin/bash
folder="${1:-images}"
for file in "$folder"/*.jpg "$folder"/*.jpeg "$folder"/*.png "$folder"/*.webp; do
  [ -e "$file" ] || continue
  id=$(basename "$file" | cut -d. -f1)
  curl -s -o /dev/null -w "food $id: HTTP %{http_code}\n" -F "file=@$file" "http://localhost:8080/api/foods/$id/image"
done
