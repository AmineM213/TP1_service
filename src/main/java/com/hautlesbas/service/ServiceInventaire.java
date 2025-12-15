package com.hautlesbas.service;

import com.hautlesbas.db.DatabaseManager;
import com.hautlesbas.model.Chaussette;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInventaire {
    private final DatabaseManager dbManager;

    public ServiceInventaire() {
        this.dbManager = DatabaseManager.getDb();
    }

    public void ajouterChaussette(Chaussette chaussette) throws SQLException {
        String sql = "INSERT INTO chaussette (couleur, taille, type_tissu, prix) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, chaussette.getCouleur());
            ps.setString(2, chaussette.getTaille());
            ps.setString(3, chaussette.getTypeTissu());
            ps.setDouble(4, chaussette.getPrix());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    chaussette.setIdentifiant(rs.getInt(1));
                }
            }
        }
    }

    public void modifierChaussette(int id, Chaussette chaussette) throws SQLException {
        String sql = "UPDATE chaussette SET couleur=?, taille=?, type_tissu=?, prix=? WHERE id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, chaussette.getCouleur());
            ps.setString(2, chaussette.getTaille());
            ps.setString(3, chaussette.getTypeTissu());
            ps.setDouble(4, chaussette.getPrix());
            ps.setInt(5, id);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Chaussette non trouvée avec l'ID: " + id);
            }
        }
    }

    public void supprimerChaussette(int id) throws SQLException {
        String sql = "DELETE FROM chaussette WHERE id=?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Chaussette> listerChaussettes() throws SQLException {
        List<Chaussette> liste = new ArrayList<>();
        String sql = "SELECT * FROM chaussette";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSetToChaussette(rs));
            }
        }
        return liste;
    }

    public List<Chaussette> rechercherChaussette(String couleur, String taille) throws SQLException {
        List<Chaussette> liste = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM chaussette WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (couleur != null && !couleur.isEmpty()) {
            sql.append(" AND couleur = ?");
            params.add(couleur);
        }
        if (taille != null && !taille.isEmpty()) {
            sql.append(" AND taille = ?");
            params.add(taille);
        }

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToChaussette(rs));
                }
            }
        }
        return liste;
    }

    public Chaussette obtenirChaussette(int id) throws SQLException {
        String sql = "SELECT * FROM chaussette WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChaussette(rs);
                }
            }
        }
        return null;
    }

    private Chaussette mapResultSetToChaussette(ResultSet rs) throws SQLException {
        return new Chaussette(
                rs.getInt("id"),
                rs.getString("couleur"),
                rs.getString("taille"),
                rs.getString("type_tissu"),
                rs.getDouble("prix")
        );
    }
}