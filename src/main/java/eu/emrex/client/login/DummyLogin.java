package eu.emrex.client.login;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.IdentityImpl;

import eu.emrex.client.session.Bruker;
import eu.emrex.client.session.EmrexLogger;

@Named
public class DummyLogin {

    @Inject
    private Bruker bruker;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Messages messages;
    @Inject
    private IdentityImpl identity;

    private final EmrexLogger log = new EmrexLogger(DummyLogin.class);


    public String logout() {
        if (bruker != null) {
            bruker.setAllRoles(null);
            bruker.setBrukernavn(null);
            bruker.setFodselsnummer(null);
            bruker.setFulltnavn(null);
            identity.logout();
        }
        return "velgInstitusjon?faces-redirect=true";
    }


    public void login() throws IOException {
        log.info("login");
        if (!bruker.getBrukernavn().equals("dollyduck")) {
            messages.info("Username not found!");
        } else {
            bruker.setFulltnavn("Dolly Duck");
            bruker.setFodselsnummer("30535890168");
            identity.login();
        }

        FacesContext.getCurrentInstance().getExternalContext().redirect("import.jsf?faces-redirect=true");
    }

}
