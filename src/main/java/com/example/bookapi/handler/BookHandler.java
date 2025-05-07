package com.example.bookapi.handler;

import com.example.bookapi.dao.BookDAO;
import com.example.bookapi.model.Book;
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
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
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

    private void handlePut(HttpExchange exchange) throws Exception {
        Book book =mapper.readValue(exchange.getRequestBody(), Book.class);
        if (book.getId() <= 0
            || book.getTitle() == null || book.getTitle().isBlank()
            || book.getAuthor() == null || book.getAuthor().isBlank()) {
            sendTextResponse(exchange, 400, "Falsche ID, Titel oder Autor");
            return;
        }

        if (dao.findById(book.getId()) == null) {
            sendTextResponse(exchange, 404, "Buch nicht gefunden");
            return;
        }

        boolean ok = dao.update(book);
        if (!ok) {
            sendTextResponse(exchange, 500, "Updatefehler");
            return;
        }
        byte[] data = mapper.writeValueAsBytes(book);
        sendJsonResponse(exchange,200, data);
    }

    private void handleDelete(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("id=")) {
            sendTextResponse(exchange, 400, "ID-Parameter fehlt");
            return;
        }

        int id = Integer.parseInt(query.substring(3));
        boolean ok = dao.delete(id);
        if (!ok) {
            sendTextResponse(exchange, 404, "Buch nicht gefunden");
            return;
        }

        exchange.sendResponseHeaders(204, -1);

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
