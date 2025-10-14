package com.hautlesbas.model;

public class Chaussette {
    private int identifiant;
    private String couleur;
    private String taille;
    private String typeTissu;
    private double prix;

    public Chaussette() {}

    public Chaussette(int identifiant, String couleur, String taille, String typeTissu, double prix) {
        this.identifiant = identifiant;
        this.couleur = couleur;
        this.taille = taille;
        this.typeTissu = typeTissu;
        this.prix = prix;
    }

    public int getIdentifiant() { return identifiant; }
    public void setIdentifiant(int identifiant) { this.identifiant = identifiant; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public String getTaille() { return taille; }
    public void setTaille(String taille) { this.taille = taille; }

    public String getTypeTissu() { return typeTissu; }
    public void setTypeTissu(String typeTissu) { this.typeTissu = typeTissu; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
}