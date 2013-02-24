package ru.selena;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.CoreService;

import javax.servlet.http.HttpServlet;

/**
 * Date: 12/20/12
 * Time: 11:43 PM
 *
 * @author Artem Titov
 */
public final class HttpServerInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(HttpServerInitializer.class);

    private Server server;

    private HttpServlet clientServlet;
    private HttpServlet internalServlet;
    private CoreService coreService;

    @Required
    public void setClientServlet(final HttpServlet clientServlet) {
        this.clientServlet = clientServlet;
    }

    @Required
    public void setInternalServlet(final HttpServlet internalServlet) {
        this.internalServlet = internalServlet;
    }

    @Required
    public void setCoreService(final CoreService coreService) {
        this.coreService = coreService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing server");
        server = new Server();
        server.setConnectors(createConnectors());
        server.setHandler(createHandler());
    }

    private Handler createHandler() {
        final ServletHandler servletHandler = new ServletHandler();
        servletHandler.setServlets(new ServletHolder[]{
                new ServletHolder("client", clientServlet),
                new ServletHolder("internal", internalServlet)
        });
        servletHandler.setServletMappings(new ServletMapping[]{
                mappingFor("client", "/get"),
                mappingFor("client", "/put"),
                mappingFor("client", "/delete"),
                mappingFor("internal", "/internal/get"),
                mappingFor("internal", "/internal/put"),
                mappingFor("internal", "/internal/delete"),
        });
        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        servletContextHandler.setServletHandler(servletHandler);
        final HandlerCollection handlerCollection = new HandlerList();
        handlerCollection.setHandlers(new Handler[] {
                servletContextHandler,
                new DefaultHandler()
        });
        return handlerCollection;
    }

    private ServletMapping mappingFor(final String servletName, final String path) {
        final ServletMapping mapping = new ServletMapping();
        mapping.setServletName(servletName);
        mapping.setPathSpec(path);
        return mapping;
    }

    private Connector[] createConnectors() {
        final Connector connector = new SelectChannelConnector();
        final int port = coreService.getCurrentHost().getPort();
        connector.setPort(port);
        log.info("Set up connector on port " + port);
        return new Connector[]{connector};
    }

    public void start() throws Exception {
        log.info("Starting server...");
        server.start();
        log.info("Server started");
    }

    public void stop() throws Exception {
        log.info("Stopping server...");
        server.stop();
        log.info("Server stopped");
    }
}
