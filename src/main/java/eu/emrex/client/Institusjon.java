package no.usit.norex;

import java.io.Serializable;

public class Institusjon implements Serializable {

    private static final long serialVersionUID = 1L;

    private String kode;
    /**
     * Kortnavn på institusjon med korrekt bruk av store bokstaver.
     * 
     * Eks: UiO, KHiB
     */
    private String displayKode;

    /**
     * Fullt navn på institusjonen.
     * 
     * Forklarende tekst kan legges til.
     * 
     * Eks: Universitetet i Oslo - Testbase
     */
    private String navn;

    /**
     * Navn på institusjonens database.
     * 
     * Eks: FSUIO, FSKHIB
     */
    private String dbNavn;

    /**
     * Navn som kan brukes til jndi-oppslag for å hente ut Datasourcen.
     */
    private String jndiNavn;


    // ----- public methods -----
    /**
     * @return the kode
     */
    public String getKode() {
        return kode;
    }


    /**
     * @param kode
     *            the kode to set
     */
    public void setKode(String kode) {
        this.kode = kode;
    }


    /**
     * @return the displayKode
     */
    public String getDisplayKode() {
        return displayKode;
    }


    /**
     * @param displayKode
     *            the displayKode to set
     */
    public void setDisplayKode(String displayKode) {
        this.displayKode = displayKode;
    }


    /**
     * @return the navn
     */
    public String getNavn() {
        return navn;
    }


    /**
     * @param navn
     *            the navn to set
     */
    public void setNavn(String navn) {
        this.navn = navn;
    }


    /**
     * @return the dbNavn
     */
    public String getDbNavn() {
        return dbNavn;
    }


    /**
     * @param dbNavn
     *            the dbNavn to set
     */
    public void setDbNavn(String dbNavn) {
        this.dbNavn = dbNavn;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n   navn: ").append(getNavn());
        sb.append("\n   kode: ").append(getKode());
        sb.append("\n   displayKode: ").append(getDisplayKode());
        sb.append("\n   dbNavn: ").append(getDbNavn());
        sb.append("\n   jndiNavn: ").append(getJndiNavn());
        return sb.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbNavn == null) ? 0 : dbNavn.hashCode());
        result = prime * result
                 + ((displayKode == null) ? 0 : displayKode.hashCode());
        result = prime * result
                 + ((jndiNavn == null) ? 0 : jndiNavn.hashCode());
        result = prime * result + ((kode == null) ? 0 : kode.hashCode());
        result = prime * result + ((navn == null) ? 0 : navn.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Institusjon other = (Institusjon) obj;
        if (dbNavn == null) {
            if (other.dbNavn != null)
                return false;
        } else if (!dbNavn.equals(other.dbNavn))
            return false;
        if (displayKode == null) {
            if (other.displayKode != null)
                return false;
        } else if (!displayKode.equals(other.displayKode))
            return false;
        if (jndiNavn == null) {
            if (other.jndiNavn != null)
                return false;
        } else if (!jndiNavn.equals(other.jndiNavn))
            return false;
        if (kode == null) {
            if (other.kode != null)
                return false;
        } else if (!kode.equals(other.kode))
            return false;
        if (navn == null) {
            if (other.navn != null)
                return false;
        } else if (!navn.equals(other.navn))
            return false;
        return true;
    }


    public String getJndiNavn() {
        return jndiNavn;
    }


    public void setJndiNavn(String jndiNavn) {
        this.jndiNavn = jndiNavn;
    }
}
