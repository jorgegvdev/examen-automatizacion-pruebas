package org.taller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TareaHttpServer {

    private static final TareaService tareaService = new TareaService(new InMemoryTareaRepository());
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TITULO_PATTERN = Pattern.compile("\"titulo\"\\s*:\\s*\"([^\"]*)\"");

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/health", TareaHttpServer::handleHealth);
        server.createContext("/tareas", TareaHttpServer::handleTareas);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Servidor de tareas escuchando en http://0.0.0.0:8080");
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        responder(exchange, 200, "{\"status\":\"UP\"}");
    }

    private static void handleTareas(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(metodo) && path.equals("/tareas")) {
                crearTarea(exchange);
            } else if ("GET".equalsIgnoreCase(metodo) && path.equals("/tareas")) {
                listarTareas(exchange);
            } else if ("POST".equalsIgnoreCase(metodo) && path.matches("/tareas/[^/]+/completar")) {
                String id = path.split("/")[2];
                completarTarea(exchange, id);
            } else {
                responder(exchange, 404, "{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (NoSuchElementException e) {
            responder(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            responder(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private static void crearTarea(HttpExchange exchange) throws IOException {
        String body = leerCuerpo(exchange);
        String id = extraer(ID_PATTERN, body);
        String titulo = extraer(TITULO_PATTERN, body);
        Tarea tarea = tareaService.crearTarea(id, titulo);
        responder(exchange, 201, "{\"id\":\"" + tarea.getId() + "\",\"titulo\":\"" + tarea.getTitulo() + "\"}");
    }

    private static void listarTareas(HttpExchange exchange) throws IOException {
        List<Tarea> tareas = tareaService.listarTareas();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tareas.size(); i++) {
            Tarea t = tareas.get(i);
            if (i > 0) json.append(",");
            json.append("{\"id\":\"").append(t.getId())
                .append("\",\"titulo\":\"").append(t.getTitulo())
                .append("\",\"completada\":").append(t.isCompletada()).append("}");
        }
        json.append("]");
        responder(exchange, 200, json.toString());
    }

    private static void completarTarea(HttpExchange exchange, String id) throws IOException {
        Tarea tarea = tareaService.completarTarea(id);
        responder(exchange, 200, "{\"id\":\"" + tarea.getId() + "\",\"completada\":" + tarea.isCompletada() + "}");
    }

    private static String leerCuerpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extraer(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static void responder(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
