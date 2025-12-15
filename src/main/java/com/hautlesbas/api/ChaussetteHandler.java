package com.hautlesbas.api;

import com.hautlesbas.service.ServiceInventaire;
import com.hautlesbas.model.Chaussette;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ChaussetteHandler extends ApiHandler implements HttpHandler {
    private ServiceInventaire serviceInventaire;

    public ChaussetteHandler(ServiceInventaire serviceInventaire) {
        this.serviceInventaire = serviceInventaire;
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
                case "PUT":
                    handlePut(exchange, path);
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

    private void handleGet(HttpExchange exchange, String path) throws IOException, SQLException {
        if (path.equals("/chaussettes")) {
            List<Chaussette> chaussettes = serviceInventaire.listerChaussettes();
            sendResponse(exchange, 200, chaussettes);
        } else if (path.startsWith("/chaussettes/recherche")) {
            String query = exchange.getRequestURI().getQuery();
            String couleur = getQueryParam(query, "couleur");
            String taille = getQueryParam(query, "taille");

            List<Chaussette> resultats = serviceInventaire.rechercherChaussette(couleur, taille);
            sendResponse(exchange, 200, resultats);
        } else if (path.startsWith("/chaussettes/")) {
            String idStr = getPathParameter(path, 2);
            if (idStr != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    Chaussette chaussette = serviceInventaire.obtenirChaussette(id);
                    if (chaussette != null) {
                        sendResponse(exchange, 200, chaussette);
                    } else {
                        sendError(exchange, 404, "Chaussette non trouvée");
                    }
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "ID invalide");
                }
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException, SQLException {
        Chaussette chaussette = parseRequestBody(exchange.getRequestBody(), Chaussette.class);
        serviceInventaire.ajouterChaussette(chaussette);
        sendResponse(exchange, 201, new MessageResponse("Chaussette ajoutée avec succès"));
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String idStr = getPathParameter(path, 2);
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                Chaussette chaussette = parseRequestBody(exchange.getRequestBody(), Chaussette.class);
                serviceInventaire.modifierChaussette(id, chaussette);
                sendResponse(exchange, 200, new MessageResponse("Chaussette modifiée avec succès"));
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "ID invalide");
            } catch (IllegalArgumentException | SQLException e) {
                sendError(exchange, 404, e.getMessage());
            }
        } else {
            sendError(exchange, 400, "ID manquant");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String idStr = getPathParameter(path, 2);
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                serviceInventaire.supprimerChaussette(id);
                sendResponse(exchange, 200, new MessageResponse("Chaussette supprimée avec succès"));
            } catch (NumberFormatException e) {
                sendError(exchange, 400, "ID invalide");
            } catch (SQLException e) {
                throw new RuntimeException(e);
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