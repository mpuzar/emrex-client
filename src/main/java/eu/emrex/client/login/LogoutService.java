package eu.emrex.client.login;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.security.IdentityImpl;

import eu.emrex.client.session.Bruker;
import eu.emrex.client.session.EmrexLogger;

@Named
public class LogoutService {

    private final EmrexLogger log = new EmrexLogger(LogoutService.class);

    @Inject
    IdentityImpl identity;
    @Inject
    Bruker bruker;


    public String logout() throws IOException {
        log.infof("logout bruker %s", bruker.getBrukernavn());

        bruker.setAllRoles(null);
        bruker.setBrukernavn(null);

        identity.logout();
        return "login?faces-redirect=true";

    }

}
