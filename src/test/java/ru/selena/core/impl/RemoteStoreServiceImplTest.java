package ru.selena.core.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.selena.HttpServerInitializer;
import ru.selena.TestModelFactories;
import ru.selena.core.ClusterManager;
import ru.selena.core.LocalStoreService;
import ru.selena.core.exception.DataStoreException;
import ru.selena.core.exception.UpdatingOlderVersionException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.net.impl.HttpTransportService;
import ru.selena.net.model.Host;
import ru.selena.net.model.impl.HostWithIntegerToken;
import ru.selena.net.model.impl.HostWithIntegerTokenFactory;
import ru.selena.net.servlet.IOServlet;
import ru.selena.net.servlet.InternalIOServlet;
import ru.selena.util.NumberUtils;

import java.util.NoSuchElementException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Date: 12/23/12
 * Time: 11:54 PM
 *
 * @author Artem Titov
 */
public class RemoteStoreServiceImplTest {

    private Host host = new HostWithIntegerTokenFactory().createHost("localhost", 8080, NumberUtils.toByteArray(1));

    private LocalStoreService localStoreService;
    private ClusterManager clusterManager = mock(ClusterManager.class);

    {
        when(clusterManager.getCurrentHost()).thenReturn(host);
    }

    private RemoteStoreServiceImpl remoteStoreService = new RemoteStoreServiceImpl();
    private HttpTransportService transportService = new HttpTransportService();

    {
        remoteStoreService.setTransportService(transportService);
    }

    private InternalIOServlet internalIOServlet = new InternalIOServlet();
    private IOServlet clientIOServlet = new IOServlet();
    private HttpServerInitializer serverInitializer = new HttpServerInitializer();

    {
        serverInitializer.setClientServlet(clientIOServlet);
        serverInitializer.setInternalServlet(internalIOServlet);
        serverInitializer.setClusterManager(clusterManager);
    }

    @Before
    public void init() throws Exception {
        localStoreService = mock(LocalStoreService.class);
        internalIOServlet.setStoreService(localStoreService);
        serverInitializer.afterPropertiesSet();
        serverInitializer.start();
    }

    @After
    public void destroy() throws Exception {
        serverInitializer.stop();
    }

    @Test
    public void testGetNormal() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                new byte[]{1, 2, 3, 4}
        );
        when(localStoreService.get(key)).thenReturn(value);
        final DataObject returnedValue = remoteStoreService.get(key, host);
        assertEquals(value, returnedValue);
        verify(localStoreService, times(1)).get(key);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testGetStub() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
        );
        when(localStoreService.get(key)).thenReturn(value);
        final DataObject returnedValue = remoteStoreService.get(key, host);
        assertEquals(value, returnedValue);
        verify(localStoreService, times(1)).get(key);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testGetNotFound() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final String errorMessage = "Test";
        when(localStoreService.get(key)).thenThrow(new NoSuchElementException(errorMessage));
        try {
            remoteStoreService.get(key, host);
            assertEquals("Exception not thrown", true, false);
        } catch (NoSuchElementException e) {
            assertTrue(String.format("Error message don't ends with cause message (%s; %s)", e.getMessage(), errorMessage),
                    e.getMessage().endsWith(errorMessage));
        }
        verify(localStoreService, times(1)).get(key);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testGetDataStoreException() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final String getMessage = "Test";
        when(localStoreService.get(key)).thenThrow(new DataStoreException(getMessage));
        try {
            remoteStoreService.get(key, host);
            assertEquals("Exception not thrown", true, false);
        } catch (DataStoreException e) {
            assertTrue(String.format("Error message don't ends with cause message (%s; %s)", e.getMessage(), getMessage),
                    e.getMessage().endsWith(getMessage));
        }
        verify(localStoreService, times(1)).get(key);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testPutNormal() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                new byte[]{1, 2, 3, 4}
        );
        remoteStoreService.put(value, host);
        verify(localStoreService, times(1)).put(value);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testPutStub() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
        );
        remoteStoreService.put(value, host);
        verify(localStoreService, times(1)).put(value);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testPutConflict() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                new byte[]{1, 2, 3, 4}
        );
        final String conflictMessage = "Test";
        doThrow(new UpdatingOlderVersionException(conflictMessage)).when(localStoreService).put(value);
        try {
            remoteStoreService.put(value, host);
            assertEquals("Exception not thrown", true, false);
        } catch (UpdatingOlderVersionException e) {
            assertTrue(String.format("Error message don't ends with cause message (%s; %s)", e.getMessage(), conflictMessage),
                    e.getMessage().endsWith(conflictMessage));
        }
        verify(localStoreService, times(1)).put(value);
    }

    @Test
    public void testPutDataStoreException() throws Exception {
        final Key key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        final DataObject value = TestModelFactories.createDataObject(
                key,
                TestModelFactories.createVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                new byte[]{1, 2, 3, 4}
        );
        final String storeMessage = "Test";
        doThrow(new DataStoreException(storeMessage)).when(localStoreService).put(value);
        try {
            remoteStoreService.put(value, host);
            assertEquals("Exception not thrown", true, false);
        } catch (DataStoreException e) {
            assertTrue(String.format("Error message don't ends with cause message (%s; %s)", e.getMessage(), storeMessage),
                    e.getMessage().endsWith(storeMessage));
        }
        verify(localStoreService, times(1)).put(value);
        verifyNoMoreInteractions(localStoreService);
    }
}
