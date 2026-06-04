package Proiect;

import java.sql.*;

public class Pacient extends Utilizator {

    public Pacient() {}

    public Pacient(int id, String nume, String prenume, String email, String tel, String data, String pass, String gen) {
        super(id, nume, prenume, email, tel, data, pass, gen);
    }

    public boolean registerPacient(String nume, String prenume, String email, String nrTelefon, String data, String gen, String pass) {

        if (utilizatorExistent(email)) return false;

        String sql = "INSERT INTO pacienti (nume, prenume, email, nrTelefon, dataNasterii, gen, parola) VALUES (?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nume);
            ps.setString(2, prenume);
            ps.setString(3, email);
            ps.setString(4, nrTelefon);
            ps.setDate(5, Date.valueOf(data));
            ps.setString(6, gen);
            ps.setString(7, pass);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}