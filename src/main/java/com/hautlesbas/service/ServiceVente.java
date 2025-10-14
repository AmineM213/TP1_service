package com.hautlesbas.service;

import com.hautlesbas.model.Vente;
import com.hautlesbas.model.Chaussette;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceVente {
    private List<Vente> ventes;
    private int prochainId;
    private ServiceInventaire serviceInventaire;

    public ServiceVente(ServiceInventaire serviceInventaire) {
        this.ventes = new ArrayList<>();
        this.prochainId = 1;
        this.serviceInventaire = serviceInventaire;
    }

    public void creerVente(Vente vente) {
        for (Chaussette chaussette : vente.getChaussettes()) {
            if (serviceInventaire.obtenirChaussette(chaussette.getIdentifiant()) == null) {
                throw new IllegalArgumentException("Chaussette non trouvée avec l'ID: " + chaussette.getIdentifiant());
            }
        }

        vente.setIdentifiant(prochainId++);
        ventes.add(vente);

        for (Chaussette chaussette : vente.getChaussettes()) {
            serviceInventaire.supprimerChaussette(chaussette.getIdentifiant());
        }
    }

    public void annulerVente(int id) {
        Vente vente = rechercherVente(id);
        if (vente != null) {
            for (Chaussette chaussette : vente.getChaussettes()) {
                serviceInventaire.ajouterChaussette(chaussette);
            }
            ventes.removeIf(venteItem -> venteItem.getIdentifiant() == id);
        }
    }

    public List<Vente> listerVentes() {
        return new ArrayList<>(ventes);
    }

    public Vente rechercherVente(int id) {
        return ventes.stream()
                .filter(vente -> vente.getIdentifiant() == id)
                .findFirst()
                .orElse(null);
    }

    public Vente rechercherVenteParDate(Date date) {
        return ventes.stream()
                .filter(vente -> vente.getDateVente().equals(date))
                .findFirst()
                .orElse(null);
    }
}