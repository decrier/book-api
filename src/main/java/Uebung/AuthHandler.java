package Uebung;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthHandler implements HttpHandler {
    private HttpHandler next;

    public AuthHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final String TESTUSER = "admin";
        final String TESTPASS = "1111";

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        String base64 = auth.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        String[] parts = credentials.split(":", 2);
        String user = parts[0];
        String pass = parts[1];

        if (!user.equals(TESTUSER) || !pass.equals(TESTPASS)){
            exchange.getResponseHeaders().add("FATAL", "ERROR!!!!!!!");
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        next.handle(exchange);

    }
}
