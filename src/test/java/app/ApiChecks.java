package app;


import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
public class ApiChecks {
    static void assertEquals(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    static void assertTrue(boolean value) { if (!value) throw new AssertionError("Condition failed"); }
    public static void main(String[] args) throws Exception {
        for (String name : new String[]{"lifecycleAndConsumption", "errorsAndAtomicUpdate", "itemsCrudAndOwnershipCleanup"}) {
            ApiChecks test = new ApiChecks(); test.start();
            try { ApiChecks.class.getDeclaredMethod(name).invoke(test); System.out.println("PASS " + name); }
            finally { test.stop(); }
        }
    }
    HttpServer server; String base;
    HttpClient client = HttpClient.newHttpClient();
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost",0),0);
        server.createContext("/",new SurvivorHandler(new SurvivalService()));
        server.start(); base="http://localhost:"+server.getAddress().getPort();
    }
    void stop(){ server.stop(0); }
    HttpResponse<String> call(String method,String path,String json) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/json")
            .method(method,HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }
    void lifecycleAndConsumption() throws Exception {
        assertEquals(201,call("POST","/survivors","{\"name\":\"James\"}").statusCode());
        assertEquals(201,call("POST","/items","{\"type\":\"Medkit\"}").statusCode());
        assertEquals(200,call("POST","/survivors/1/damage","{\"amount\":60}").statusCode());
        assertEquals(200,call("POST","/survivors/1/inventory","{\"itemId\":1}").statusCode());
        assertEquals(409,call("POST","/survivors/1/inventory","{\"itemId\":1}").statusCode());
        var used=call("POST","/survivors/1/inventory/1/use","");
        assertEquals(200,used.statusCode()); assertTrue(used.body().contains("\"health\":80"));
        assertEquals(404,call("GET","/items/1","").statusCode());
        assertEquals(200,call("PUT","/survivors/1","{\"name\":\"Heather\",\"health\":0,\"stamina\":50}").statusCode());
        assertEquals("[]",call("GET","/survivors?alive=true","").body());
        assertTrue(call("GET","/survivors?name=heat","").body().contains("Heather"));
        assertEquals(204,call("DELETE","/survivors/1","").statusCode());
        assertEquals(404,call("GET","/survivors/1","").statusCode());
    }
    void errorsAndAtomicUpdate() throws Exception {
        assertEquals(400,call("POST","/survivors","{").statusCode());
        assertEquals(400,call("POST","/survivors","{\"name\":null}").statusCode());
        call("POST","/survivors","{\"name\":\"James\"}");
        assertEquals(400,call("PUT","/survivors/1","{\"name\":\"Other\",\"health\":101,\"stamina\":50}").statusCode());
        assertTrue(call("GET","/survivors/1","").body().contains("James"));
        assertEquals(400,call("GET","/survivors/no","").statusCode());
        assertEquals(404,call("GET","/survivors/1/unknown","").statusCode());
        assertEquals(405,call("PATCH","/survivors/1","").statusCode());
        assertEquals(400,call("GET","/survivors?alive=maybe","").statusCode());
    }
    void itemsCrudAndOwnershipCleanup() throws Exception {
        call("POST","/survivors","{\"name\":\"James\"}");
        call("POST","/items","{\"type\":\"Medkit\"}");
        assertEquals(200,call("PUT","/items/1","{\"type\":\"EnergyDrink\"}").statusCode());
        assertEquals("[]",call("GET","/items?type=Medkit","").body());
        call("POST","/survivors/1/inventory","{\"itemId\":1}");
        assertEquals(204,call("DELETE","/items/1","").statusCode());
        assertEquals("[]",call("GET","/survivors/1/inventory","").body());
    }
}


