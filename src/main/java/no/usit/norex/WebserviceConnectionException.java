package no.usit.norex;

public class WebserviceConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    private int responsKode;


    public WebserviceConnectionException(String message, int responsKode) {
        super(message);
        this.responsKode = responsKode;
    }


    public int getResponsKode() {
        return responsKode;
    }


    public void setErrorCode(int responsKode) {
        this.responsKode = responsKode;
    }

}
