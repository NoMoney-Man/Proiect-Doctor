package Proiect;

import java.sql.Date;
import java.sql.Time;

public class Programari {

    private int id;
    private String pacientNume;
    private String medicNume;
    private Date data;
    private Time oraBg;
    private Time oraSf;
    private String status;

    public Programari(int id, String pacientNume, String medicNume,
                      Date data, Time oraBg, Time oraSf, String status) {
        this.id = id;
        this.pacientNume = pacientNume;
        this.medicNume = medicNume;
        this.data = data;
        this.oraBg = oraBg;
        this.oraSf = oraSf;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getPacientNume() {
        return pacientNume;
    }

    public String getMedicNume() {
        return medicNume;
    }

    public Date getData() {
        return data;
    }

    public Time getOraBg() {
        return oraBg;
    }

    public Time getOraSf() {
        return oraSf;
    }

    public String getStatus() {
        return status;
    }
}