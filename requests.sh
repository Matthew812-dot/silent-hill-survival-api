#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE_URL:-http://localhost:8080}
# Every call checks its HTTP status. All requests have a finite timeout.
request() {
  local expected=$1 method=$2 path=$3 data=${4-} response status
  local args=(-sS --max-time 10 -X "$method" "$BASE$path" -H 'Content-Type: application/json')
  if [[ -n "$data" ]]; then args+=(-d "$data"); fi
  response=$(curl "${args[@]}" -w $'\n%{http_code}')
  status=${response##*$'\n'}
  BODY=${response%$'\n'*}
  printf '%s %s -> %s %s\n' "$method" "$path" "$status" "$BODY"
  [[ "$status" == "$expected" ]] || { echo "Expected $expected" >&2; exit 1; }
}
extract_id() { printf '%s' "$BODY" | sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p'; }
request 200 GET /ping
request 201 POST /survivors '{"name":"James"}'; survivor=$(extract_id)
request 200 GET /survivors
request 200 GET "/survivors/$survivor"
request 200 PUT "/survivors/$survivor" '{"name":"James","health":30,"stamina":20}'
request 201 POST /items '{"type":"Medkit"}'; medkit=$(extract_id)
request 201 POST /items '{"type":"Medkit"}'; drink=$(extract_id)
request 200 GET /items
request 200 GET "/items/$medkit"
request 200 PUT "/items/$drink" '{"type":"EnergyDrink"}'
request 200 GET '/items?type=EnergyDrink'
request 200 POST "/survivors/$survivor/damage" '{"amount":10}'
request 200 POST "/survivors/$survivor/inventory" "{\"itemId\":$medkit}"
request 409 POST "/survivors/$survivor/inventory" "{\"itemId\":$medkit}"
request 200 GET "/survivors/$survivor/inventory"
request 409 POST "/survivors/$survivor/inventory/$drink/use"
request 200 POST "/survivors/$survivor/inventory/$medkit/use"
request 404 GET "/items/$medkit"
request 200 POST "/survivors/$survivor/inventory" "{\"itemId\":$drink}"
request 200 POST "/survivors/$survivor/inventory/$drink/use"
request 200 GET '/survivors?alive=true&name=jam'
request 400 POST /survivors '{'
request 400 POST /survivors '{"name":""}'
request 400 POST /items '{"type":"Unknown"}'
request 400 PUT "/survivors/$survivor" '{"name":"James","health":101,"stamina":20}'
request 400 POST "/survivors/$survivor/damage" '{"amount":-1}'
request 400 GET '/survivors?alive=maybe'
request 400 GET /survivors/no
request 404 GET /survivors/2147483647
request 405 PATCH "/survivors/$survivor"
request 404 GET "/survivors/$survivor/unknown"
request 404 GET /unknown
request 201 POST /items '{"type":"Medkit"}'; item=$(extract_id)
request 200 POST "/survivors/$survivor/inventory" "{\"itemId\":$item}"
request 204 DELETE "/items/$item"
request 200 GET "/survivors/$survivor/inventory"
request 201 POST /items '{"type":"EnergyDrink"}'; item=$(extract_id)
request 200 POST "/survivors/$survivor/inventory" "{\"itemId\":$item}"
request 204 DELETE "/survivors/$survivor"
request 404 GET "/survivors/$survivor"
request 404 GET "/items/$item"
echo 'All curl checks passed'
