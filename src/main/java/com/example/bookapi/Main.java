package com.example.bookapi;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main{
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());

        BookDAO dao = new BookDAOimpl();
        server.createContext("/book", new BookHandler(dao));
        server.start();
        System.out.println("Server started at http://localhost:8080/");
    }

    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if(!"GET".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                String msg = "Hello, Book Api!";
                byte[] bytes = msg.getBytes("UTF-8");

                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}