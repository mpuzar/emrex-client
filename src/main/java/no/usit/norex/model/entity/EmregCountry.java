package no.usit.norex.model.entity;

public class EmregCountry {

    private String countryCode;
    private String countryName;
    private Boolean singleFetch;


    public EmregCountry(String countryCode, String countryName, Boolean singleFetch) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.singleFetch = singleFetch;
    }


    public String getCountryCode() {
        return countryCode;
    }


    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public String getCountryName() {
        return countryName;
    }


    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }


    public Boolean getSingleFetch() {
        return singleFetch;
    }


    public void setSingleFetch(Boolean singleFetch) {
        this.singleFetch = singleFetch;
    }

}
