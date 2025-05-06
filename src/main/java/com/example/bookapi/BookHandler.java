package com.example.bookapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.util.List;

public class BookHandler implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            BookDAO dao = new BookDAO();

            if ("GET".equals(method)) {
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

                exchange.getResponseHeaders()
                        .add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, respBytes.length);
                try (OutputStream os = exchange.getResponseBody()){
                    os.write(respBytes);
                }
                return;
            }

            if ("POST".equals(method)) {
                Book newBook = mapper.readValue(exchange.getRequestBody(), Book.class);

                boolean ok = dao.save(newBook);
                if (!ok) {
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }

                byte[] bytes = mapper.writeValueAsBytes(newBook);
                exchange.getResponseHeaders()
                        .add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(201, bytes.length);

                try (OutputStream os = exchange.getResponseBody()){
                    os.write(bytes);
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
