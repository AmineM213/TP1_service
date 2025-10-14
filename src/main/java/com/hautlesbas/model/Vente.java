package com.hautlesbas.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Vente {
    private int identifiant;
    private Date dateVente;
    private double total;
    private List<Chaussette> chaussettes;

    public Vente() {
        this.chaussettes = new ArrayList<>();
        this.dateVente = new Date();
    }

    public Vente(int identifiant, Date dateVente, List<Chaussette> chaussettes) {
        this.identifiant = identifiant;
        this.dateVente = dateVente;
        this.chaussettes = chaussettes;
        this.total = calculerTotal();
    }

    public void ajouterChaussette(Chaussette chaussette) {
        this.chaussettes.add(chaussette);
        this.total = calculerTotal();
    }

    public double calculerTotal() {
        return chaussettes.stream().mapToDouble(Chaussette::getPrix).sum();
    }

    public int getIdentifiant() { return identifiant; }
    public void setIdentifiant(int identifiant) { this.identifiant = identifiant; }

    public Date getDateVente() { return dateVente; }
    public void setDateVente(Date dateVente) { this.dateVente = dateVente; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<Chaussette> getChaussettes() { return chaussettes; }
    public void setChaussettes(List<Chaussette> chaussettes) {
        this.chaussettes = chaussettes;
        this.total = calculerTotal();
    }
}