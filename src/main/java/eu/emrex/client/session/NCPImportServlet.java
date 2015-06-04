package eu.emrex.client.session;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.emrex.client.controller.EmrexController;

@WebServlet(name = "NCPdata", urlPatterns = { "/ncpdata" })
public class NCPImportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final EmrexLogger log = new EmrexLogger(NCPImportServlet.class);

    @Inject
    private EmrexController ec;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String sess = request.getParameter("sessionId");
        log.info("sessionId: " + sess);

        String elmo = request.getParameter("elmo");
        ec.setResultaterXml(elmo);

        ec.verifyXmlSignature();

        response.sendRedirect("import.jsf");
    }

}
