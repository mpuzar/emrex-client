package no.usit.norex.db;

public class IngenDBException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public IngenDBException() { 
        super();
    }
    public IngenDBException(String msg) {
        super(msg);
    }
    public IngenDBException(String msg, Exception exc) {
        super(msg, exc);
    }

}
