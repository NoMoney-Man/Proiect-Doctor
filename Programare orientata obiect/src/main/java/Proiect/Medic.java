package Proiect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Medic extends Utilizator {

    private String specializare;

    public Medic() {}

    public Medic(int id, String nume, String prenume, String email, String tel, Date data, String pass, String gen, String spec) {
        super(id, nume, prenume, email, tel, data.toString(), pass, gen);
        this.specializare = spec;
    }

    public boolean registerMedic(String nume, String prenume, String email, String nrTelefon, String data, String gen, String pass, String spec) {

        if (utilizatorExistent(email)) return false;

        String sql = "INSERT INTO medici (nume, prenume, email, nrTelefon, dataNasterii, gen, parola, specializare) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nume);
            ps.setString(2, prenume);
            ps.setString(3, email);
            ps.setString(4, nrTelefon);
            ps.setDate(5, Date.valueOf(data));
            ps.setString(6, gen);
            ps.setString(7, pass);
            ps.setString(8, spec);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Medic[] listaMedici() {

        List<Medic> lista = new ArrayList<>();

        String sql = "SELECT * FROM medici";

        try (Connection con = DBConnection.getConn();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Medic(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getString("email"),
                        rs.getString("telefon"),
                        rs.getDate("dataNasterii"),
                        null,
                        rs.getString("gen"),
                        rs.getString("specializare")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista.toArray(new Medic[0]);
    }

    public String getSpecializare() {
        return specializare;
    }
}