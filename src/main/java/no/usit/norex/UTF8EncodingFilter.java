package no.usit.norex;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException; 
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Setter encoding av request og respons til UTF-8.
 * 
 * Primefaces 3.X endrer håndtering av Request, slik at vår default
 * (org.apache.catalina.connector.USE_BODY_ENCODING_FOR_QUERY_STRING)
 * ikke slår til. Å sette -Dfile.encoding hjelper heller ikke.
 * 
 * @author leivhe
 *
 */
public class UTF8EncodingFilter implements Filter {

    public void init(FilterConfig config) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    public void destroy() { }

}