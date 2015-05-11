package no.usit.norex.exception;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.NonexistentConversationException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.inject.Inject;

import no.usit.norex.db.IngenDBException;

import org.jboss.seam.security.Identity;
import org.jboss.solder.logging.Logger;

/**
 * <p>
 * Handler for various Exceptions which might arise.
 * </p>
 * 
 * <p>
 * Begynnelsen var snippa mer eller mindre direkte fra en Ed Burns blog-post. Men etter hvert har mer kommet til, for
 * ConversationException og IkkeValgtDBException.
 * 
 * @author leivhe
 * @see http://weblogs.java.net/blog/edburns/archive/2009/09/03/dealing-gracefully-viewexpiredexception-jsf2
 * 
 */
public class ApplicationExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler wrapped;
    private static final Logger log = Logger.getLogger(ApplicationExceptionHandler.class);
    @Inject
    private Identity identity;


    public ApplicationExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }


    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }


    @Override
    public void handle() throws FacesException {
        // log.debug("handle()");
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = getInnermostCause(context.getException());
            log.debug("handle caught " + t);
            FacesContext fc = getFc();
            NavigationHandler nav = getNav();
            Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();

            boolean removeException = true;
            boolean tooLateForRedirect = fc.getExternalContext().isResponseCommitted();
            try {

                if (t instanceof ViewExpiredException) {
                    ViewExpiredException vee = (ViewExpiredException) t;

                    // Start Handle viewExpired
                    if (vee.getViewId().contains("velgInstitusjon")) {
                        log.debug("handle() velgInstitusjon request");
                        nav.handleNavigation(fc, null, "viewExpired");
                    }

                    // Push some useful stuff to the request scope for use in the page
                    else {
                        log.debug("handle() else block");
                        sessionExpired(t, fc, nav, vee, requestMap);
                    }
                    // End Handle viewExpired

                } else if (t instanceof IngenDBException) {
                    log.debug("handle ignore IkkeValgtDBException");
                    if (tooLateForRedirect) {
                        continue;
                    }
                    nav.handleNavigation(fc, null, "viewExpired");

                } else if (t instanceof NonexistentConversationException) {
                    log.debug("handle ignore NonexistentConversationException");
                    if (tooLateForRedirect) {
                        continue;
                    }

                    boolean loggedIn = identity != null && identity.isLoggedIn();
                    if (loggedIn) {
                        nav.handleNavigation(fc, null, "startPage");
                    } else {
                        nav.handleNavigation(fc, null, "viewExpired");
                    }

                } else {
                    log.warnf(t, "handle not prepared to deal with %s", t);

                    nav.handleNavigation(fc, null, "error");
                    removeException = false;
                }
            } finally {
                if (removeException) {
                    i.remove();
                }
            }

        }
        // At this point, the queue will not contain any exceptions we care about
        // Therefore, let the parent handle them.
        getWrapped().handle();

    }


    private NavigationHandler getNav() {
        return getFc().getApplication().getNavigationHandler();
    }


    private FacesContext getFc() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc;
    }


    private void sessionExpired(Throwable t,
            FacesContext fc,
            NavigationHandler nav,
            ViewExpiredException vee,
            Map<String, Object> requestMap) {
        for (Entry<String, Object> entry : requestMap.entrySet()) {
            log.debugf("requestmap has key %s value %s", entry.getKey(), entry.getValue());
        }
        log.debugf("handle() found request for %s", vee.getViewId());
        requestMap.put("currentViewId", vee.getViewId());

        nav.handleNavigation(fc, null, "viewExpired");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "The session expired!", t.getMessage());
        fc.addMessage(null, msg);
        fc.renderResponse();
    }


    private Throwable getInnermostCause(Throwable t) {
        if (t.getCause() == null) {
            return t;
        } else {
            return getInnermostCause(t.getCause());
        }
    }

}
