package eu.emrex.client.controller;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.emrex.client.model.entity.EmregCountry;
import eu.emrex.client.model.entity.EmregNCP;
import eu.emrex.client.session.Bruker;
import eu.emrex.client.session.EmrexLogger;
import eu.emrex.client.util.WSUtil;

@Named("nc")
@SessionScoped
public class EmrexController implements Serializable {

    private static final long serialVersionUID = 1L;
    private final EmrexLogger log = new EmrexLogger(EmrexController.class);

    public static final String KONFIGMAPPE_PROPERTY = "emrex.konfigmappe";

    @Inject
    Bruker bruker;

    private String resultaterXml = null;
    private String returnURL;

    private ArrayList<EmregCountry> countries;
    private ArrayList<EmregNCP> ncps;

    private EmregCountry chosenCountry = null;
    private EmregNCP chosenNCP = null;


    @PostConstruct
    public void init() {
        // setResultaterXml(readFile());
        getDataFromSMP();

        String returnURL = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
            .get("returnURL");
        if (returnURL != null && !returnURL.equals("")) {
            setReturnURL(returnURL);
            log.info("returnURL: " + returnURL);
        }
    }


    public void getDataFromSMP() {
        HttpURLConnection conn = null;
        String json = null;
        try {
            conn = WSUtil.setupConnection(System.getProperty("emrex.smp_url"), "GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                json = WSUtil.getDataFromConnection(conn);

                JSONObject jsonObj = new JSONObject(json);
                JSONArray jsonNCPs = jsonObj.getJSONArray("ncps");
                JSONArray jsonCountries = jsonObj.getJSONArray("countries");

                log.info("Countries: " + jsonCountries.length());
                countries = new ArrayList<EmregCountry>();
                for (int i = 0; i < jsonCountries.length(); i++) {
                    JSONObject c = (JSONObject) jsonCountries.get(i);
                    countries.add(new EmregCountry(c.get("countryCode"), c.get("countryName"), Boolean.valueOf(c.get(
                        "singleFetch").toString())));
                }

                log.info("NCPS: " + jsonNCPs);
                ncps = new ArrayList<EmregNCP>();
                for (int i = 0; i < jsonNCPs.length(); i++) {
                    if ("null".equalsIgnoreCase(jsonNCPs.get(i).toString()))
                        continue;
                    JSONObject c = (JSONObject) jsonNCPs.get(i);
                    JSONArray jsonInsts = (JSONArray) c.get("institutions");
                    String[] institutions = new String[jsonInsts.length()];
                    for (int j = 0; j < jsonInsts.length(); j++) {
                        institutions[j] = jsonInsts.get(j).toString();
                    }
                    ncps.add(new EmregNCP(c.get("acronym"), c.get("countryCode"), c.get("url"), c.get("pubKey"),
                                          institutions));
                    // log.info("Added NCP: " + ncps[i].getAcronym());
                }
            }
        } catch (IOException t) {
            log.error("Failed to get NCP list.");
        }

    }


    public void checkCountry() {
        log.info("Chosen country: " + chosenCountry.getCountryCode());
    }


    public void checkImportData() throws IOException {
        String xml = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
            .get("elmo");
        if (xml != null && !xml.equals("")) {
            log.info("Elmo detected!");
            setResultaterXml(xml);
        } else {
            // log.info("Elmo not found");
        }
    }


