package com.hautlesbas;

import com.hautlesbas.db.DatabaseManager;
import com.hautlesbas.service.ServiceInventaire;
import com.hautlesbas.service.ServiceVente;
import com.hautlesbas.api.ChaussetteHandler;
import com.hautlesbas.api.VenteHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Démarrage de l'application Hautlesbas...");

        DatabaseManager.getDb();

        ServiceInventaire serviceInventaire = new ServiceInventaire();
        ServiceVente serviceVente = new ServiceVente();

        ChaussetteHandler chaussetteHandler = new ChaussetteHandler(serviceInventaire);
        VenteHandler venteHandler = new VenteHandler(serviceVente);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/chaussettes", chaussetteHandler);
        server.createContext("/ventes", venteHandler);

        server.setExecutor(null);
        server.start();

        System.out.println("Serveur démarré sur le port 8080");
        System.out.println("-> API Chaussettes : http://localhost:8080/chaussettes");
        System.out.println("-> API Ventes      : http://localhost:8080/ventes");
    }
}