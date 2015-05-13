package no.usit.norex.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import no.usit.norex.WebserviceConnectionException;
import no.usit.norex.controller.NorexController;

public class WSUtil {

    public static String
            httpPost(String urlStr, String[] paramName, String[] paramVal, NorexController nc) throws Exception {

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String encodedCredential = new String(
                                              Base64Coder.encode((nc.getWsBrukernavn() + ":" + nc.getWsPassord()).getBytes()));
        conn.setRequestProperty("Authorization", "BASIC " + encodedCredential);

        // Create the form content
        OutputStream out = conn.getOutputStream();
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        for (int i = 0; i < paramName.length; i++) {
            writer.write(paramName[i]);
            writer.write("=");
            writer.write(urlEncode(paramVal[i]));
            writer.write("&");
        }
        writer.close();
        out.close();
        int errorCode = conn.getResponseCode();
        if (errorCode != HttpURLConnection.HTTP_OK) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                                                                         conn.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            String msg = sb.toString();
            msg = msg.substring(msg.indexOf("ORA-"), msg.indexOf("</h1>")).replaceAll("&quot;", "\"");

            throw new WebserviceConnectionException(msg, errorCode);
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }


    public static String
            httpGet(String urlStr, NorexController nc) throws Exception {

        URL url = new URL(urlStr);
        System.out.println("Kaller WS: " + urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String encodedCredential = new String(
                                              Base64Coder.encode((nc.getWsBrukernavn() + ":" + nc.getWsPassord()).getBytes()));
        conn.setRequestProperty("Authorization", "BASIC " + encodedCredential);

        int errorCode = conn.getResponseCode();
        if (errorCode != HttpURLConnection.HTTP_OK) {
            throw new WebserviceConnectionException(conn.getResponseMessage(), errorCode);
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }


    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
