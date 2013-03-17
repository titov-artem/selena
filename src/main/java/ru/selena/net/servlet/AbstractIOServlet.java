package ru.selena.net.servlet;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import ru.selena.Factories;
import ru.selena.core.StoreService;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.utils.SerializationUtils;
import ru.selena.utils.collections.ArrayUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class can be used for client request serving or for node's request serving.
 * Set {@link ru.selena.core.LocalStoreService} as store service for node's requests serving and
 * {@link ru.selena.core.CoordinationService} for client request serving
 * <p/>
 * Date: 12/17/12
 * Time: 11:25 PM
 *
 * @author Artem Titov
 */
public abstract class AbstractIOServlet<T extends StoreService> extends HttpServlet {

    protected T storeService;

    @Required
    public void setStoreService(final T storeService) {
        this.storeService = storeService;
    }

    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {
        final String hexadecimalKey = request.getParameter("key");
        log().info("Get");
        if (log().isDebugEnabled()) {
            log().debug("Key: " + hexadecimalKey);
        }
        if (hexadecimalKey == null || hexadecimalKey.isEmpty()) {
            final String message = "Key parameter not specified";
            log().warn(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        final byte[] key;
        try {
            key = ArrayUtils.toByteArray(hexadecimalKey);
        } catch (IllegalArgumentException e) {
            log().warn("Failed to parse key", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Key parameter must be hexadecimal string");
            return;
        }

        processGet(Factories.Instances.getKeyFactory().createKey(key), response);
    }

    protected abstract void processGet(final Key key, final HttpServletResponse response) throws IOException;

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws ServletException, IOException {
        final DataObject dataObject = deserializeDataObject(request.getInputStream());
        processPost(dataObject, response);
    }

    protected abstract void processPost(final DataObject dataObject, final HttpServletResponse response) throws IOException;

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    private DataObject deserializeDataObject(final ServletInputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        final byte[] source = out.toByteArray();

        return SerializationUtils.deserializeDataObject(source, 0);
    }

    protected final Logger log() {
        return LoggerFactory.getLogger(getClass());
    }
}
