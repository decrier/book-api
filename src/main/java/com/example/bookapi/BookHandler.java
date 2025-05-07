package com.example.bookapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public class BookHandler implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BookDAO dao;

    public BookHandler(BookDAO dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        byte[] respBytes;

        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.substring(3));
            Book book = dao.findById(id);
            if (book == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            respBytes = mapper.writeValueAsBytes(book);
        } else {
            List<Book> all = dao.findAll();
            respBytes = mapper.writeValueAsBytes(all);
        }

        sendJsonResponse(exchange, 200, respBytes);
    }

    private void handlePost(HttpExchange exchange) throws Exception {
        Book newBook = mapper.readValue(exchange.getRequestBody(), Book.class);

        // Prüfen, dass title und author nicht leer sind
        if (newBook.getTitle() == null || newBook.getTitle().trim().isEmpty()
                || newBook.getAuthor() == null || newBook.getAuthor().trim().isEmpty()) {
            sendTextResponse(exchange, 400, "Titel und Autor sind benötigt.");
            return;
        }

        Optional<Book> existing = dao.findByTitleAndAuthor(newBook.getTitle(), newBook.getAuthor());
        if (existing.isPresent()) {
            sendTextResponse(exchange, 409, "Dieses Buch existiert bereits");
            return;
        }

        boolean ok = dao.save(newBook);
        if (!ok) {
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        byte[] bytes = mapper.writeValueAsBytes(newBook);
        sendJsonResponse(exchange, 201,bytes);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, byte[] data) throws Exception {
        exchange.getResponseHeaders()
                .add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()){
            os.write(data);
        }
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String msg) throws Exception {
        byte[] bytes = msg.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()){
            os.write(bytes);
        }
    }
}
