package com.hautlesbas.api;

import com.hautlesbas.service.ServiceVente;
import com.hautlesbas.model.Vente;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class VenteHandler extends ApiHandler implements HttpHandler {
    private ServiceVente serviceVente;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public VenteHandler(ServiceVente serviceVente) {
        this.serviceVente = serviceVente;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendError(exchange, 405, "Méthode non autorisée");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Erreur interne: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/ventes")) {
            List<Vente> ventes = serviceVente.listerVentes();
            sendResponse(exchange, 200, ventes);
        } else if (path.startsWith("/ventes/")) {
            String idStr = getPathParameter(path, 2);
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Vente vente = serviceVente.rechercherVente(id);
                    if (vente != null) {
                        sendResponse(exchange, 200, vente);
                    } else {
                        sendError(exchange, 404, "Vente non trouvée");
                    }
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "ID invalide");
                }
            }
        } else if (path.startsWith("/ventes/recherche/date")) {
            String query = exchange.getRequestURI().getQuery();
            String dateStr = getQueryParam(query, "date");
            if (dateStr != null) {
                try {
                    Vente vente = serviceVente.rechercherVenteParDate(dateFormat.parse(dateStr));
                    if (vente != null) {
                        sendResponse(exchange, 200, vente);
                    } else {
                        sendError(exchange, 404, "Vente non trouvée pour cette date");
                    }
                } catch (ParseException e) {
                    sendError(exchange, 400, "Format de date invalide. Utilisez yyyy-MM-dd");
                }
            } else {
                sendError(exchange, 400, "Paramètre date manquant");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Vente vente = parseRequestBody(exchange.getRequestBody(), Vente.class);
        serviceVente.creerVente(vente);
        sendResponse(exchange, 201, new MessageResponse("Vente créée avec succès"));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String idStr = getPathParameter(path, 2);
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                serviceVente.annulerVente(id);
                sendResponse(exchange, 200, new MessageResponse("Vente annulée avec succès"));
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "ID invalide");
            }
        } else {
            sendError(exchange, 400, "ID manquant");
        }
    }

    private String getQueryParam(String query, String paramName) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    private static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}