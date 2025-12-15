package com.hautlesbas.api;

import com.hautlesbas.service.ServiceVente;
import com.hautlesbas.model.Vente;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VenteHandler extends ApiHandler implements HttpHandler {
    private final ServiceVente serviceVente;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public VenteHandler(ServiceVente serviceVente) {
        this.serviceVente = serviceVente;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendError(exchange, 405, "Méthode non autorisée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Erreur base de données");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Erreur interne: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException, SQLException {
        if (path.equals("/ventes")) {
            List<Vente> ventes = serviceVente.listerVentes();
            sendResponse(exchange, 200, ventes);
        }
        // C'est ici que la logique a changé pour gérer une PLAGE de dates
        else if (path.startsWith("/ventes/recherche/date")) {
            String query = exchange.getRequestURI().getQuery();
            String startStr = getQueryParam(query, "start");
            String endStr = getQueryParam(query, "end");

            if (startStr != null && endStr != null) {
                try {
                    Date start = dateFormat.parse(startStr);
                    Date end = dateFormat.parse(endStr);
                    if (end.before(start)) {
                        sendError(exchange, 400, "La date de fin doit être après la date de début");
                        return;
                    }
                    // Appel de la méthode au PLURIEL avec deux arguments
                    List<Vente> resultats = serviceVente.rechercherVentesParDate(start, end);
                    sendResponse(exchange, 200, resultats);
                } catch (ParseException e) {
                    sendError(exchange, 400, "Format de date invalide. Utilisez yyyy-MM-dd");
                }
            } else {
                sendError(exchange, 400, "Paramètres 'start' et 'end' requis (ex: ?start=2023-01-01&end=2023-12-31)");
            }
        }
        else if (path.startsWith("/ventes/")) {
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
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        try {
            Vente vente = parseRequestBody(exchange.getRequestBody(), Vente.class);
            if (vente.getChaussettes() == null || vente.getChaussettes().isEmpty()) {
                sendError(exchange, 400, "La vente doit contenir au moins une chaussette");
                return;
            }
            serviceVente.creerVente(vente);
            sendResponse(exchange, 201, new MessageResponse("Vente créée avec succès, ID: " + vente.getIdentifiant()));
        } catch (IllegalArgumentException e) {
            sendError(exchange, 409, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException, SQLException {
        String idStr = getPathParameter(path, 2);
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                serviceVente.annulerVente(id);
                sendResponse(exchange, 200, new MessageResponse("Vente annulée avec succès"));
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "ID invalide");
            } catch (IllegalArgumentException e) {
                sendError(exchange, 404, e.getMessage());
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

    private record MessageResponse(String message) {}
}