
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
curl -i http://localhost:8080/items/1
curl -i "http://localhost:8080/items?type=medkit"

curl -i -X PUT http://localhost:8080/items/2 \
  -H "Content-Type: application/json" \
  -d '{"type":"energyDrink"}'

curl -i http://localhost:8080/items/2

curl -i -X POST http://localhost:8080/survivors/1/inventory \
  -H "Content-Type: application/json" \
  -d '{"itemId":1}'

curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/survivors/1/inventory/1/use

curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/survivors/1/inventory \
  -H "Content-Type: application/json" \
  -d '{"itemId":2}'

curl -i -X POST http://localhost:8080/survivors/1/inventory/2/use

curl -i http://localhost:8080/survivors/1

curl -i -X PUT http://localhost:8080/survivors/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"James Updated"}'

curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"medkit"}'

curl -i -X PUT http://localhost:8080/items/3 \
  -H "Content-Type: application/json" \
  -d '{"type":"energyDrink"}'

curl -i http://localhost:8080/items/3

curl -i -X DELETE http://localhost:8080/items/3

curl -i -X DELETE http://localhost:8080/survivors/1

curl -i http://localhost:8080/survivors/999
curl -i http://localhost:8080/items/999

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"unknown"}'

curl -i -X POST http://localhost:8080/survivors/999/inventory \
  -H "Content-Type: application/json" \
  -d '{"itemId":999}'

curl -i -X POST http://localhost:8080/survivors/999/inventory/999/use

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":""}'

curl -i -X PUT http://localhost:8080/survivors/999 \
  -H "Content-Type: application/json" \
  -d '{"name":"Nobody"}'

curl -i -X DELETE http://localhost:8080/items/999
