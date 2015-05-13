package no.usit.norex;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;

/**
 * <p>
 * Handler for ViewExpiredException.
 * </p>
 * 
 * Snippa mer eller mindre direkte fra en Ed Burns blog-post.
 * 
 * @author leivhe
 * @see http://weblogs.java.net/blog/edburns/archive/2009/09/03/dealing-gracefully-viewexpiredexception-jsf2
 * 
 */
public class ViewExpiredExceptionExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler wrapped;
    private static final Logger log = Logger.getLogger(ViewExpiredExceptionExceptionHandler.class);


    public ViewExpiredExceptionExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }


    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }


    @Override
    public void handle() throws FacesException {
        log.debug("handle()");
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            log.debugf("handle has throwable: %s", t.getMessage());
            if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                FacesContext fc = FacesContext.getCurrentInstance();
                Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
                HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();

                NavigationHandler nav =
                                        fc.getApplication().getNavigationHandler();

                try {

                    // TODO når vi begynner med egen ekstra login-side og feide-login
                    // kan VEE bli håndtert mer smooth:

                    // if (vee.getViewId().contains("velgInstitusjon")) {
                    // log.info("handle() velgInstitusjon request");
                    // String institusjon = request.getParameter("institusjonsvalg:institusjonsMenu");
                    // if (institusjon != null && !institusjon.isEmpty()) {
                    // try {
                    // log.debug("handle redirects to login inst " + institusjon);
                    // fc.getExternalContext().redirect("./login.jsf?inst=" + institusjon);
                    //
                    // } catch (IOException ioe) {
                    // log.debug("handle got ioexception", ioe);
                    // }
                    // }
                    // }
                    //
                    // else if (vee.getViewId().contains("login")) {
                    // log.info("handle() login request");
                    // String institusjon = request.getParameter("inst");
                    // if (institusjon != null && !institusjon.isEmpty()) {
                    // log.debug("handle try to let this one pass; inst is set");
                    // } else {
                    // log.debug("handle login no inst set");
                    // nav.handleNavigation(fc, null, "viewExpired");
                    // fc.renderResponse();
                    // }
                    // }
                    //
                    // // Push some useful stuff to the request scope for use in the page
                    // else {
                    log.debug("handle() else block");
                    for (Entry<String, Object> entry : requestMap.entrySet()) {
                        log.debugf("requestmap has key %s value %s", entry.getKey(), entry.getValue());
                    }
                    log.debugf("handle() found request for %s", vee.getViewId());
                    requestMap.put("currentViewId", vee.getViewId());

                    // FIXME: add a facesmessages saying the session expired
                    nav.handleNavigation(fc, null, "viewExpired");
                    fc.renderResponse();
                    // }
                } finally {
                    i.remove();
                }
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();

    }

}
