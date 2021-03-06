package eu.emrex.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WSUtil {

    public static void postData(HttpURLConnection conn, String[] paramName, String[] paramVal) throws Exception {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

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

    }


    // public static String
    // httpPost(String urlStr, String[] paramName, String[] paramVal, EmrexController nc) throws Exception {
    //
    // URL url = new URL(urlStr);
    // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // conn.setRequestMethod("POST");
    // conn.setDoOutput(true);
    // conn.setDoInput(true);
    // conn.setUseCaches(false);
    // conn.setAllowUserInteraction(false);
    // conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    //
    // String encodedCredential = new String(
    // Base64Coder.encode((nc.getWsBrukernavn() + ":" + nc.getWsPassord())
    // .getBytes()));
    // conn.setRequestProperty("Authorization", "BASIC " + encodedCredential);
    //
    // // Create the form content
    // OutputStream out = conn.getOutputStream();
    // Writer writer = new OutputStreamWriter(out, "UTF-8");
    // for (int i = 0; i < paramName.length; i++) {
    // writer.write(paramName[i]);
    // writer.write("=");
    // writer.write(urlEncode(paramVal[i]));
    // writer.write("&");
    // }
    // writer.close();
    // out.close();
    // int errorCode = conn.getResponseCode();
    // if (errorCode != HttpURLConnection.HTTP_OK) {
    // BufferedReader rd = new BufferedReader(new InputStreamReader(
    // conn.getErrorStream()));
    // StringBuilder sb = new StringBuilder();
    // String line;
    // while ((line = rd.readLine()) != null) {
    // sb.append(line);
    // }
    // rd.close();
    //
    // String msg = sb.toString();
    // msg = msg.substring(msg.indexOf("ORA-"), msg.indexOf("</h1>")).replaceAll("&quot;", "\"");
    //
    // throw new WebserviceConnectionException(msg, errorCode);
    // }
    //
    // // Buffer the result into a string
    // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    // StringBuilder sb = new StringBuilder();
    // String line;
    // while ((line = rd.readLine()) != null) {
    // sb.append(line);
    // }
    // rd.close();
    //
    // conn.disconnect();
    // return sb.toString();
    // }
    //
    //
    // public static String
    // httpGet(String urlStr, EmrexController nc) throws Exception {
    //
    // URL url = new URL(urlStr);
    // System.out.println("Kaller WS: " + urlStr);
    //
    // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    // conn.setRequestMethod("GET");
    // conn.setDoOutput(true);
    // conn.setDoInput(true);
    // conn.setUseCaches(false);
    // conn.setAllowUserInteraction(false);
    // conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    //
    // String encodedCredential = new String(
    // Base64Coder.encode((nc.getWsBrukernavn() + ":" + nc.getWsPassord())
    // .getBytes()));
    // conn.setRequestProperty("Authorization", "BASIC " + encodedCredential);
    //
    // int errorCode = conn.getResponseCode();
    // if (errorCode != HttpURLConnection.HTTP_OK) {
    // throw new WebserviceConnectionException(conn.getResponseMessage(), errorCode);
    // }
    //
    // // Buffer the result into a string
    // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    // StringBuilder sb = new StringBuilder();
    // String line;
    // while ((line = rd.readLine()) != null) {
    // sb.append(line);
    // }
    // rd.close();
    //
    // conn.disconnect();
    // return sb.toString();
    // }
    //
    //
    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static HttpURLConnection setupConnection(String wsurl, String method) throws MalformedURLException,
            IOException,
            ProtocolException {
        URL url = new URL(wsurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }


    public static String getDataFromConnection(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getInputStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        return writer.toString();
    }


    public static <T> T getJsonObjectFromRequest(HttpServletRequest request, Class<T> t) throws IOException {
        StringBuffer jb = new StringBuffer();
        String line = null;

        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
            jb.append(line);

        String inputReq = jb.toString();

        Gson gson = new GsonBuilder().create();
        return gson.fromJson(inputReq, t);
    }


    public static int levenshteinDistance(String s, String t) {
        if (s == null && t == null) {
            return 0;
        }
        if (s == null || s.length() == 0) {
            return t.length();
        }
        if (t == null || t.length() == 0) {
            return s.length();
        }

        int[] v0 = new int[t.length() + 1];
        int[] v1 = new int[t.length() + 1];

        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0

            // first element of v1 is A[i+1][0]
            // edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < t.length(); j++) {
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }
        }

        return v1[t.length()];
    }

}
