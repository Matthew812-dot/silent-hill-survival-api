package app;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Collection;
import tools.jackson.databind.json.JsonMapper;
import java.io.OutputStream;
import tools.jackson.databind.JsonNode;

public class SurvivorHandler implements HttpHandler {
    private final SurvivalService service;
    public SurvivorHandler(SurvivalService service) {
        this.service = service;
    }
    private final JsonMapper mapper = JsonMapper.builder().build();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("GET")) {
            Collection<Survivor> survivors = service.getAllSurvivors();

            byte[] body = mapper.writeValueAsBytes(survivors);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }


        } else if (exchange.getRequestMethod().equals("POST")) {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            JsonNode json = mapper.readTree(requestBody);
            String name = json.get("name").asString();
            int id = service.createSurvivor(name);
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        }
    }
}