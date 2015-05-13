package eu.emrex.client.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

import eu.emrex.client.session.Bruker;

public class SessionTenantConnectionProvider implements ConnectionProvider {

    private static final long serialVersionUID = 1L;

    private static final String BASE_JNDI_NAME = "/jdbc/epn";

    private final Logger log = Logger.getLogger(SessionTenantConnectionProvider.class);

    Bruker bruker;


    public void configure(Properties props) {
        log.info("configure()");
        for (Object key : props.keySet()) {
            log.info("configure key: " + key + ", value: " + props.get(key));
        }
    }


    public Connection getConnection() throws SQLException {
        // log.info("getConnection(), for bruker " + bruker);

        String dbnavn = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("inst");

        final String tenantDataSourceName = BASE_JNDI_NAME + "/" + dbnavn;
        DataSource tenantDataSource = getDSFromJNDI(tenantDataSourceName);
        return tenantDataSource.getConnection();
    }


    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }


    public boolean supportsAggressiveRelease() {
        // so long as the tenant identifier remains available in TL throughout, we can
        return true;
    }


    public void close() throws HibernateException {
        // currently nothing to do here
    }


    private DataSource getDSFromJNDI(String DSName) {
        // log.info("getDSFromJNDI prøver slå opp : " + DSName);
        try {
            Context initCtx = new InitialContext();
            return (DataSource) initCtx.lookup(DSName);
        } catch (Exception ne) {
            throw new IngenDBException("fant ikke datakilde for " + DSName, ne);
        }
    }


    @Override
    public boolean isUnwrappableAs(@SuppressWarnings("rawtypes") Class unwrapType) {
        return ConnectionProvider.class.equals(unwrapType) || DataSource.class.isAssignableFrom(unwrapType);
        // || OracleDriver.class.isAssignableFrom(unwrapType)

    }


    @Override
    @SuppressWarnings({ "unchecked" })
    public <T> T unwrap(Class<T> unwrapType) {
        if (ConnectionProvider.class.equals(unwrapType)) { // ||
                                                           // OracleDriver.class.isAssignableFrom(unwrapType)
            return (T) this;
        } else if (DataSource.class.isAssignableFrom(unwrapType)) {
            return (T) this;
        } else {
            throw new UnknownUnwrapTypeException(unwrapType);
        }
    }
}