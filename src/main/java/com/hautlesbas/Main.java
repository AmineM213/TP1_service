package com.hautlesbas;

import com.hautlesbas.service.ServiceInventaire;
import com.hautlesbas.service.ServiceVente;
import com.hautlesbas.api.ChaussetteHandler;
import com.hautlesbas.api.VenteHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        ServiceInventaire serviceInventaire = new ServiceInventaire();
        ServiceVente serviceVente = new ServiceVente(serviceInventaire);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/chaussettes", new ChaussetteHandler(serviceInventaire));
        server.createContext("/ventes", new VenteHandler(serviceVente));
        server.setExecutor(null);
        server.start();

        System.out.println("Serveur démarré sur le port 8080");
        // Fais attention, "http://localhost:8080" n'est pas toujours valide
//        System.out.println("API Chaussettes disponible sur: http://localhost:8080/chaussettes");
//        System.out.println("API Ventes disponible sur: http://localhost:8080/ventes");
    }
}