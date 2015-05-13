package no.usit.norex.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import no.usit.norex.WebserviceConnectionException;
import no.usit.norex.session.Bruker;
import no.usit.norex.session.NorexLogger;
import no.usit.norex.util.TekstUtil;
import no.usit.norex.util.WSUtil;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named("nc")
@SessionScoped
public class NorexController implements Serializable {

    private static final long serialVersionUID = 1L;
    private final NorexLogger log = new NorexLogger(NorexController.class);

    public static final String KONFIGMAPPE_PROPERTY = "norex.konfigmappe";

    @Inject
    Bruker bruker;

    private Map<String, String> countryURL;

    private String wsBrukernavn = "resex_no";
    private String wsPassord = "Re5ExWsUsr";

    private String resultaterXml = null;
    private String returnURL;


    @PostConstruct
    public void init() {
        // setResultaterXml(readFile());
        countryURL = new HashMap<String, String>();
        countryURL.put("FI", "http://virtawstesti.csc.fi/norex/");
        // countryURL.put("FI", "http://193.166.44.20/elmo/elmo.php");
        countryURL.put("SE", "http://www.aftonbladet.se/");
        countryURL.put("HR", "http://folk.uio.no/matija/elmo.php");

        String returnURL = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                                       .get("returnURL");
        if (returnURL != null && !returnURL.equals("")) {
            setReturnURL(returnURL);
            log.info("returnURL: " + returnURL);
        }
    }


    public void checkImportData() throws IOException {
        String xml = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                                 .get("elmo");
        if (xml != null && !xml.equals("")) {
            log.info("Elmo detected!");
            setResultaterXml(xml);
            // FacesContext.getCurrentInstance().getExternalContext()
            // .redirect("/norex/import.jsf");
        } else {
            log.info("Elmo not found");
        }
    }


    public void hentResultater() {
        String xml = kallWebservice("https://w3utv-jb01.uio.no/fsrest/rest/elm/report/" + bruker.getFodselsnummer(),
                                    false, null, null);
        setResultaterXml(xml);
    }


    public void tomResultater() {
        log.info("Tømmer resultater");
        setResultaterXml(null);
    }


    public StreamedContent lastNedResultater() {
        log.infof("lastNedResultater() %s", bruker.getInst());
        // https://jboss-test.uio.no/fsrest/rest/elm/report/30535890168
        String xml = getResultaterXml();
        if (xml != null) {
            InputStream stream;
            try {
                stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
                return new DefaultStreamedContent(
                                                  stream,
                                                  "text/xml",
                                                  "Norex_Export.xml");
                // return new DefaultStreamedContent(
                // stream,
                // "text/xml",
                // "Norex_Export_" + TekstUtil.convertTimeToISOWithSecondsFilenameSafe(null)
                // + ".xml");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }


    public void importResults() {
        String paramName[] = { "elmReport", "nin" };
        String paramVal[] = { resultaterXml, bruker.getFodselsnummer() };
        String res = kallWebservice("https://w3utv-jb01.uio.no/fsrest/rest/elm/import", true, paramName, paramVal);
        log.info(paramVal[0]);
        // String res = kallWebservice("http://localhost:8123", true, paramName, paramVal);
        // + bruker.getFodselsnummer()
        skrivBrukerTilbakemelding(res);
        log.info(res);
    }


    private String kallWebservice(String url, boolean post, String[] paramName, String[] paramVal) {
        log.info("Kall mot url = " + url);
        try {
            String resultat;
            if (!post) {
                resultat = WSUtil.httpGet(url, this);
            } else {
                resultat = WSUtil.httpPost(url, paramName, paramVal, this);
            }

            // skrivBrukerTilbakemelding(resultat);
            return resultat;

        } catch (WebserviceConnectionException wEx) {
            handterWebserviceFeil("Hente data ", wEx);
        } catch (Exception e) {
            handterUkjentFeil(e);
        }
        return null;
    }


    private String genererFeilmeldingTilBruker(WebserviceConnectionException wEx) {
        String respons = null;
        switch (wEx.getResponsKode()) {
        case HttpURLConnection.HTTP_NOT_FOUND:
            respons = "No contact with webservice, please try again later.";
            break;
        case HttpURLConnection.HTTP_UNAUTHORIZED:
            respons = "No access to webservice, please contact the user support.";
            break;
        default:
            respons = "Error with webservice, error code = " + wEx.getResponsKode();
            respons += ", message from the database: " + wEx.getMessage();
            break;
        }
        return respons;
    }


    private void skrivBrukerTilbakemelding(String melding) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(melding));
    }


    private void handterWebserviceFeil(String jobbnavn, WebserviceConnectionException wEx) {
        log.errorf("%s kaster exception med responskode = %s, klokkeslett = %s, feilmelding = %s", jobbnavn,
                   wEx.getResponsKode(), TekstUtil.convertTimeToStringWithSeconds(TekstUtil.now()),
                   konverterExceptionTilStackTraceString(wEx));
        String feilmelding = genererFeilmeldingTilBruker(wEx);
        skrivBrukerTilbakemelding(feilmelding);
    }


    private void handterUkjentFeil(Exception e) {
        log.error("Ukjent feil med webservice " + konverterExceptionTilStackTraceString(e));
    }


    private String konverterExceptionTilStackTraceString(Exception e) {
        // StringWriter sw = new StringWriter();
        // e.printStackTrace(new PrintWriter(sw));
        // return sw.toString();
        return e.getMessage();

    }


    public String getWsBrukernavn() {
        return wsBrukernavn;
    }


    public void setWsBrukernavn(String wsBrukernavn) {
        this.wsBrukernavn = wsBrukernavn;
    }


    public String getWsPassord() {
        return wsPassord;
    }


    public void setWsPassord(String wsPassord) {
        this.wsPassord = wsPassord;
    }


    public String getResultaterXml() {
        return resultaterXml;
    }


    public void setResultaterXml(String resultaterXml) {
        this.resultaterXml = resultaterXml;
        log.infof("setResultaterXml, %d bytes", (resultaterXml == null) ? 0 : resultaterXml.length());
    }


    public String readFile() {
        String fileName = System.getProperty(KONFIGMAPPE_PROPERTY) + File.separator + "elmotest.xml";
        try {
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            setResultaterXml(sb.toString());
            return sb.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }


    public Map<String, String> getCountryURL() {
        return countryURL;
    }


    public void setCountryURL(Map<String, String> countryURL) {
        this.countryURL = countryURL;
    }


    public void sendToCountry() throws IOException {
        String returnURL = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRequestURL()
                                                                                                                     .toString();

        if (returnURL.indexOf("jboss-utv") != -1) {
            returnURL = returnURL.replaceAll("http:", "https:");
        }

        String outURL = getCountryURL().get(bruker.getCountry()) + "?returnURL=" + returnURL;

        log.infof("Redirigerer til %s", outURL);

        FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(outURL);
    }


    public String getReturnURL() {
        return returnURL;
    }


    public void setReturnURL(String returnURL) {
        this.returnURL = returnURL;
    }

}
