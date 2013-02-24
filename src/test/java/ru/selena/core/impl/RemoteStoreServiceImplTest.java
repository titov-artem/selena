package ru.selena.core.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.selena.HttpServerInitializer;
import ru.selena.core.CoreService;
import ru.selena.core.LocalStoreService;
import ru.selena.core.exception.DataStoreException;
import ru.selena.core.exception.UpdatingOlderVersionException;
import ru.selena.model.DataObject;
import ru.selena.model.impl.DataObjectImpl;
import ru.selena.model.impl.IntegerHasKey;
import ru.selena.model.impl.LongVersion;
import ru.selena.net.impl.HttpTransportService;
import ru.selena.net.model.HostWithIntegerToken;
import ru.selena.net.servlet.IOServlet;
import ru.selena.net.servlet.InternalIOServlet;

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

    private HostWithIntegerToken host = new HostWithIntegerToken("localhost", 8080, 1);

    private LocalStoreService localStoreService;
    private CoreService coreService = mock(CoreService.class);

    {
        when(coreService.getCurrentHost()).thenReturn(host);
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
        serverInitializer.setCoreService(coreService);
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
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
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
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
        );
        when(localStoreService.get(key)).thenReturn(value);
        final DataObject returnedValue = remoteStoreService.get(key, host);
        assertEquals(value, returnedValue);
        verify(localStoreService, times(1)).get(key);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testGetNotFound() throws Exception {
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
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
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
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
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                new byte[]{1, 2, 3, 4}
        );
        remoteStoreService.put(value, host);
        verify(localStoreService, times(1)).put(value);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testPutStub() throws Exception {
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
        );
        remoteStoreService.put(value, host);
        verify(localStoreService, times(1)).put(value);
        verifyNoMoreInteractions(localStoreService);
    }

    @Test
    public void testPutConflict() throws Exception {
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
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
        final IntegerHasKey key = new IntegerHasKey(new byte[]{1, 2, 3, 4});
        final DataObjectImpl value = new DataObjectImpl(
                key,
                new LongVersion(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
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
