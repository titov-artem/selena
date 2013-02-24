package ru.selena.net.servlet;

import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.CoordinationService;
import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.utils.SerializationUtils;
import ru.selena.util.collections.ArrayUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;

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
public class IOServlet extends AbstractIOServlet<CoordinationService> {

    @Required
    public void setStoreService(final CoordinationService storeService) {
        this.storeService = storeService;
    }

    @Override
    protected void processGet(final Key key, final HttpServletResponse response) throws IOException {
        final DataObject dataObject;
        try {
            dataObject = storeService.get(key);
        } catch (NoSuchElementException e) {
            log().warn("No any data found", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (DataStoreException e) {
            log().error("Failed to get data for key " + ArrayUtils.toHexString(key.getHash()), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        if (dataObject.isStub()) {
            log().warn("Found stub object");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final ServletOutputStream out = response.getOutputStream();
        serializeDataObject(dataObject, out);
        out.close();
    }

    @Override
    protected void processPost(final DataObject dataObject, final HttpServletResponse response) throws IOException {
        try {
            storeService.put(dataObject);
        } catch (DataStoreException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void serializeDataObject(final DataObject dataObject, final ServletOutputStream out) throws IOException {
        final byte[] buffer = new byte[SerializationUtils.getRequiredSize(dataObject)];
        SerializationUtils.serializeDataObject(dataObject, buffer, 0);
        out.write(buffer);
    }
}
