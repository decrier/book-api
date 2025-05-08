package Uebung;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class TestApp {
     public static void main(String[] args) throws Exception {
         HttpServer testServer = HttpServer.create(new InetSocketAddress(8080), 0);

         TestHandler testHandler = new TestHandler();
         AuthHandler authHandler = new AuthHandler(testHandler);

         testServer.createContext("/test", authHandler);
         testServer.start();
         System.out.println("Test-Server started at http://localhost:8080/");
    }
}