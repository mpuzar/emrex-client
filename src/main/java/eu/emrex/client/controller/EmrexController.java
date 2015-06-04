package eu.emrex.client.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import eu.emrex.client.model.entity.EmregCountry;
import eu.emrex.client.model.entity.EmregNCP;
import eu.emrex.client.model.entity.VerificationRequest;
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
    private String returnUrl;
    private String smpUrl;

    private ArrayList<EmregCountry> countries;
    private ArrayList<EmregNCP> ncps;

    private EmregCountry chosenCountry = null;
    private EmregNCP chosenNCP = null;

    private String verificationResult = "";


    @PostConstruct
    public void init() {
        smpUrl = System.getProperty("emrex.smp_url");
        // setResultaterXml(readFile());
        getDataFromSMP();

        String returnURL = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
            .get("returnURL");
        if (returnURL != null && !returnURL.equals("")) {
            setReturnURL(returnURL);
            log.info("returnURL: " + returnURL);
        } else {
            getReturnURL();
        }
    }


    public void getDataFromSMP() {
        HttpURLConnection conn = null;
        String json = null;
        try {
            conn = WSUtil.setupConnection(smpUrl + "/emreg/list", "GET");
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


    public void verifyXmlSignature() {
        HttpURLConnection conn = null;
        try {
            conn = WSUtil.setupConnection(smpUrl + "/data/verify", "POST");
            VerificationRequest req = new VerificationRequest();

            req.setPubKey(getChosenNCP().getPubKey());
            req.setSessionId("burek");
            req.setData(getResultaterXml());
            req.setBirthDate(bruker.getBirthDate());
            req.setGender(bruker.getGender());
            req.setFamilyName(bruker.getFamilyName());
            req.setGivenNames(bruker.getFirstName());

            Gson gson = new GsonBuilder().create();
            String reqJson = gson.toJson(req);

            log.info("Verifying request: " + reqJson);
            OutputStream out = conn.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(reqJson);
            writer.close();
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String json = WSUtil.getDataFromConnection(conn);
                log.info("Verified XML: " + json);
                setVerificationResult(json);
            }
        } catch (Exception t) {
            log.error("Failed to verify XML signature.");
        }
    }


    public void checkCountry() {
        log.info("Chosen country: " + chosenCountry.getCountryCode());
    }


    public String getResultaterXml() {
        return resultaterXml;
    }


    public void setResultaterXml(String resultaterXml) {
        this.resultaterXml = resultaterXml;
        log.infof("setResultaterXml, %d bytes", (resultaterXml == null) ? 0 : resultaterXml.length());
    }


    // public void sendToCountry() throws IOException {
    //
    // if (returnURL.indexOf("jboss-utv") != -1) {
    // returnURL = returnURL.replaceAll("http:", "https:");
    // }
    //
    // String outURL = chosenNCP.getUrl() + "?returnURL=" + returnURL;
    //
    // log.infof("Redirigerer til %s", outURL);
    //
    // FacesContext.getCurrentInstance().getExternalContext()
    // .redirect(outURL);
    // }
    //
    //
    public String getReturnURL() {
        if (returnUrl == null || "".equals(returnUrl)) {
            String ret = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
                .getRequestURL()
                .toString();
            if (!ret.contains("localhost")) {
                ret = ret.replaceAll("http:", "https:");
            }
            ret = ret.replaceAll("\\/[^\\/]+$", "/ncpdata");
            setReturnURL(ret);
        }
        return returnUrl;
    }


    public void setReturnURL(String returnURL) {
        this.returnUrl = returnURL;
    }


    public EmregCountry getChosenCountry() {
        return chosenCountry;
    }


    public void setChosenCountry(EmregCountry chosenCountry) {
        this.chosenCountry = chosenCountry;
        if (chosenCountry != null && chosenCountry.getSingleFetch() == false) {
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
        log.info("chosenCountry.getSingleFetch(): " + chosenCountry.getSingleFetch());
        log.info("chosenNCP: " + chosenNCP);
        if (chosenCountry.getSingleFetch() == false)
            return true;
        if (chosenNCP != null)
            return true;
        return false;
    }


    public void tomResultater() {
        setChosenCountry(null);
        setResultaterXml(null);
    }


    public String getVerificationResult() {
        if (verificationResult == null) {
            return "";
        }
        return verificationResult;
    }


    public String getVerificationResultFormatted() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(getVerificationResult().trim());
        String json = gson.toJson(je);
        return json;
    }


    public void setVerificationResult(String verificationResult) {
        this.verificationResult = verificationResult;
    }
}
