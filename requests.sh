
#!/usr/bin/env bash

curl -i http://localhost:8080/items/1/something
curl -i -X POST http://localhost:8080/survivors/1/unknown

curl -i -X PUT http://localhost:8080/items
curl -i -X POST http://localhost:8080/items/1
curl -i http://localhost:8080/survivors/1/damage

curl -i http://localhost:8080/items/abc
curl -i -X POST http://localhost:8080/survivors/1/damage \
  -H "Content-Type: application/json" \
  -d '{"amount":0}'

printf '{"type":"medkit"}%*s' 16368 '' | curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  --data-binary @-

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":'

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":'

curl -i http://localhost:8080/ping

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":"James"}'

curl -i http://localhost:8080/survivors
curl -i http://localhost:8080/survivors/1
curl -i "http://localhost:8080/survivors?name=James"
curl -i "http://localhost:8080/survivors?alive=true"
curl -i "http://localhost:8080/survivors?alive=false"

curl -i "http://localhost:8080/survivors?alive=maybe"
curl -i "http://localhost:8080/survivors?alive=true&alive=false"

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"medkit"}'

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"energyDrink"}'

curl -i http://localhost:8080/items
curl -i http://localhost:8080/items/1
curl -i "http://localhost:8080/items?type=medkit"

curl -i "http://localhost:8080/items?type=medkit&source=test"
curl -i "http://localhost:8080/items?source=test&type=energy%44rink"
curl -i "http://localhost:8080/items?prototype=medkit"
curl -i "http://localhost:8080/items?type=unknown"
curl -i "http://localhost:8080/items?type="
curl -i "http://localhost:8080/items?type=medkit&type=energyDrink"

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

curl -i -X POST http://localhost:8080/survivors/1/sprint
curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/survivors/1/inventory/2/use

curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/survivors/1/sprint
curl -i -X POST http://localhost:8080/survivors/1/sprint
curl -i -X POST http://localhost:8080/survivors/1/sprint
curl -i http://localhost:8080/survivors/1

curl -i -X POST http://localhost:8080/survivors/999/sprint
curl -i http://localhost:8080/survivors/1/sprint

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

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":"Heather"}'

curl -i -X POST http://localhost:8080/survivors/2/damage \
  -H "Content-Type: application/json" \
  -d '{"amount":50}'

curl -i http://localhost:8080/survivors/2

curl -i -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"type":"medkit"}'

curl -i -X POST http://localhost:8080/survivors/2/inventory \
  -H "Content-Type: application/json" \
  -d '{"itemId":4}'

curl -i -X POST http://localhost:8080/survivors/2/inventory/4/use

curl -i http://localhost:8080/survivors/2

curl -i -X POST http://localhost:8080/survivors/2/inventory/4/use

curl -i -X POST http://localhost:8080/survivors/2/damage \
  -H "Content-Type: application/json" \
  -d '{"amount":100}'

curl -i -X POST http://localhost:8080/survivors \
  -H "Content-Type: application/json" \
  -d '{"name":"Harry"}'

curl -i http://localhost:8080/survivors
curl -i "http://localhost:8080/survivors?alive=true"
curl -i "http://localhost:8080/survivors?alive=false"
curl -i "http://localhost:8080/survivors?alive=false&name=heath"
curl -i "http://localhost:8080/survivors?name=heath&alive=true"
