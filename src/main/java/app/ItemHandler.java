
package app;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class ItemHandler implements HttpHandler {

    private final SurvivalService service;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public ItemHandler(SurvivalService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {

            // GET
            if (method.equals("GET")) {

                if (path.equals("/items")) {

                    String query = exchange.getRequestURI().getRawQuery();
                    Collection<Item> items;

                    if (query != null && query.contains("type=")) {
                        String type = query.substring(query.indexOf("type=") + 5);
                        items = service.getItemsByType(type);
                    } else {
                        items = service.getAllItems();
                    }

                    sendJson(exchange, 200, items);
                    return;
                }

                int id = getId(path);
                Item item = service.getItem(id);

                if (item == null) {
                    sendError(exchange, 404, "Item not found");
                    return;
                }

                sendJson(exchange, 200, item);
                return;
            }

            // POST
            if (method.equals("POST") && path.equals("/items")) {

                JsonNode json = mapper.readTree(
                        exchange.getRequestBody().readAllBytes()
                );

                if (json == null || json.get("type") == null ||
                        !json.get("type").isString()) {
                    sendError(exchange, 400, "Invalid item type");
                    return;
                }

                String type = json.get("type").asString();
                Item item;

                if (type.equals("medkit")) {
                    item = new Medkit();
                } else if (type.equals("energyDrink")) {
                    item = new EnergyDrink();
                } else {
                    sendError(exchange, 400, "Unknown item type");
                    return;
                }

                int id = service.createItem(item);
                sendJson(exchange, 201, Map.of("id", id));
                return;
            }

            // PUT
            if (method.equals("PUT")) {

                int id = getId(path);

                JsonNode json = mapper.readTree(
                        exchange.getRequestBody().readAllBytes()
                );

                if (json == null || json.get("type") == null ||
                        !json.get("type").isString()) {
                    sendError(exchange, 400, "Invalid item type");
                    return;
                }

                String type = json.get("type").asString();
                Item item;

                if (type.equals("medkit")) {
                    item = new Medkit();
                } else if (type.equals("energyDrink")) {
                    item = new EnergyDrink();
                } else {
                    sendError(exchange, 400, "Unknown item type");
                    return;
                }

                if (!service.updateItem(id, item)) {
                    sendError(exchange, 404, "Item not found");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            // DELETE
            if (method.equals("DELETE")) {

                int id = getId(path);

                if (!service.deleteItem(id)) {
                    sendError(exchange, 404, "Item not found");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            exchange.getResponseHeaders().set(
                    "Allow", "GET, POST, PUT, DELETE"
            );
            sendError(exchange, 405, "Method not allowed");

        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 400, "Invalid request");
        }
    }

    private int getId(String path) {
        String[] parts = path.split("/");
        if (parts.length != 3 || !parts[1].equals("items")) {
            throw new IllegalArgumentException("Invalid path");
        }
        return Integer.parseInt(parts[2]);
    }

    private void sendJson(HttpExchange exchange, int code, Object data)
            throws IOException {

        byte[] body = mapper.writeValueAsBytes(data);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=utf-8"
        );

        exchange.sendResponseHeaders(code, body.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private void sendError(HttpExchange exchange, int code, String message)
            throws IOException {
        sendJson(exchange, code, Map.of("error", message));
    }
}
