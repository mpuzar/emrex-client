package no.usit.norex.login;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import no.ntnu.it.fw.saml2api.EduPerson;
import no.ntnu.it.fw.saml2api.SAML2Exception;
import no.ntnu.it.fw.saml2api.SAML2Util;
import no.ntnu.it.fw.saml2api.exthiggins.SAMLLogoutResponse;
import no.usit.norex.session.Bruker;
import no.usit.norex.session.NorexLogger;

import org.eclipse.higgins.saml2idp.saml2.SAMLAssertion;
import org.eclipse.higgins.saml2idp.saml2.SAMLConstants;
import org.eclipse.higgins.saml2idp.saml2.SAMLResponse;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.security.IdentityImpl;

@Named
public class FeideLogin {

    @Inject
    private FeideController controller;
    @Inject
    private Messages messages;
    @Inject
    private Bruker bruker;
    @Inject
    private IdentityImpl identity;

    private EduPerson eduPerson;

    @PersistenceContext
    private EntityManager em;
    private final NorexLogger log = new NorexLogger(FeideLogin.class);


    public void logout() throws SAML2Exception, IOException {
        log.info("logout");

        String samlResponseString = ((HttpServletRequest) FacesContext
                                                                      .getCurrentInstance().getExternalContext()
                                                                      .getRequest())
                                                                                    .getParameter("SAMLResponse");
        if (samlResponseString == null)
            return;
        SAMLLogoutResponse samlLogoutResponse = SAML2Util
                                                         .parseSAMLogoutResponse(samlResponseString);
        // log.info("Parsed LogoutResponse:"
        // + SAML2Util.dom2String(samlLogoutResponse.getDocument()));

        bruker.setAllRoles(null);
        bruker.setBrukernavn(null);
        bruker.setEduPerson(null);
        bruker.setFeideSessionIndex(null);
        bruker.setFodselsnummer(null);
        bruker.setFulltnavn(null);

        identity.logout();
        messages.info("Du er logget ut");

    }


    public void login() throws IOException {
        String samlResponseString = ((HttpServletRequest) FacesContext
                                                                      .getCurrentInstance().getExternalContext()
                                                                      .getRequest())
                                                                                    .getParameter("SAMLResponse");
        if (samlResponseString != null) {
            createEduPerson(samlResponseString);
        }
        if (bruker.isFeidepalogget()) {
            identity.login();
        }

        String redirectURL = "velgInstitusjon.jsf?faces-redirect=true";
        if (identity.isLoggedIn()) {
            redirectURL = "import.jsf?faces-redirect=true";
            messages.info("Feidepålogging vellykket");
        } else {
            messages.info("Feidepålogging mislyktes");
        }
        FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(redirectURL);
    }


    private void createEduPerson(String samlResponseString) {
        log.info("doPreLogin");
        try {
            SAMLResponse samlResponse = SAML2Util
                                                 .parseSAMLResponse(samlResponseString);
            if (controller.getSPConf().getWantSignedAssertions()) {
                SAML2Util.verifySignature(samlResponse, controller.getIDPConf()
                                                                  .getPublicKey());
            }
            if (!SAMLConstants.STATUSCODE_SUCCESS.equals(samlResponse
                                                                     .getStatusCodeValue())) {
                messages.warn("statuskode != suksess");
                return;
            }

            SAMLAssertion samlAssertion = samlResponse.getSAMLAssertion();
            samlAssertion.verify(controller.getIDPConf().getPublicKey());
            if (samlAssertion == null
                || samlAssertion.getIssuer().indexOf("feide.no") == -1
                || SAML2Util.parseSessionIndex(samlAssertion) == null) {
                messages.warn("noe galt med samlAssertion");
                return;
            }

            log.debugf("doPreLogin assertion dump %s", samlAssertion.dump());

            // Bruker er pålogga hos feide.
            bruker.setFeideSessionIndex(SAML2Util
                                                 .parseSessionIndex(samlAssertion));
            bruker.setNameId(samlAssertion.getSubject().getNameID());
            eduPerson = SAML2Util.createEduPerson(samlAssertion, false,
                                                  controller.getIDPConf().getFeideSplitChar());
            bruker.setBrukernavn(eduPerson.getPrincipalName());
            bruker.setEduPerson(eduPerson);
            // bruker.setFodselsnummer(finnFodselsnummer());
            // bruker.setFulltnavn(eduPerson.getDisplayName() + "!" + eduPerson.getFullname() + "!" +
            // eduPerson.getGivenName() + "!" + eduPerson.getLastname());
            bruker.setFodselsnummer("30535890168");
            bruker.setFulltnavn("Dolly Duck");
            log.debugf("doPreLogin eduperson dump\n%s", eduPerson.dump());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     */
    private String finnFodselsnummer() {
        String fnr = null;
        List<String> identitynums = eduPerson.getNationalIdentityNumbers();
        if (identitynums != null && !identitynums.isEmpty()) {
            fnr = identitynums.get(0);
        }
        return fnr;
    }

}
