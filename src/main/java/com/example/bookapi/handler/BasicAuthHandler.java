package com.example.bookapi.handler;

import com.example.bookapi.model.User;
import com.example.bookapi.dao.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class BasicAuthHandler implements HttpHandler {
    private final HttpHandler next;
    private final UserDAO userDAO;

    public BasicAuthHandler(HttpHandler next, UserDAO userDAO) {
        this.next = next;
        this.userDAO = userDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String auth= exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            sendUnauthorized(exchange);
            return;
        }

        String base64 = auth.substring(6);
        String credentials;
        try {
            credentials = new String(
                    Base64.getDecoder().decode(base64),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            sendUnauthorized(exchange);
            return;
        }

        String [] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            sendUnauthorized(exchange);
            return;
        }

        String user = parts[0], pass = parts[1];

        Optional<User> maybeUser = userDAO.findByUsername(user);
        if (maybeUser.isEmpty() || !maybeUser.get().getPassword().equals(pass)) {
            sendUnauthorized(exchange);
            return;
        }

        next.handle(exchange);
    }

    private void sendUnauthorized(HttpExchange exchange) throws  IOException {
        exchange.getResponseHeaders()
                .add("WWW-Authenticate", "Basic realm=\"Books\"");
        exchange.sendResponseHeaders(401, -1);
    }
}
