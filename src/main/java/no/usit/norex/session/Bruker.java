package no.usit.norex.session;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import no.ntnu.it.fw.saml2api.EduPerson;

import org.picketlink.idm.api.User;

/**
 * @author leivhe
 * 
 *         Denne klassen representerer den brukeren en bruker av webappen er logget inn som. Hver bruker vil knyttes til
 *         en institusjon og få et sett med roller
 */

@Named("bruker")
@SessionScoped
@Startup
public class Bruker implements User, Serializable {

    private static final long serialVersionUID = 1L;
    private final NorexLogger log = new NorexLogger(Bruker.class);

    private String inst = null;
    private String country = null;

    private String brukernavn = "";
    private String fodselsnummer;
    private List<String> roles; /* Current roles based on your own preference */
    private List<String> allRoles; /* Roles as found in EPNAUTORISERTBRUKER */

    private EduPerson eduPerson;
    private String feideSessionIndex;
    private String nameID;
    private String fulltnavn;


    @PostConstruct
    public void init() {
    }


    public String getFodselsnummer() {
        return fodselsnummer;
    }


    public void setFodselsnummer(String fodselsnummer) {
        this.fodselsnummer = fodselsnummer;
    }


    public String getBrukernavn() {
        return brukernavn;
    }


    public void setBrukernavn(String brukernavn) {
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("brukernavn", brukernavn);
        }
        this.brukernavn = brukernavn;
    }


    @Override
    public String getKey() {
        return brukernavn;
    }


    @Override
    public String getId() {
        return brukernavn;
    }


    public List<String> getAllRoles() {
        return allRoles;
    }


    public void setAllRoles(List<String> allRoles) {
        this.allRoles = allRoles;
    }


    public EduPerson getEduPerson() {
        return eduPerson;
    }


    public void setEduPerson(EduPerson eduPerson) {
        this.eduPerson = eduPerson;

    }


    public void setFeideSessionIndex(String sessionIndex) {
        this.feideSessionIndex = sessionIndex;
    }


    public String getFeideSessionIndex() {
        return feideSessionIndex;
    }


    public boolean isFeidepalogget() {
        return feideSessionIndex != null;
    }


    public void setNameId(String nameID) {
        this.nameID = nameID;
    }


    public String getNameID() {
        return nameID;
    }


    public String getFulltnavn() {
        return fulltnavn;
    }


    public void setFulltnavn(String fulltnavn) {
        this.fulltnavn = fulltnavn;
    }


    public String getInst() {
        return inst;
    }


    public void setInst(String inst) {
        this.inst = inst;
        log.infof("setInst() %s", inst);
    }


    public String getCountry() {
        return country;
    }


    public void setCountry(String country) {
        this.country = country;
    }

}