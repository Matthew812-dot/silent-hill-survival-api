package app;

import com.sun.net.httpserver.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** HTTP routing and JSON validation; game rules remain in SurvivalService. */
public class SurvivorHandler implements HttpHandler {
    private final SurvivalService service;
    private final JsonMapper mapper = JsonMapper.builder().build();
    public SurvivorHandler(SurvivalService service) { this.service = service; }
    private static class ApiError extends RuntimeException {
        final int status;
        ApiError(int status, String message) { super(message); this.status = status; }
    }
    private void send(HttpExchange e, int status, Object value) throws IOException {
        e.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (status == 204) { e.sendResponseHeaders(status, -1); return; }
        byte[] bytes = mapper.writeValueAsBytes(value);
        e.sendResponseHeaders(status, bytes.length);
        e.getResponseBody().write(bytes);
    }
    private JsonNode body(HttpExchange e) throws IOException {
        try {
            byte[] input = e.getRequestBody().readNBytes(65537);
            if (input.length > 65536) throw new ApiError(413, "JSON body exceeds 64 KiB");
            JsonNode node = mapper.readTree(input);
            if (node == null || !node.isObject()) throw new IllegalArgumentException();
            return node;
        } catch (ApiError ex) { throw ex; }
        catch (RuntimeException ex) { throw new ApiError(400, "Expected a JSON object"); }
    }
    private String text(JsonNode n, String key) {
        JsonNode v = n.get(key);
        if (v == null || !v.isString() || v.asString().isBlank()) throw new ApiError(400, key + " must be a nonempty string");
        return v.asString().trim();
    }
    private int number(JsonNode n, String key) {
        JsonNode v = n.get(key);
        if (v == null || !v.isIntegralNumber() || !v.canConvertToInt()) throw new ApiError(400, key + " must be an integer");
        return v.asInt();
    }
    private int id(String value) {
        try { int id = Integer.parseInt(value); if (id > 0) return id; } catch (NumberFormatException ignored) { }
        throw new ApiError(400, "ID must be a positive integer");
    }
    private Survivor survivor(int id) {
        Survivor s = service.getSurvivor(id);
        if (s == null) throw new ApiError(404, "Survivor not found");
        return s;
    }
    private Item item(int id) {
        Item i = service.getItem(id);
        if (i == null) throw new ApiError(404, "Item not found");
        return i;
    }
    private Object view(int id, Survivor s) {
        return Map.of("id", id, "name", s.getName(), "health", s.getHealth(), "stamina", s.getStamina());
    }
    private Object view(int id, Item i) { return Map.of("id", id, "type", i.getName()); }
    private Item parseItem(JsonNode n) {
        return switch (text(n, "type")) {
            case "Medkit" -> new Medkit();
            case "EnergyDrink" -> new EnergyDrink();
            default -> throw new ApiError(400, "type must be Medkit or EnergyDrink");
        };
    }
    private void method(HttpExchange e, String allowed) {
        e.getResponseHeaders().set("Allow", allowed);
        throw new ApiError(405, "Method not allowed");
    }
    @Override public void handle(HttpExchange e) throws IOException {
        try { synchronized (service) { route(e); } }
        catch (ApiError ex) { send(e, ex.status, Map.of("error", ex.getMessage())); }
        catch (IllegalArgumentException ex) { send(e, 400, Map.of("error", ex.getMessage())); }
        catch (RuntimeException ex) { ex.printStackTrace(); send(e, 500, Map.of("error", "Internal server error")); }
        finally { e.close(); }
    }
    private void route(HttpExchange e) throws IOException {
        String[] p = e.getRequestURI().getPath().split("/", -1);
        String method = e.getRequestMethod();
        if (p.length < 2 || !(p[1].equals("survivors") || p[1].equals("items"))) throw new ApiError(404, "Route not found");
        boolean people = p[1].equals("survivors");
        Map<String,String> query = new HashMap<>();
        String raw = e.getRequestURI().getRawQuery();
        if (raw != null) for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            query.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), kv.length == 2 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        if (p.length == 2) {
            if (method.equals("GET")) {
                List<Object> result = new ArrayList<>();
                if (people) {
                    if (query.containsKey("alive") && !Set.of("true", "false").contains(query.get("alive"))) throw new ApiError(400, "alive must be true or false");
                    service.survivorEntries().forEach((id,s) -> {
                        if (query.containsKey("name") && !s.getName().toLowerCase(Locale.ROOT).contains(query.get("name").toLowerCase(Locale.ROOT))) return;
                        if (query.containsKey("alive") && (s.getHealth() > 0) != Boolean.parseBoolean(query.get("alive"))) return;
                        result.add(view(id,s));
                    });
                } else {
                    if (query.containsKey("type") && !Set.of("Medkit", "EnergyDrink").contains(query.get("type"))) throw new ApiError(400, "Unknown item type");
                    service.itemEntries().forEach((id,i) -> { if (!query.containsKey("type") || i.getName().equals(query.get("type"))) result.add(view(id,i)); });
                }
                send(e,200,result); return;
            }
            if (method.equals("POST")) {
                JsonNode n = body(e);
                int id = people ? service.createSurvivor(text(n,"name")) : service.createItem(parseItem(n));
                e.getResponseHeaders().set("Location", "/" + p[1] + "/" + id);
                send(e,201, people ? view(id,survivor(id)) : view(id,item(id))); return;
            }
            method(e,"GET, POST");
        }
        int id = id(p[2]);
        if (people) survivor(id); else item(id);
        if (p.length == 3) {
            switch (method) {
                case "GET" -> send(e,200,people ? view(id,survivor(id)) : view(id,item(id)));
                case "DELETE" -> { if (people) service.deleteSurvivor(id); else service.deleteItem(id); send(e,204,null); }
                case "PUT" -> {
                    JsonNode n = body(e);
                    if (people) {
                        String name = text(n,"name"); int health = number(n,"health"), stamina = number(n,"stamina");
                        if (health < 0 || health > 100 || stamina < 0 || stamina > 100) throw new ApiError(400,"health and stamina must be between 0 and 100");
                        service.updateSurvivor(id,name,health,stamina);
                    } else service.updateItem(id,parseItem(n));
                    send(e,200,people ? view(id,survivor(id)) : view(id,item(id)));
                }
                default -> method(e,"GET, PUT, DELETE");
            }
            return;
        }
        if (people && p.length == 4 && p[3].equals("damage")) {
            if (!method.equals("POST")) method(e,"POST");
            int amount = number(body(e),"amount");
            if (amount <= 0) throw new ApiError(400,"amount must be positive");
            service.damageSurvivor(id,amount); send(e,200,view(id,survivor(id))); return;
        }
        if (people && p.length == 4 && p[3].equals("inventory")) {
            if (method.equals("GET")) {
                List<Object> result = new ArrayList<>();
                for (int itemId : service.inventory(id)) result.add(view(itemId,item(itemId)));
                send(e,200,result); return;
            }
            if (method.equals("POST")) {
                int itemId = number(body(e),"itemId"); item(itemId);
                if (!service.addItemToInventory(id,itemId)) throw new ApiError(409,"Item already belongs to an inventory");
                send(e,200,Map.of("itemId",itemId)); return;
            }
            method(e,"GET, POST");
        }
        if (people && p.length == 6 && p[3].equals("inventory") && p[5].equals("use")) {
            if (!method.equals("POST")) method(e,"POST");
            int itemId = id(p[4]); item(itemId);
            if (!service.useInventoryItem(id,itemId)) throw new ApiError(409,"Item is not in this survivor's inventory");
            send(e,200,view(id,survivor(id))); return;
        }
        throw new ApiError(404,"Route not found");
    }
}


