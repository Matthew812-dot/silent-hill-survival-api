
package app;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

        boolean collection = path.equals("/items");
        boolean itemPath = path.matches("/items/[^/]+");

        try {
            List<String> allowedMethods;
            if (collection) {
                allowedMethods = List.of("GET", "POST");
            } else if (itemPath) {
                allowedMethods = List.of("GET", "PUT", "DELETE");
            } else {
                allowedMethods = List.of();
            }

            if (allowedMethods.isEmpty()) {
                sendError(exchange, 404, "Route not found");
                return;
            }
            if (!allowedMethods.contains(method)) {
                exchange.getResponseHeaders().set("Allow", String.join(", ", allowedMethods));
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String[] parts = path.split("/");

            if (method.equals("GET")) {

                if (collection) {

                    String type;
                    try {
                        type = readTypeFilter(exchange.getRequestURI().getRawQuery());
                    } catch (IllegalArgumentException e) {
                        sendError(exchange, 400, "Invalid type filter");
                        return;
                    }

                    Map<Integer, Item> items = type == null
                            ? service.getAllItemsWithIds()
                            : service.getItemsByTypeWithIds(type);
                    List<ItemResponse> response = items.entrySet().stream()
                            .map(entry -> ItemResponse.from(entry.getKey(), entry.getValue()))
                            .toList();

                    sendJson(exchange, 200, response);
                    return;
                }

                int id = parseId(parts[2]);
                Item item = service.getItem(id);

                if (item == null) {
                    sendError(exchange, 404, "Item not found");
                    return;
                }

                sendJson(exchange, 200, ItemResponse.from(id, item));
                return;
            }

            if (method.equals("POST")) {

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

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

            if (method.equals("PUT")) {

                int id = parseId(parts[2]);

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

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

            if (method.equals("DELETE")) {

                int id = parseId(parts[2]);

                if (!service.deleteItem(id)) {
                    sendError(exchange, 404, "Item not found");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

        } catch (RequestBody.TooLargeException e) {
            exchange.getResponseHeaders().set("Connection", "close");
            sendError(exchange, 413, e.getMessage());
        } catch (BadRequestException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (IOException e) {
            exchange.close();
            throw e;
        } catch (RuntimeException e) {
            e.printStackTrace();
            if (exchange.getResponseCode() == -1) {
                sendError(exchange, 500, "Internal server error");
            } else {
                exchange.close();
            }
        }
    }

    private String readTypeFilter(String rawQuery) {
        if (rawQuery == null) {
            return null;
        }

        String type = null;
        for (String parameter : rawQuery.split("&")) {
            String[] parts = parameter.split("=", 2);
            String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (!name.equals("type")) {
                continue;
            }
            if (type != null || parts.length != 2) {
                throw new IllegalArgumentException("Expected a single type value");
            }
            type = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            if (type.isBlank()) {
                throw new IllegalArgumentException("Empty type value");
            }
        }
        return type;
    }

    private int parseId(String value) {
        if (!value.matches("[0-9]+")) {
            throw new BadRequestException("Invalid ID");
        }

        int id;
        try {
            id = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid ID", e);
        }
        if (id == 0) {
            throw new BadRequestException("Invalid ID");
        }
        return id;
    }

    private record ItemResponse(int id, String name) {
        static ItemResponse from(int id, Item item) {
            return new ItemResponse(id, item.getName());
        }
    }

    private void sendJson(HttpExchange exchange, int code, Object data)
            throws IOException {

        byte[] body = mapper.writeValueAsBytes(data);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=utf-8"
        );

        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(code, -1);
            exchange.close();
            return;
        }

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