    // public void hentResultater() {
    // String xml = kallWebservice("https://w3utv-jb01.uio.no/fsrest/rest/elm/report/" + bruker.getFodselsnummer(),
    // false, null, null);
    // setResultaterXml(xml);
    // }
    //
    //
    // public void tomResultater() {
    // log.info("Tømmer resultater");
    // setResultaterXml(null);
    // }
    //
    //
    // public StreamedContent lastNedResultater() {
    // log.infof("lastNedResultater() %s", bruker.getInst());
    // // https://jboss-test.uio.no/fsrest/rest/elm/report/30535890168
    // String xml = getResultaterXml();
    // if (xml != null) {
    // InputStream stream;
    // try {
    // stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    // return new DefaultStreamedContent(
    // stream,
    // "text/xml",
    // "Norex_Export.xml");
    // // return new DefaultStreamedContent(
    // // stream,
    // // "text/xml",
    // // "Norex_Export_" + TekstUtil.convertTimeToISOWithSecondsFilenameSafe(null)
    // // + ".xml");
    // } catch (UnsupportedEncodingException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // return null;
    // }
    //
    //
    // public void importResults() {
    // String paramName[] = { "elmReport", "nin" };
    // String paramVal[] = { resultaterXml, bruker.getFodselsnummer() };
    // String res = kallWebservice("https://w3utv-jb01.uio.no/fsrest/rest/elm/import", true, paramName, paramVal);
    // log.info(paramVal[0]);
    // // String res = kallWebservice("http://localhost:8123", true, paramName, paramVal);
    // // + bruker.getFodselsnummer()
    // skrivBrukerTilbakemelding(res);
    // log.info(res);
    // }
    //
    //
    // private String kallWebservice(String url, boolean post, String[] paramName, String[] paramVal) {
    // log.info("Kall mot url = " + url);
    // try {
    // String resultat;
    // if (!post) {
    // resultat = WSUtil.httpGet(url, this);
    // } else {
    // resultat = WSUtil.httpPost(url, paramName, paramVal, this);
    // }
    //
    // // skrivBrukerTilbakemelding(resultat);
    // return resultat;
    //
    // } catch (WebserviceConnectionException wEx) {
    // handterWebserviceFeil("Hente data ", wEx);
    // } catch (Exception e) {
    // handterUkjentFeil(e);
    // }
    // return null;
    // }
    //
    //
    // private String genererFeilmeldingTilBruker(WebserviceConnectionException wEx) {
    // String respons = null;
    // switch (wEx.getResponsKode()) {
    // case HttpURLConnection.HTTP_NOT_FOUND:
    // respons = "No contact with webservice, please try again later.";
    // break;
    // case HttpURLConnection.HTTP_UNAUTHORIZED:
    // respons = "No access to webservice, please contact the user support.";
    // break;
    // default:
    // respons = "Error with webservice, error code = " + wEx.getResponsKode();
    // respons += ", message from the database: " + wEx.getMessage();
    // break;
    // }
    // return respons;
    // }
    //
    //
    // private void skrivBrukerTilbakemelding(String melding) {
    // FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(melding));
    // }
    //
    //
    // private void handterWebserviceFeil(String jobbnavn, WebserviceConnectionException wEx) {
    // log.errorf("%s kaster exception med responskode = %s, klokkeslett = %s, feilmelding = %s", jobbnavn,
    // wEx.getResponsKode(), TekstUtil.convertTimeToStringWithSeconds(TekstUtil.now()),
    // konverterExceptionTilStackTraceString(wEx));
    // String feilmelding = genererFeilmeldingTilBruker(wEx);
    // skrivBrukerTilbakemelding(feilmelding);
    // }
    //
    //
    // private void handterUkjentFeil(Exception e) {
    // log.error("Ukjent feil med webservice " + konverterExceptionTilStackTraceString(e));
    // }
    //
    //
    // private String konverterExceptionTilStackTraceString(Exception e) {
    // // StringWriter sw = new StringWriter();
    // // e.printStackTrace(new PrintWriter(sw));
    // // return sw.toString();
    // return e.getMessage();
    //
    // }
    //
    //
    // public String getWsBrukernavn() {
    // return wsBrukernavn;
    // }
    //
    //
    // public void setWsBrukernavn(String wsBrukernavn) {
    // this.wsBrukernavn = wsBrukernavn;
    // }
    //
    //
    // public String getWsPassord() {
    // return wsPassord;
    // }
    //
    //
    // public void setWsPassord(String wsPassord) {
    // this.wsPassord = wsPassord;
    // }

    public String getResultaterXml() {
        return resultaterXml;
    }


    public void setResultaterXml(String resultaterXml) {
        this.resultaterXml = resultaterXml;
        log.infof("setResultaterXml, %d bytes", (resultaterXml == null) ? 0 : resultaterXml.length());
    }


    // public String readFile() {
    // String fileName = System.getProperty(KONFIGMAPPE_PROPERTY) + File.separator + "elmotest.xml";
    // try {
    // @SuppressWarnings("resource")
    // BufferedReader br = new BufferedReader(new FileReader(fileName));
    // StringBuilder sb = new StringBuilder();
    // String line = br.readLine();
    //
    // while (line != null) {
    // sb.append(line);
    // sb.append("\n");
    // line = br.readLine();
    // }
    // setResultaterXml(sb.toString());
    // return sb.toString();
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    //
    // public Map<String, String> getCountryURL() {
    // return countryURL;
    // }
    //
    //
    // public void setCountryURL(Map<String, String> countryURL) {
    // this.countryURL = countryURL;
    // }
    //
    //
    public void sendToCountry() throws IOException {
        String returnURL = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
            .getRequestURL()
            .toString();

        if (returnURL.indexOf("jboss-utv") != -1) {
            returnURL = returnURL.replaceAll("http:", "https:");
        }

        String outURL = chosenNCP.getUrl() + "?returnURL=" + returnURL;

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


    public EmregCountry getChosenCountry() {
        return chosenCountry;
    }


    public void setChosenCountry(EmregCountry chosenCountry) {
        this.chosenCountry = chosenCountry;
        if (chosenCountry.getSingleFetch() == false) {
            setChosenNCP(getNcpsByCountry(chosenCountry.getCountryCode()).get(0));
        } else {
            setChosenNCP(null);
        }
    }


    public EmregNCP getChosenNCP() {
        return chosenNCP;
    }


    public void setChosenNCP(EmregNCP chosenNCP) {
        this.chosenNCP = chosenNCP;
    }


    public ArrayList<EmregCountry> getCountries() {
        return countries;
    }


    public ArrayList<EmregNCP> getNcps() {
        return ncps;
    }


    public EmregCountry getCountryByCode(String countryCode) {
        for (EmregCountry country : getCountries()) {
            if (country.getCountryCode().equals(countryCode))
                return country;
        }
        return null;
    }


    public ArrayList<EmregNCP> getNcpsByCountry(String countryCode) {
        ArrayList<EmregNCP> list = new ArrayList<EmregNCP>();
        for (EmregNCP ncp : getNcps()) {
            if (ncp != null && countryCode.equals(ncp.getCountryCode()))
                list.add(ncp);
        }
        return list;
    }


    public EmregNCP getNcpByAcronym(String value) {
        for (EmregNCP ncp : getNcps()) {
            if (ncp != null && ncp.getAcronym().equals(value))
                return ncp;
        }
        return null;
    }


    public boolean canFetch() {
        if (chosenCountry == null)
            return false;
        if (chosenCountry.getSingleFetch() == false)
            return true;
        if (chosenNCP != null)
            return true;
        return false;
    }
}
