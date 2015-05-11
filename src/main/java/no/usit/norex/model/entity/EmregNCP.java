package no.usit.norex.model.entity;

public class EmregNCP {

    private String acronym;
    private String countryCode;
    private String url;
    private String pubKey;
    private String[] institutions;


    public EmregNCP(
            String acronym,
            String countryCode,
            String url,
            String pubKey,
            String[] institutions) {
        this.acronym = acronym;
        this.countryCode = countryCode;
        this.url = url;
        this.pubKey = pubKey;
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
