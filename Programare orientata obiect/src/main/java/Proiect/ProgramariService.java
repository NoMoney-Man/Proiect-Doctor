package Proiect;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgramariService {

    public void creeazaProgramare(Utilizator u, String specializare, String data, int schimb) {

        if (data == null || data.isBlank()) {
            throw new RuntimeException("Data invalida");
        }

        java.sql.Date sqlDate;
        try {
            sqlDate = java.sql.Date.valueOf(data);
        } catch (Exception e) {
            throw new RuntimeException("Format data invalid: " + data);
        }

        if (schimb < 1 || schimb > 12) {
            throw new RuntimeException("Interval orar invalid");
        }

        String numePacient = u.getNume() + " " + u.getPrenume();

        String sqlFindMedic = "SELECT nume, prenume FROM medici WHERE LOWER(specializare)=? LIMIT 1";

        try (Connection con = DBConnection.getConn()) {

            String numeMedic = null;

            try (PreparedStatement ps = con.prepareStatement(sqlFindMedic)) {
                ps.setString(1, specializare.toLowerCase());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    numeMedic = rs.getString("nume") + " " + rs.getString("prenume");
                }
            }

            if (numeMedic == null) {
                throw new RuntimeException("Nu exista medic pentru specializare");
            }

            if (!esteDisponibil(data, numeMedic, schimb)) {
                throw new RuntimeException("Slot ocupat");
            }

            String sqlInsert =
                    "INSERT INTO programari (pacientNume, medicNume, data, oraBg, oraSf, status) " +
                            "VALUES (?,?,?,?,?,?)";

            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {

                ps.setString(1, numePacient);
                ps.setString(2, numeMedic);
                ps.setDate(3, sqlDate);
                ps.setTime(4, Time.valueOf(LocalTime.of(7 + schimb, 0)));
                ps.setTime(5, Time.valueOf(LocalTime.of(8 + schimb, 0)));
                ps.setString(6, "In asteptare");

                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Eroare la crearea programarii: " + e.getMessage());
        }
    }

    public boolean esteDisponibil(String data, String medic, int schimb) {

        if (data == null || data.isBlank()) return false;
        if (schimb < 1 || schimb > 12) return false;

        try {
            java.sql.Date sqlDate = java.sql.Date.valueOf(data);
            LocalTime ora = LocalTime.of(7 + schimb, 0);

            String sql =
                    "SELECT COUNT(*) FROM programari " +
                            "WHERE data=? AND oraBg=? AND medicNume=? AND status != 'Anulata'";

            try (Connection con = DBConnection.getConn();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDate(1, sqlDate);
                ps.setTime(2, Time.valueOf(ora));
                ps.setString(3, medic);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean[] getOreOcupate(String specializare, String data) {
        boolean[] oreOcupate = new boolean[13];

        try {
            java.sql.Date sqlDate = java.sql.Date.valueOf(data);

            String sql = "SELECT DISTINCT oraBg FROM programari p " +
                    "JOIN medici m ON p.medicNume = CONCAT(m.nume, ' ', m.prenume) " +
                    "WHERE LOWER(m.specializare) = LOWER(?) AND p.data = ? AND p.status != 'Anulata'";

            try (Connection con = DBConnection.getConn();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, specializare);
                ps.setDate(2, sqlDate);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    java.sql.Time oraBg = rs.getTime("oraBg");
                    int ora = oraBg.toLocalTime().getHour();
                    int schimb = ora - 7;
                    if (schimb >= 1 && schimb <= 12) {
                        oreOcupate[schimb] = true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return oreOcupate;
    }

    public void actualizeazaProgramariFinalizate() {

        String sql =
                "UPDATE programari " +
                        "SET status='Finalizata' " +
                        "WHERE status != 'Anulata' " +
                        "AND TIMESTAMP(data, oraSf) < NOW()";

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Programari[] getProgramari(Utilizator u) {

        String coloana = (u instanceof Pacient) ? "pacientNume" : "medicNume";

        String sql = "SELECT * FROM programari WHERE " + coloana + "=? " +
                "AND status != 'Finalizata' AND status != 'Anulata'";

        List<Programari> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNume() + " " + u.getPrenume());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Programari(
                        rs.getInt("id"),
                        rs.getString("pacientNume"),
                        rs.getString("medicNume"),
                        rs.getDate("data"),
                        rs.getTime("oraBg"),
                        rs.getTime("oraSf"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista.toArray(new Programari[0]);
    }

    public void confirma(int id) {
        updateStatus(id, "Confirmata");
    }

    public void anuleaza(int id) {
        updateStatus(id, "Anulata");
    }

    public Programari[] getIstoricProgramari(Utilizator u) {

        String coloana = (u instanceof Pacient) ? "pacientNume" : "medicNume";
        String sql = "SELECT * FROM programari WHERE " + coloana + "=? AND (status='Anulata' OR status='Finalizata')";

        List<Programari> lista = new ArrayList<>();

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNume() + " " + u.getPrenume());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Programari(
                        rs.getInt("id"),
                        rs.getString("pacientNume"),
                        rs.getString("medicNume"),
                        rs.getDate("data"),
                        rs.getTime("oraBg"),
                        rs.getTime("oraSf"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista.toArray(new Programari[0]);
    }

    private void updateStatus(int id, String status) {

        String sql = "UPDATE programari SET status=? WHERE id=?";

        try (Connection con = DBConnection.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}