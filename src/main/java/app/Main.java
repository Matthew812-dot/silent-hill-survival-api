package app;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;


public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {SurvivalService service = new SurvivalService();
        int jamesId = service.createSurvivor("James");
        Survivor james = service.getSurvivor(jamesId);
        System.out.println("Здоровье до урона: " + james.getHealth());
        service.damageSurvivor(jamesId, 60);
        System.out.println("Здоровье после урона: " + james.getHealth());
        service.useItem(jamesId, new Medkit());
        System.out.println("Здоровье после аптечки: " + james.getHealth());

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Каждый контекст — это префикс пути и обработчик для него.
        // Внимание: контекст "/ping" получит и запросы на "/ping/что-угодно".
        server.createContext("/ping", new PingHandler());

        // TODO: зарегистрируйте здесь обработчики своих ресурсов

        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT + "/ping");
    }
}
