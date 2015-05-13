package eu.emrex.client.login;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.security.BaseAuthenticator;

import eu.emrex.client.session.Bruker;
import eu.emrex.client.session.EmrexLogger;

@Named
public class Authenticator extends BaseAuthenticator {

    @Inject
    Bruker bruker;
    @PersistenceContext
    EntityManager em;

    private final EmrexLogger log = new EmrexLogger(Authenticator.class);


    @Override
    public void authenticate() {
        if (bruker == null) {
            setStatus(AuthenticationStatus.FAILURE);
            setUser(null);
            return;
        }
        setStatus(AuthenticationStatus.SUCCESS);
        setUser(bruker);
        log.infof("authenticate Logged in %s", bruker.getBrukernavn());

    }


    @Override
    public void postAuthenticate() {
    }

    private String getApplicationUri() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String viewId = facesContext.getViewRoot().getViewId();
        String url = viewId + "?faces-redirect=true&includeViewParams=true";
        return url;
    }

}
