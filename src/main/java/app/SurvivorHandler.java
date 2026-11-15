
package app;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;

public class SurvivorHandler implements HttpHandler {

    private final SurvivalService service;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public SurvivorHandler(SurvivalService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {

            if (method.equals("GET")) {

                if (path.equals("/survivors")) {

                    String query = exchange.getRequestURI().getRawQuery();
                    Collection<Survivor> survivors;

                    if (query != null && query.contains("alive=true")) {
                        survivors = service.getAliveSurvivors();

                    } else if (query != null && query.contains("name=")) {

                        String name = URLDecoder.decode(
                                query.substring(query.indexOf("name=") + 5),
                                StandardCharsets.UTF_8
                        );

                        survivors = service.findSurvivorsByName(name);

                    } else {
                        survivors = service.getAllSurvivors();
                    }

                    sendJson(exchange, 200, survivors);
                    return;
                }

                int id = getId(path);
                Survivor survivor = service.getSurvivor(id);

                if (survivor == null) {
                    sendError(exchange, 404, "Survivor not found");
                    return;
                }

                sendJson(exchange, 200, survivor);
                return;
            }

            if (method.equals("POST") && path.equals("/survivors")) {

                JsonNode json = mapper.readTree(
                        exchange.getRequestBody().readAllBytes()
                );

                if (json == null || json.get("name") == null ||
                        !json.get("name").isString() ||
                        json.get("name").asString().isBlank()) {

                    sendError(exchange, 400, "Invalid name");
                    return;
                }

                int id = service.createSurvivor(
                        json.get("name").asString()
                );

                sendJson(exchange, 201, Map.of("id", id));
                return;
            }

            if (method.equals("POST") &&
                    path.matches("/survivors/\\d+/inventory")) {

                String[] parts = path.split("/");
                int survivorId = Integer.parseInt(parts[2]);

                JsonNode json = mapper.readTree(
                        exchange.getRequestBody().readAllBytes()
                );

                if (json == null || json.get("itemId") == null ||
                        !json.get("itemId").isIntegralNumber()) {

                    sendError(exchange, 400, "Invalid itemId");
                    return;
                }

                int itemId = json.get("itemId").asInt();

                if (!service.addItemToInventory(survivorId, itemId)) {
                    sendError(exchange, 409, "Cannot add item");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (method.equals("POST") &&
                    path.matches("/survivors/\\d+/inventory/\\d+/use")) {

                String[] parts = path.split("/");

                int survivorId = Integer.parseInt(parts[2]);
                int itemId = Integer.parseInt(parts[4]);

                if (!service.useInventoryItem(survivorId, itemId)) {
                    sendError(exchange, 409, "Cannot use item");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (method.equals("PUT") && path.matches("/survivors/\\d+")) {

                int id = getId(path);

                JsonNode json = mapper.readTree(
                        exchange.getRequestBody().readAllBytes()
                );

                if (json == null || json.get("name") == null ||
                        !json.get("name").isString() ||
                        json.get("name").asString().isBlank()) {

                    sendError(exchange, 400, "Invalid name");
                    return;
                }

                if (!service.updateSurvivor(
                        id, json.get("name").asString())) {

                    sendError(exchange, 404, "Survivor not found");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (method.equals("DELETE") && path.matches("/survivors/\\d+")) {

                int id = getId(path);

                if (!service.deleteSurvivor(id)) {
                    sendError(exchange, 404, "Survivor not found");
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

        if (parts.length != 3 || !parts[1].equals("survivors")) {
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
