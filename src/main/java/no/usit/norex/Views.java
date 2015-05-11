package no.usit.norex;

import org.jboss.seam.faces.event.PhaseIdType;
import org.jboss.seam.faces.rewrite.FacesRedirect;
import org.jboss.seam.faces.security.LoginView;
import org.jboss.seam.faces.security.RestrictAtPhase;
import org.jboss.seam.faces.view.config.ViewConfig;
import org.jboss.seam.faces.view.config.ViewPattern;
import org.jboss.seam.security.annotations.LoggedIn;

/**
 * Views i applikasjonen som krever spesialbehandling.
 * 
 * De fleste har krav om at bruker er pålogget.
 * 
 * Andre krav kan legges til, se dokumentasjon til Seam3 Faces og Security.
 * 
 * @author leivhe
 * 
 * @See http://docs.jboss.org/seam/3/faces/latest/reference/en-US/html/viewconfig.html
 */
@ViewConfig
public interface Views {

    static enum Pages {

        @ViewPattern("/index.xhtml")
        @RestrictAtPhase(PhaseIdType.RESTORE_VIEW)
        @LoggedIn
        @FacesRedirect
        @LoginView("/login.xhtml?faces-redirect=true")
        INDEX,

        @ViewPattern("/import*")
        @RestrictAtPhase(PhaseIdType.RESTORE_VIEW)
        @LoggedIn
        @FacesRedirect
        @LoginView("/login.xhtml?faces-redirect=true")
        IMPORT,

        @ViewPattern("/export*")
        @RestrictAtPhase(PhaseIdType.RESTORE_VIEW)
        @LoggedIn
        @FacesRedirect
        @LoginView("/login.xhtml?faces-redirect=true")
        EXPORT;

    }

}
