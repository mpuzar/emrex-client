package eu.emrex.client.model.entity;

public class EmregNCP {

    private String acronym;
    private String countryCode;
    private String url;
    private String pubKey;
    private String[] institutions;


    public EmregNCP(
            Object acronym,
            Object countryCode,
            Object url,
            Object pubKey,
            String[] institutions) {
        this.acronym = acronym.toString();
        this.countryCode = countryCode.toString();
        this.url = url.toString();
        this.pubKey = pubKey.toString();
        this.institutions = institutions;
    }


    public String getAcronym() {
        return acronym;
    }


    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }


    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public String getPubKey() {
        return pubKey;
    }


    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }


    public String[] getInstitutions() {
        return institutions;
    }


    public void setInstitutions(String[] institutions) {
        this.institutions = institutions;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
