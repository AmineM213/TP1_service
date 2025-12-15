package com.hautlesbas.service;

import com.hautlesbas.db.DatabaseManager;
import com.hautlesbas.model.Chaussette;
import com.hautlesbas.model.Vente;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceVente {
    private final DatabaseManager dbManager;

    // Constructeur : On utilise DatabaseManager, PAS ServiceInventaire
    public ServiceVente() {
        this.dbManager = DatabaseManager.getDb();
    }

    public void creerVente(Vente vente) throws SQLException {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false); // DÉBUT TRANSACTION

            // 1. Vérifier stocks et calculer total
            double totalCalcul = 0;
            List<Chaussette> chaussettesCompletes = new ArrayList<>();

            // On vérifie directement en SQL si la chaussette existe
            String sqlCheck = "SELECT * FROM chaussette WHERE id = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                for (Chaussette c : vente.getChaussettes()) {
                    psCheck.setInt(1, c.getIdentifiant());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Chaussette ID " + c.getIdentifiant() + " n'est plus disponible.");
                        }
                        // On recharge les données complètes depuis la BD
                        Chaussette fullSock = new Chaussette(
                                rs.getInt("id"), rs.getString("couleur"),
                                rs.getString("taille"), rs.getString("type_tissu"),
                                rs.getDouble("prix")
                        );
                        chaussettesCompletes.add(fullSock);
                        totalCalcul += fullSock.getPrix();
                    }
                }
            }
            vente.setTotal(totalCalcul);

            // 2. Insérer la vente
            String sqlInsertVente = "INSERT INTO vente (date_vente, total) VALUES (?, ?)";
            int venteId;
            try (PreparedStatement psVente = conn.prepareStatement(sqlInsertVente, Statement.RETURN_GENERATED_KEYS)) {
                psVente.setTimestamp(1, new Timestamp(new Date().getTime()));
                psVente.setDouble(2, totalCalcul);
                psVente.executeUpdate();
                try (ResultSet rs = psVente.getGeneratedKeys()) {
                    if (rs.next()) venteId = rs.getInt(1);
                    else throw new SQLException("Échec création vente, ID non retourné.");
                }
            }
            vente.setIdentifiant(venteId);

            // 3. Déplacer les chaussettes (Delete Inventory -> Insert Archive)
            String sqlDeleteInv = "DELETE FROM chaussette WHERE id = ?";
            String sqlInsertDetail = "INSERT INTO vente_detail (vente_id, chaussette_id_org, couleur, taille, type_tissu, prix) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteInv);
                 PreparedStatement psDetail = conn.prepareStatement(sqlInsertDetail)) {

                for (Chaussette c : chaussettesCompletes) {
                    // Supprimer de l'inventaire
                    psDel.setInt(1, c.getIdentifiant());
                    psDel.executeUpdate();

                    // Archiver dans les détails
                    psDetail.setInt(1, venteId);
                    psDetail.setInt(2, c.getIdentifiant());
                    psDetail.setString(3, c.getCouleur());
                    psDetail.setString(4, c.getTaille());
                    psDetail.setString(5, c.getTypeTissu());
                    psDetail.setDouble(6, c.getPrix());
                    psDetail.executeUpdate();
                }
            }

            conn.commit(); // VALIDATION TRANSACTION
        } catch (Exception e) {
            if (conn != null) conn.rollback(); // ANNULATION SI ERREUR
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void annulerVente(int idVente) throws SQLException {
        Connection conn = null;
        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Récupérer les détails pour restaurer l'inventaire
            String sqlGetDetails = "SELECT * FROM vente_detail WHERE vente_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlGetDetails)) {
                ps.setInt(1, idVente);
                try (ResultSet rs = ps.executeQuery()) {
                    String sqlRestore = "INSERT INTO chaussette (couleur, taille, type_tissu, prix) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psRestore = conn.prepareStatement(sqlRestore)) {
                        while(rs.next()) {
                            psRestore.setString(1, rs.getString("couleur"));
                            psRestore.setString(2, rs.getString("taille"));
                            psRestore.setString(3, rs.getString("type_tissu"));
                            psRestore.setDouble(4, rs.getDouble("prix"));
                            psRestore.executeUpdate();
                        }
                    }
                }
            }

            // 2. Supprimer la vente (Cascade supprimera les détails)
            String sqlDeleteVente = "DELETE FROM vente WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteVente)) {
                ps.setInt(1, idVente);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new IllegalArgumentException("Vente introuvable ID: " + idVente);
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Vente> listerVentes() throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM vente ORDER BY date_vente DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vente v = new Vente();
                v.setIdentifiant(rs.getInt("id"));
                v.setDateVente(rs.getTimestamp("date_vente"));
                v.setTotal(rs.getDouble("total"));
                v.setChaussettes(getDetailsVente(conn, v.getIdentifiant()));
                ventes.add(v);
            }
        }
        return ventes;
    }

    public Vente rechercherVente(int id) throws SQLException {
        String sql = "SELECT * FROM vente WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Vente v = new Vente();
                    v.setIdentifiant(rs.getInt("id"));
                    v.setDateVente(rs.getTimestamp("date_vente"));
                    v.setTotal(rs.getDouble("total"));
                    v.setChaussettes(getDetailsVente(conn, v.getIdentifiant()));
                    return v;
                }
            }
        }
        return null;
    }

    public List<Vente> rechercherVentesParDate(java.util.Date debut, java.util.Date fin) throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM vente WHERE date_vente BETWEEN ? AND ? ORDER BY date_vente DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(debut.getTime()));
            ps.setTimestamp(2, new Timestamp(fin.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vente v = new Vente();
                    v.setIdentifiant(rs.getInt("id"));
                    v.setDateVente(rs.getTimestamp("date_vente"));
                    v.setTotal(rs.getDouble("total"));
                    v.setChaussettes(getDetailsVente(conn, v.getIdentifiant()));
                    ventes.add(v);
                }
            }
        }
        return ventes;
    }

    private List<Chaussette> getDetailsVente(Connection conn, int venteId) throws SQLException {
        List<Chaussette> details = new ArrayList<>();
        String sql = "SELECT * FROM vente_detail WHERE vente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venteId);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    Chaussette c = new Chaussette(
                            rs.getInt("chaussette_id_org"),
                            rs.getString("couleur"),
                            rs.getString("taille"),
                            rs.getString("type_tissu"),
                            rs.getDouble("prix")
                    );
                    details.add(c);
                }
            }
        }
        return details;
    }
}