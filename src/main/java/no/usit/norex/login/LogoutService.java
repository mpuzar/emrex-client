package no.usit.norex.login;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import no.ntnu.it.fw.saml2api.SAML2Exception;
import no.ntnu.it.fw.saml2api.SAML2Util;
import no.usit.norex.session.Bruker;
import no.usit.norex.session.NorexLogger;

import org.jboss.seam.security.IdentityImpl;

@Named
public class LogoutService {

    private final NorexLogger log = new NorexLogger(LogoutService.class);

    @Inject
    IdentityImpl identity;
    @Inject
    FeideController feideController;
    @Inject
    Bruker bruker;


    public String logout() throws SAML2Exception, IOException {
        log.infof("logout bruker %s", bruker.getBrukernavn());

        if (bruker.getEduPerson() != null) { // må logge ut fra feide først
            log.info("logout redirigerer til feide!");
            String redirectUrl = SAML2Util.createSAMLLogoutRequest(feideController.getIDPConf(),
                                                                   feideController.getSPConf(), bruker.getNameID(),
                                                                   bruker.getFeideSessionIndex(), "borte");
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectUrl);
        } else {
            bruker.setAllRoles(null);
            bruker.setBrukernavn(null);

            identity.logout();
            return "login?faces-redirect=true";

        }
        return null;
    }

}
