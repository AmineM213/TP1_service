package com.hautlesbas.api;

import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class ApiHandler {
    protected Gson gson = new Gson();

    protected void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = gson.toJson(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendResponse(exchange, statusCode, message);
    }

    protected <T> T parseRequestBody(InputStream body, Class<T> classOfT) throws IOException {
        String bodyString = new String(body.readAllBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(bodyString, classOfT);
    }

    protected String getPathParameter(String path, int index) {
        String[] parts = path.split("/");
        if (index < parts.length) {
            return parts[index];
        }
        return null;
    }

//    protected static class ErrorResponse {
//        private String error;
//
//        public ErrorResponse(String error) {
//            this.error = error;
//        }
//
//        public String getError() { return error; }
//    }
}