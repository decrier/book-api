package Uebung;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;

public class TestHandler implements HttpHandler
{
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equals(exchange.getRequestMethod())){
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String msg = "Hello World!";
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
