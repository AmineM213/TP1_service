package com.hautlesbas.model;

import java.util.ArrayList;
import java.util.Date;
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

    public double calculerTotal() {
        if (chaussettes == null) return 0.0;
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