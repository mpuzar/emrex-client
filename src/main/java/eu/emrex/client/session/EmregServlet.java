package no.usit.norex.session;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import no.usit.norex.model.entity.EmregCountry;
import no.usit.norex.model.entity.EmregNCP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Stateless
@Path("/emreg")
public class EmregServlet {

    private final NorexLogger log = new NorexLogger(EmregServlet.class);


    @GET
    public Response all() {
        EmregCountry[] countries = getCountries();
        EmregNCP[] ncps = getNCPs();

        JSONArray jsonNCPs = new JSONArray(ncps);
        JSONArray jsonCountries = new JSONArray(countries);

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("countries", jsonCountries);
            jsonObj.put("ncps", jsonNCPs);

            String jsonStr = jsonObj.toString();

            log.info("JSON: " + jsonStr + ", antall ncps=" + ncps.length);

            return Response.ok(jsonStr).build();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            return Response.serverError().build();
        }

    }


    private EmregCountry[] getCountries() {
        EmregCountry[] ret = new EmregCountry[4];
        EmregCountry c;

        c = new EmregCountry("FI", "Finland", false);
        ret[0] = c;
        c = new EmregCountry("SE", "Sweden", false);
        ret[1] = c;
        c = new EmregCountry("NO", "Norway", false);
        ret[2] = c;
        c = new EmregCountry("IT", "Italy", true);
        ret[3] = c;

        return ret;
    }


    private EmregNCP[] getNCPs() {
        EmregNCP[] ret = new EmregNCP[5];
        EmregNCP n;
        String[] insts;

        String keyFI = "-----BEGIN CERTIFICATE-----\n"
                       + "MIIB+TCCAWICCQDiZILVgSkjojANBgkqhkiG9w0BAQUFADBBMQswCQYDVQQGEwJG\n"
                       + "STERMA8GA1UECAwISGVsc2lua2kxETAPBgNVBAcMCEhlbHNpbmtpMQwwCgYDVQQK\n"
                       + "DANDU0MwHhcNMTUwMjA1MTEwNTI5WhcNMTgwNTIwMTEwNTI5WjBBMQswCQYDVQQG\n"
                       + "EwJGSTERMA8GA1UECAwISGVsc2lua2kxETAPBgNVBAcMCEhlbHNpbmtpMQwwCgYD2\n"
                       + "VQQKDANDU0MwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMyVVTyGT1Cp8z1f\n"
                       + "jYEO93HEtIpFKnb/tvPb6Ee5b8m8lnuv6YWsF8DBWPVfsOq0KCWD8zE1yD+w+xxM\n"
                       + "mp6+zATp089PUrEUYawG/tGu9OG+EX+nhOAj0SBvGHEkXh6lGJgeGxbdFVwZePAN\n"
                       + "135ra5L3gYcwYBVOuEyYFZJp7diHAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAP2E9\n"
                       + "YD7djCum5UYn1Od9Z1w55j+SuKRWMnTR3yzy1PXJjb2dGqNcV9tEhdbqWbwTnNfl\n"
                       + "6sidCnd1U0p4XdLjg28me8ZmfftH+QU4LkwSFSyF4ajoTFC3QHD0xTtGpQIT/rAD\n"
                       + "x/59fhfX5icydMzzNulwXJWImtXq2/AX43/yR+M=\n"
                       + "-----END CERTIFICATE-----";

        String keySE = "-----BEGIN CERTIFICATE-----\n"
                       + "MIIB+zCCAWQCCQDFl6qyaXVcDTANBgkqhkiG9w0BAQUFADBCMQswCQYDVQQGEwJT\n"
                       + "RTETMBEGA1UECAwKU29tZS1TdGF0ZTEOMAwGA1UEBwwFVW1lw6UxDjAMBgNVBAoM\n"
                       + "BUxBRE9LMB4XDTE1MDIwNTEzNDAwOVoXDTE4MDUyMDEzNDAwOVowQjELMAkGA1UE\n"
                       + "BhMCU0UxEzARBgNVBAgMClNvbWUtU3RhdGUxDjAMBgNVBAcMBVVtZcOlMQ4wDAYD\n"
                       + "VQQKDAVMQURPSzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAt2hMqdBWvKJ7\n"
                       + "rFWVxboxXnh8KO1Wx01+4eJ7/CK9UsJSiaCtNtozdSP1Go1MMv1TItrHXIYRKodQ\n"
                       + "DRxL16C1zobOC8fvfvL++JKiPYPM+iuH2njPEbO/SALEs03w8n1fN7r24gMHs73g\n"
                       + "YmLPj2bWHR6UcRwNSstY1dyN1syInXUCAwEAATANBgkqhkiG9w0BAQUFAAOBgQCV\n"
                       + "u+S6+9w2X2kTVTJNurlCjy6PqBvBpUfxs0Mg67pdiD8UvU77QwgCPd3NhlPP6T4f\n"
                       + "0vAG44KgOqGbIexckuHfUHe4aoaO8arUXy0Jx1pT+tFqppjq3yo3XQD68YpyfcCv\n"
                       + "BWleZe3B2F8/p0MbcdJEJUuSyDSMm5EX2QShTwtK+g==\n"
                       + "-----END CERTIFICATE-----";

        String keyNO = "-----BEGIN CERTIFICATE-----\n"
                       + "MIIB9TCCAV4CCQDzcL/uks2ttjANBgkqhkiG9w0BAQUFADA/MQswCQYDVQQGEwJO\n"
                       + "TzETMBEGA1UECAwKU29tZS1TdGF0ZTENMAsGA1UEBwwET3NsbzEMMAoGA1UECgwD\n"
                       + "VWlPMB4XDTE1MDIwNTEzNDE1MloXDTE4MDUyMDEzNDE1MlowPzELMAkGA1UEBhMC\n"
                       + "Tk8xEzARBgNVBAgMClNvbWUtU3RhdGUxDTALBgNVBAcMBE9zbG8xDDAKBgNVBAoM\n"
                       + "A1VpTzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAsnI3V1JKOl6Ghgzu5AOS\n"
                       + "sUFcJfefKP3y4lkVzW18T8AXkLKjNWQxJpW7h7kqeEYpcijAIV/xRMu8vEebroad\n"
                       + "h9joOOD+ePL95VnAxcMxy3SdrGxRJ5RZwNf1l5UBxm416acIR78frlFX2kKz8kz0\n"
                       + "gmOmqFFey3zZm7ZdiKw9fTsCAwEAATANBgkqhkiG9w0BAQUFAAOBgQBn6Mlnlecl\n"
                       + "AG/BVF4irljHag7OOopxTlQzb1V/mR0kYtsr6PZzCTJdwwU5YYWPG2hdqYRzyydl\n"
                       + "EGid5OPeCVtNQNQG8yKDCYeCS571jSbQ7Q0n0IR4mv7SeLq537mVBaaftV3Hdnyo\n"
                       + "SLcXQpDep/akUOa8TpPwVg/+2U7TWNGJeQ==\n"
                       + "-----END CERTIFICATE-----";

        String keyIT_UNIVR = "-----BEGIN CERTIFICATE-----"
                             + "MIICEzCCAXwCCQC35e0UlS2R2zANBgkqhkiG9w0BAQUFADBOMQswCQYDVQQGEwJJ"
                             + "VDEPMA0GA1UECAwGVmVyb25hMQ8wDQYDVQQHDAZWZXJvbmExHTAbBgNVBAoMFFVu"
                             + "aXZlcnNpdHkgb2YgVmVyb25hMB4XDTE1MDQyMzEzMDE1NloXDTE4MDgwNTEzMDE1"
                             + "NlowTjELMAkGA1UEBhMCSVQxDzANBgNVBAgMBlZlcm9uYTEPMA0GA1UEBwwGVmVy"
                             + "b25hMR0wGwYDVQQKDBRVbml2ZXJzaXR5IG9mIFZlcm9uYTCBnzANBgkqhkiG9w0B"
                             + "AQEFAAOBjQAwgYkCgYEAs+dWsOpoLYqk1D+MW3nZNLwDwnYCqzaA/k5QsEioVSQt"
                             + "lBQp7FpZiIJurm4o0J/7Gznk6PwWYlmX95+K41rK5qzW1bE+jkz5EJq6ySYfS4Ma"
                             + "6blQRNYh9N1L2UOYJYpomI/EYUsbe3AX6ZkWrQsRMRoQZ7wdFVt5Ig6YkpNkTD0C"
                             + "AwEAATANBgkqhkiG9w0BAQUFAAOBgQBihUBJgdXSMJovHfzT8WmJhpFFSYQ/fbXi"
                             + "kST0SMjknhjggGw6of1AAToH4K3ebcj5m0mn9EVxDvOULUqX2GrSfUTdKRHf10Sv"
                             + "lqS+HcnJgVGEOlrw+jSLl0M/9mkRoU1NfksmU3rq+Y9TmQT0RQb+M9mEbrQyhG0V"
                             + "6/2/6yq69Q=="
                             + "-----END CERTIFICATE-----";

        String keyIT_UNISI = "-----BEGIN CERTIFICATE-----"
                             + "MIICDTCCAXYCCQCRPCNbwk9oojANBgkqhkiG9w0BAQUFADBLMQswCQYDVQQGEwJJ"
                             + "VDEOMAwGA1UECAwFU2llbmExDjAMBgNVBAcMBVNpZW5hMRwwGgYDVQQKDBNVbml2"
                             + "ZXJzaXR5IG9mIFNpZW5hMB4XDTE1MDQyMzEzMDI0OVoXDTE4MDgwNTEzMDI0OVow"
                             + "SzELMAkGA1UEBhMCSVQxDjAMBgNVBAgMBVNpZW5hMQ4wDAYDVQQHDAVTaWVuYTEc"
                             + "MBoGA1UECgwTVW5pdmVyc2l0eSBvZiBTaWVuYTCBnzANBgkqhkiG9w0BAQEFAAOB"
                             + "jQAwgYkCgYEAxC5NIlVT5lbI+cOl5ylPIOkTYJbsiC6usomUdqM0XcH18jjwH3BW"
                             + "6FOdRBdfxfZ+oNqBP3OSmiQnUm15lQsPQbImrL7uJ0aSvK91/n7K/GZHcvWsJGkO"
                             + "9kvk/iuKs5LWTJKnLOqdlylL1tUVUCCiup/nxUkkuCRSc8KE9YedMOsCAwEAATAN"
                             + "BgkqhkiG9w0BAQUFAAOBgQBXsqG/0GGOkwbZf6obemdFRs47CZ6Nt2JoFED8ffSV"
                             + "FiiOMkXkymWR5gI0WuM5FbzSlU5NH/sVzUfQLWKcGae2A6WJ2l0PoS7E1WS6E6S0"
                             + "kDvtND37wldgYt93P8RPGahKkpcVUfYc6UipJA4HQNc8j8hK3kckqQOd8ew/9Eaz"
                             + "PQ=="
                             + "-----END CERTIFICATE-----";

        insts = new String[] { "Aalto-yliopisto", "Arcada", "Helsingin yliopisto" };
        n = new EmregNCP("CSC", "FI", "http://virtawstesti.csc.fi/norex/", keyFI, insts);
        ret[0] = n;

        insts = new String[] { "Umeå", "KTH" };
        n = new EmregNCP("SEK", "SE", "http://www.aftonbladet.se/", keySE, insts);
        ret[1] = n;

        insts = new String[] { "Universitetet i Oslo", "NTNU" };
        n = new EmregNCP("FSAT", "NO", "http://fsweb.no/ncp/", keyNO, insts);
        ret[2] = n;

        insts = new String[] { "University of Verona" };
        n = new EmregNCP("UNIVR", "IT", "http://www.univr.it/ncp/", keyIT_UNIVR, insts);
        ret[3] = n;

        insts = new String[] { "University of Siena" };
        n = new EmregNCP("UNISI", "IT", "http://www.unisi.it/ncp/", keyIT_UNISI, insts);
        ret[3] = n;

        return ret;
    }
}
