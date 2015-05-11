package no.usit.norex.session;

import javax.faces.context.FacesContext;

import org.jboss.logging.Logger;

//@Named("log")
//@SessionScoped
//@Startup
public class NorexLogger {

    private Logger log;


    public NorexLogger(Class<?> cl) {
        log = Logger.getLogger(cl);
        if (log == null) {
            System.out.println("ERROR - COULD NOT GET LOGGER for " + cl);
        }
    }


    public void info(String s) {
        log.info(getInst() + s);
    }


    public void warn(String s) {
        log.warn(getInst() + s);
    }


    public void debug(String s) {
        log.debug(getInst() + s);
    }


    public void error(String s) {
        log.error(getInst() + s);
    }


    public void trace(String s) {
        log.trace(getInst() + s);
    }


    public void infof(String s, Object... vars) {
        log.infof(getInst() + s, vars);
    }


    public void warnf(String s, Object... vars) {
        log.warnf(getInst() + s, vars);
    }


    public void debugf(String s, Object... vars) {
        log.debugf(getInst() + s, vars);
    }


    public void errorf(String s, Object... vars) {
        log.errorf(getInst() + s, vars);
    }


    public void tracef(String s, Object... vars) {
        log.tracef(getInst() + s, vars);
    }


    private String getInst() {
        if (FacesContext.getCurrentInstance() != null) {
            String inst = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("inst");
            String brukernavn = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                                                     .get("brukernavn");
            return "[" + inst + ": " + brukernavn + "] ";
        } else {
            return "";
        }
    }
}
