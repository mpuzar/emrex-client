package no.usit.norex;

import java.io.IOException;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.jboss.logging.Logger;
import org.jboss.solder.servlet.WebApplication;
import org.jboss.solder.servlet.event.Initialized;

@Named
@ApplicationScoped
public class Webapp {

    private String version;
    private String path;

    private static final Logger log = Logger.getLogger(Webapp.class);

    @Inject
    ServletContext servletContext;


    public void onStartup(@Observes @Initialized WebApplication webApplication) {
        Properties props = new Properties();
        try {
            props.load(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
            version = props.getProperty("Implementation-Version");
        } catch (IOException e) {
            log.errorf("IOException ved forsøk på å lese META/INF/MANIFEST.MF: %s", e.getMessage());
        }
        if (version == null) {
            version = "versjonsonsinfo utilgjengelig";
        }
        String builddate = props.getProperty("Built-On");
        if (builddate != null && !builddate.isEmpty() && !builddate.contains("maven")) {
            version += " - " + builddate;
        }
        log.infof("onStartup Norex, versjon %s", version);
    }


    public String getVersion() {
        return version;
    }

    /* Dette er til menyen, i tilfelle vi dropper JS-versjonen
     * 
    public String getPath() {
        HttpServletRequest origRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                                                                          .getRequest();
        String path = origRequest.getServletPath();
        return path;
    }


    public String getMenu() {
        String menuitems;
        menuitems = "#forside:forside";
        menuitems += "#emne:oversikt,nyttEmne,sokEmne,endreEmne,endreEmneTabs";
        menuitems += "#verdilister:verdilister";
        menuitems += "#bruker:brukerinnstillinger";

        Pattern patPath = Pattern.compile("\\/([^\\.]+)");
        Matcher matchPath = patPath.matcher(getPath());
        if (matchPath.find()) {
            String p = matchPath.group(1);
            Pattern patAll = Pattern.compile("#([^:]+):([^#]+)");
            Matcher match = patAll.matcher(menuitems);
            while (match.find()) {
                if (match.group(2).contains(p)) {
                    return match.group(1);
                }
            }
        }

        return null;
    }
    */
}
