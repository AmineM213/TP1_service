package com.hautlesbas.service;

import com.hautlesbas.model.Chaussette;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceInventaire {
    private List<Chaussette> inventaire;
    private int prochainId;

    public ServiceInventaire() {
        this.inventaire = new ArrayList<>();
        this.prochainId = 1;
    }

    public void ajouterChaussette(Chaussette chaussette) {
        chaussette.setIdentifiant(prochainId++);
        inventaire.add(chaussette);
    }

    public void modifierChaussette(int id, Chaussette chaussetteModifiee) {
        for (int i = 0; i < inventaire.size(); i++) {
            if (inventaire.get(i).getIdentifiant() == id) {
                chaussetteModifiee.setIdentifiant(id);
                inventaire.set(i, chaussetteModifiee);
                return;
            }
        }
        throw new IllegalArgumentException("Chaussette non trouvée avec l'ID: " + id);
    }

    public void supprimerChaussette(int id) {
        inventaire.removeIf(chaussette -> chaussette.getIdentifiant() == id);
    }

    public List<Chaussette> listerChaussettes() {
        return new ArrayList<>(inventaire);
    }

    public List<Chaussette> rechercherChaussette(String couleur, String taille) {
        return inventaire.stream()
                .filter(chaussette ->
                        (couleur == null || chaussette.getCouleur().equalsIgnoreCase(couleur)) &&
                                (taille == null || chaussette.getTaille().equalsIgnoreCase(taille)))
                .collect(Collectors.toList());
    }

    public Chaussette obtenirChaussette(int id) {
        return inventaire.stream()
                .filter(chaussette -> chaussette.getIdentifiant() == id)
                .findFirst()
                .orElse(null);
    }
}