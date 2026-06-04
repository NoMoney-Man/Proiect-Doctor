package Proiect;

import java.sql.*;

public class Utilizator {

    protected int id;
    protected String nume;
    protected String prenume;
    protected String email;
    protected String nrTelefon;
    protected String dataNasterii;
    protected String parola;
    protected String gen;

    public Utilizator() {}

    public Utilizator(int id, String nume, String prenume, String email, String nrTelefon, String dataNasterii, String parola, String gen) {
        this.id = id;
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.nrTelefon = nrTelefon;
        this.dataNasterii = dataNasterii;
        this.parola = parola;
        this.gen = gen;
    }

    public Utilizator login(String email, String parola) {
        String[] tabele = {"pacienti", "medici"};

        for (String tabel : tabele) {
            String sql = "SELECT * FROM " + tabel + " WHERE email=? AND parola=?";

            try (Connection con = DBConnection.getConn();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, parola);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    if (tabel.equals("pacienti")) {
                        return new Pacient(
                                rs.getInt("id"),
                                rs.getString("nume"),
                                rs.getString("prenume"),
                                rs.getString("email"),
                                rs.getString("telefon"),
                                rs.getString("dataNasterii"),
                                rs.getString("parola"),
                                rs.getString("gen")
                        );
                    } else {
                        return new Medic(
                                rs.getInt("id"),
                                rs.getString("nume"),
                                rs.getString("prenume"),
                                rs.getString("email"),
                                rs.getString("telefon"),
                                rs.getDate("dataNasterii"),
                                rs.getString("parola"),
                                rs.getString("gen"),
                                rs.getString("specializare")
                        );
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean utilizatorExistent(String email) {
        String sql1 = "SELECT id FROM pacienti WHERE email=?";
        String sql2 = "SELECT id FROM medici WHERE email=?";

        try (Connection con = DBConnection.getConn()) {

            try (PreparedStatement ps = con.prepareStatement(sql1)) {
                ps.setString(1, email);
                if (ps.executeQuery().next()) return true;
            }

            try (PreparedStatement ps = con.prepareStatement(sql2)) {
                ps.setString(1, email);
                if (ps.executeQuery().next()) return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getNume() { return nume; }
    public String getPrenume() { return prenume; }
    public String getDataNasterii() { return dataNasterii; }
    public int getId() { return id; }
    public String getParola() { return parola; }
    public String getGen() { return gen; }
    public String getEmail() { return email; }
    public String getNrTelefon() { return nrTelefon; }
}