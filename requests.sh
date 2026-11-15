
#!/usr/bin/env bash

curl -i http://localhost:8080/ping

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":"James"}'

curl -i http://localhost:8080/survivors

curl -i http://localhost:8080/survivors/1

curl -i "http://localhost:8080/survivors?name=James"

curl -i "http://localhost:8080/survivors?alive=true"

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"medkit"}'

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"energyDrink"}'

curl -i http://localhost:8080/items

curl -i -X POST http://localhost:8080/survivors/1/inventory \
  -H "Content-Type: application/json" \
  -d '{"itemId":1}'

curl -i -X POST http://localhost:8080/survivors/1/inventory/1/use
