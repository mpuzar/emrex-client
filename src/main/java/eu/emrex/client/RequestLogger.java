package no.usit.norex;


import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;

import org.jboss.solder.logging.Logger;

/**
 * <p>Filter for å logge info om requester ala Apache.</p>
 * 
 * <p>Filteret legger til en linje i loggen for innkommende requests.
 * Hvis requestens uri matcher parameteret <i>excludes</i> fra web.xml,
 * blir ingenting logget.</p>
 * 
 * <p>Eksempel på web.xml-oppsett:
 * <pre>
 * {@code 
 * <filter>
    <filter-name>Logging Filter</filter-name>
    <filter-class>no.usit.soknadsweb.http.RequestLogger</filter-class>
    <init-param>
      <param-name>excludes</param-name>
      <param-value>.*css|.*gif|.*graphicImage.*|.*captcha|.*js</param-value>
    </init-param>
   </filter>
 * }</pre></p>
 * 
 * @author leivhe
 *
 */
@WebFilter(filterName = "Request Logger",
	urlPatterns = {"/*"},
	initParams={
		@WebInitParam(name="excludes",
					  value=".*css|.*gif|.*graphicImage.*|.*captcha|.*js|.*resource.*")})
public class RequestLogger implements Filter {

    private Logger log = Logger.getLogger(RequestLogger.class);
    private Pattern excludePattern = null;
    

    public void init(FilterConfig config) throws ServletException {
        log.info("init " + config);

        String excludes = config.getInitParameter("excludes");
        if (excludes != null) {
            excludePattern = Pattern.compile(excludes);
            log.info("init excludePattern " + excludes);
        }
    }
        
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest)request;
        String uri = req.getRequestURI();

        if (!excludePattern.matcher(uri).matches()) {

            String address = request.getRemoteAddr();
            int i = address.lastIndexOf(".");
            if (i > 0) address = address.substring(0,i);
            String userAgent = req.getHeader("user-agent");
            String query = "-";
            if (req.getQueryString() != null) {
                query = req.getQueryString();
            }
            
            log.info(address + " " + req.getMethod() +  " " + uri + " " + query + " " + userAgent);            
        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        log = null;
        excludePattern = null;
    }
}
