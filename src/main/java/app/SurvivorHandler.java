
package app;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
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

        boolean collection = path.equals("/survivors");
        boolean survivorPath = path.matches("/survivors/[^/]+");
        boolean sprint = path.matches("/survivors/[^/]+/sprint");
        boolean damage = path.matches("/survivors/[^/]+/damage");
        boolean inventory = path.matches("/survivors/[^/]+/inventory");
        boolean useItem = path.matches("/survivors/[^/]+/inventory/[^/]+/use");

        try {
            List<String> allowedMethods;
            if (collection) {
                allowedMethods = List.of("GET", "POST");
            } else if (survivorPath) {
                allowedMethods = List.of("GET", "PUT", "DELETE");
            } else if (sprint || damage || inventory || useItem) {
                allowedMethods = List.of("POST");
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

            if (sprint) {
                int id = parseId(parts[2]);
                switch (service.sprintSurvivor(id)) {
                    case SUCCESS -> {
                        exchange.sendResponseHeaders(204, -1);
                        exchange.close();
                    }
                    case SURVIVOR_NOT_FOUND -> sendError(exchange, 404, "Survivor not found");
                    case CANNOT_SPRINT -> sendError(exchange, 409, "Survivor cannot sprint");
                }
                return;
            }

            if (damage) {
                int id = parseId(parts[2]);

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

                if (json == null || json.get("amount") == null ||
                        !json.get("amount").isIntegralNumber() ||
                        !json.get("amount").canConvertToInt() ||
                        json.get("amount").asInt() <= 0) {
                    sendError(exchange, 400, "Invalid damage amount");
                    return;
                }

                int amount = json.get("amount").asInt();

                if (!service.damageSurvivor(id, amount)) {
                    sendError(exchange, 404, "Survivor not found");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (method.equals("GET")) {

                if (collection) {

                    SurvivorFilters filters = readFilters(exchange.getRequestURI().getRawQuery());
                    Map<Integer, Survivor> survivors = service.filterSurvivorsWithIds(filters.name(), filters.alive());

                    List<SurvivorResponse> response = survivors.entrySet().stream()
                            .map(entry -> SurvivorResponse.from(entry.getKey(), entry.getValue()))
                            .toList();

                    sendJson(exchange, 200, response);
                    return;
                }

                int id = parseId(parts[2]);
                Survivor survivor = service.getSurvivor(id);

                if (survivor == null) {
                    sendError(exchange, 404, "Survivor not found");
                    return;
                }

                sendJson(exchange, 200, SurvivorResponse.from(id, survivor));
                return;
            }

            if (method.equals("POST") && collection) {

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

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

            if (inventory) {

                int survivorId = parseId(parts[2]);

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

                if (json == null || json.get("itemId") == null ||
                        !json.get("itemId").isIntegralNumber() ||
                        !json.get("itemId").canConvertToInt()) {

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

            if (useItem) {

                int survivorId = parseId(parts[2]);
                int itemId = parseId(parts[4]);

                if (!service.useInventoryItem(survivorId, itemId)) {
                    sendError(exchange, 409, "Cannot use item");
                    return;
                }

                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (method.equals("PUT")) {

                int id = parseId(parts[2]);

                JsonNode json = RequestBody.readJson(exchange.getRequestBody(), mapper);

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

            if (method.equals("DELETE")) {

                int id = parseId(parts[2]);

                if (!service.deleteSurvivor(id)) {
                    sendError(exchange, 404, "Survivor not found");
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

    private SurvivorFilters readFilters(String rawQuery) {
        String name = null;
        Boolean alive = null;
        if (rawQuery == null) {
            return new SurvivorFilters(name, alive);
        }

        for (String parameter : rawQuery.split("&")) {
            String[] parts = parameter.split("=", 2);
            String key = decodeFilter(parts[0]);
            if (!key.equals("name") && !key.equals("alive")) {
                continue;
            }
            if (parts.length != 2) {
                throw new BadRequestException("Expected a value for " + key);
            }

            String value = decodeFilter(parts[1]);
            if (key.equals("name")) {
                if (name != null) {
                    throw new BadRequestException("Expected a single name filter");
                }
                name = value;
            } else {
                if (alive != null) {
                    throw new BadRequestException("Expected a single alive filter");
                }
                alive = switch (value) {
                    case "true" -> true;
                    case "false" -> false;
                    default -> throw new BadRequestException("alive must be true or false");
                };
            }
        }

        return new SurvivorFilters(name, alive);
    }

    private String decodeFilter(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid survivor filter", e);
        }
    }

    private record SurvivorFilters(String name, Boolean alive) {}

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

    private record SurvivorResponse(int id, String name, int health, int stamina) {
        static SurvivorResponse from(int id, Survivor survivor) {
            return new SurvivorResponse(id, survivor.getName(), survivor.getHealth(), survivor.getStamina());
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
