package ru.selena;

import com.google.common.collect.Lists;
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
import ru.selena.core.ClusterManager;
import ru.selena.core.KeyRingService;
import ru.selena.core.LocalStoreService;

import javax.servlet.http.HttpServlet;
import java.io.IOException;

/**
 * Date: 12/20/12
 * Time: 11:43 PM
 *
 * @author Artem Titov
 */
public final class HttpServerInitializer implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(HttpServerInitializer.class);
    private static final int DATA_CORRUPTED = 2;
    private static final int FAILED_TO_START_DUE_EXCEPTION = 3;
    private static final int FAILED_TO_OPEN_STORAGE_DUE_EXCEPTION = 1;
    private static final int CLUSTER_LOST = 4;

    private Server server;

    private HttpServlet clientServlet;
    private HttpServlet internalServlet;
    private ClusterManager clusterManager;
    private LocalStoreService localStoreService;
    private KeyRingService keyRingService;

    @Required
    public void setClientServlet(final HttpServlet clientServlet) {
        this.clientServlet = clientServlet;
    }

    @Required
    public void setInternalServlet(final HttpServlet internalServlet) {
        this.internalServlet = internalServlet;
    }

    @Required
    public void setClusterManager(final ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Required
    public void setLocalStoreService(final LocalStoreService localStoreService) {
        this.localStoreService = localStoreService;
    }

    @Required
    public void setKeyRingService(final KeyRingService keyRingService) {
        this.keyRingService = keyRingService;
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
        handlerCollection.setHandlers(new Handler[]{
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
        final int port = clusterManager.getCurrentHost().getPort();
        connector.setPort(port);
        log.info("Set up connector on port " + port);
        return new Connector[]{connector};
    }

    private Iterable<ClusterManager.ClusterEventListener> createClusterEventListeners() {
        return Lists.newArrayList(
                new ClusterManager.ClusterEventListener() {
                    @Override
                    public void onCLusterEvent(final ClusterManager.ClusterEvent event) {
                        if (event.getType() == ClusterManager.ClusterEventType.CLUSTER_CHANGED) {
                            log.info("Updating cluster information");
                            keyRingService.updateRing(
                                    event.getClusterManager().getAvailableHosts()
                            );
                        }
                    }
                },

                new ClusterManager.ClusterEventListener() {
                    @Override
                    public void onCLusterEvent(final ClusterManager.ClusterEvent event) {
                        if (event.getType() == ClusterManager.ClusterEventType.CONNECTION_LOST) {
                            stopServer();
                            closeStorage();
                            log.error("*** Lost connection to cluster! Halting. ***");
                            System.exit(CLUSTER_LOST);
                        }
                    }
                }
        );
    }

    public void start() throws Exception {
        clusterManager.setClusterEventListeners(createClusterEventListeners());
        try {
            openStorage();
            log.info("Joining cluster...");
            clusterManager.joinCluster();
            log.info("Done");
            log.info("Starting server...");
            server.start();
            log.info("Server started");
        } catch (Exception e) {
            log.error("*** Failed to start selena due to exception! Halting ***", e);
            closeStorage();
            System.exit(FAILED_TO_START_DUE_EXCEPTION);
        }
    }

    public void stop() throws Exception {
        log.info("Leaving cluster");
        clusterManager.leaveCluster();
        stopServer();
        closeStorage();
    }

    private void stopServer() {
        log.info("Stopping server...");
        try {
            server.stop();
            log.info("Server stopped");
        } catch (Exception e) {
            log.error("Exception while stopping server", e);
        }
    }

    private void openStorage() {
        log.info("Opening storage...");
        try {
            LocalStoreService.StorageStatus storageStatus = localStoreService.open();
            if (storageStatus == LocalStoreService.StorageStatus.CORRUPTED) {
                storageStatus = localStoreService.restore();
                if (storageStatus == LocalStoreService.StorageStatus.CORRUPTED) {
                    log.error("*** Failed to open storage, data is corrupted! Halting. ***");
                    System.exit(DATA_CORRUPTED);
                }
            }
            log.info("Storage opened");
        } catch (IOException e) {
            log.error("*** Failed to open or restore storage due to exception! Halting. ***", e);
            System.exit(FAILED_TO_OPEN_STORAGE_DUE_EXCEPTION);
        }
    }

    private void closeStorage() {
        log.info("Closing storage...");
        try {
            localStoreService.close();
            log.info("Storage closed");
        } catch (IOException e) {
            log.error("Failed to close storage. Date can be corrupted. If so restore will be executed when system will " +
                    "be started next time", e);
        }
    }
}
