package com.example.bookapi;

import com.example.bookapi.dao.BookDAO;
import com.example.bookapi.dao.BookDAOimpl;
import com.example.bookapi.dao.UserDAO;
import com.example.bookapi.dao.UserDAOimpl;
import com.example.bookapi.handler.BasicAuthHandler;
import com.example.bookapi.handler.BookHandler;
import com.example.bookapi.handler.MetricsHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.flywaydb.core.Flyway;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main{

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        logger.info("Starting application...");

        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:mariadb://localhost:3306/book_db", "root", "")
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();



        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloHandler());

        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        server.createContext("/metrics", new MetricsHandler(registry));

        BookDAO bookDao = new BookDAOimpl();
        BookHandler bookHandler = new BookHandler(bookDao, registry);

        UserDAO userDAO = new UserDAOimpl();
        BasicAuthHandler authHandler = new BasicAuthHandler(bookHandler,userDAO);

        server.createContext("/book", authHandler);
        server.start();
        logger.info("Server started at http://localhost:8080/");

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