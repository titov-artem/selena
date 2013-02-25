package ru.selena.core.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.selena.TestModelFactories;
import ru.selena.core.CoreService;
import ru.selena.core.KeyRingService;
import ru.selena.core.LocalStoreService;
import ru.selena.core.RemoteStoreService;
import ru.selena.core.exception.DataStoreException;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.model.impl.DataObjectImpl;
import ru.selena.model.impl.IntegerHasKey;
import ru.selena.model.impl.LongVersion;
import ru.selena.net.model.Host;
import ru.selena.net.model.HostWithIntegerToken;
import ru.selena.util.NumberUtils;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Date: 12/17/12
 * Time: 10:31 PM
 *
 * @author Artem Titov
 */
public class CoordinationServiceImplTest {

    private CoordinationServiceImpl coordinationService;
    private KeyRingService keyRingService;
    private CoreService coreService;
    private Key key;
    private DataObject dataObject1;
    private DataObject dataObject2;
    private DataObject dataObject3;

    private HostWithIntegerToken host1;
    private HostWithIntegerToken host2;
    private HostWithIntegerToken host3;

    {
        key = TestModelFactories.createKey(new byte[]{1, 2, 3, 4});
        dataObject1 = TestModelFactories.createDataObject(key, TestModelFactories.createVersion(NumberUtils.toByteArray(2l)), new byte[1]);
        dataObject2 = TestModelFactories.createDataObject(key, TestModelFactories.createVersion(NumberUtils.toByteArray(2l)), new byte[2]);
        dataObject3 = TestModelFactories.createDataObject(key, TestModelFactories.createVersion(NumberUtils.toByteArray(3l)), new byte[3]);
        host1 = new HostWithIntegerToken("localhost", 8080, 2);
        host2 = new HostWithIntegerToken("localhost", 8081, 4);
        host3 = new HostWithIntegerToken("localhost", 8082, 6);

        coreService = mock(CoreService.class);
        when(coreService.getCurrentHost()).thenReturn(host1);

        keyRingService = mock(KeyRingService.class);
        when(keyRingService.getPreferredHosts(key)).thenReturn(Arrays.<Host>asList(
                host1,
                host2,
                host3
        ));
    }

    @Before
    public void setUp() throws Exception {
        final CoordinationServiceImpl coordinationService = new CoordinationServiceImpl();

        coordinationService.setKeyRingService(keyRingService);
        coordinationService.setCoreService(coreService);

        coordinationService.setReadCount(2);
        coordinationService.setWriteCount(2);

        this.coordinationService = coordinationService;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGet() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        when(localStoreService.get(key)).thenReturn(dataObject1);

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenReturn(dataObject3);
        when(remoteStoreService.get(key, host3)).thenReturn(dataObject3);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        final DataObject dataObject = coordinationService.get(key);
        Thread.sleep(1000);

        assertEquals("Wrong result", dataObject3, dataObject);
        verify(localStoreService).put(dataObject3);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetWhenNoElement() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        when(localStoreService.get(key)).thenThrow(new NoSuchElementException());

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenThrow(new NoSuchElementException());
        when(remoteStoreService.get(key, host3)).thenThrow(new NoSuchElementException());

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        coordinationService.get(key);
    }

    @Test(expected = DataStoreException.class)
    public void testGetWhenAllResponseTimeout() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        final Answer<DataObject> answer = new Answer<DataObject>() {
            @Override
            public DataObject answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return dataObject1;
            }
        };
        when(localStoreService.get(key)).thenAnswer(answer);

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenAnswer(answer);
        when(remoteStoreService.get(key, host3)).thenAnswer(answer);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        coordinationService.get(key);
    }

    @Test(expected = DataStoreException.class)
    public void testGetWhenMajorPartResponseTimeout() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        final Answer<DataObject> answer = new Answer<DataObject>() {
            @Override
            public DataObject answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return dataObject1;
            }
        };
        when(localStoreService.get(key)).thenReturn(dataObject1);

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenAnswer(answer);
        when(remoteStoreService.get(key, host3)).thenAnswer(answer);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        coordinationService.get(key);
    }

    @Test
    public void testGetWhenMajorResponseNoSuchElement() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        final Answer<DataObject> answer = new Answer<DataObject>() {
            @Override
            public DataObject answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return dataObject1;
            }
        };
        when(localStoreService.get(key)).thenAnswer(answer);

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenThrow(new NoSuchElementException());
        when(remoteStoreService.get(key, host3)).thenThrow(new NoSuchElementException());

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        try {
            coordinationService.get(key);
            fail("No NoSuchElementException was thrown");
        } catch (NoSuchElementException ignore) {
        }
        Thread.sleep(2000);
        verify(remoteStoreService).put(dataObject1, host2);
        verify(remoteStoreService).put(dataObject1, host3);
    }

    @Test
    public void testGetWhenOneDataStoreException() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        when(localStoreService.get(key)).thenThrow(new DataStoreException("Test"));

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenReturn(dataObject1);
        when(remoteStoreService.get(key, host3)).thenReturn(dataObject1);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        coordinationService.get(key);
        Thread.sleep(1000);
        verify(localStoreService).put(dataObject1);
    }

    @Test
    public void testGetWhenMajorDataStoreException() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        when(localStoreService.get(key)).thenThrow(new DataStoreException("Test"));

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenThrow(new DataStoreException("Test"));
        when(remoteStoreService.get(key, host3)).thenReturn(dataObject1);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        try {
            coordinationService.get(key);
            fail("No DataStoreException was thrown");
        } catch (DataStoreException ignore) {
        }
        Thread.sleep(1000);
        verify(localStoreService).put(dataObject1);
        verify(remoteStoreService).put(dataObject1, host2);
    }

    @Test
    public void testGetWhenMajorDataStoreExceptionAndOnRepairOneFail() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);
        when(localStoreService.get(key)).thenThrow(new DataStoreException("Test"));

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);
        when(remoteStoreService.get(key, host2)).thenAnswer(new Answer<DataObject>() {
            @Override
            public DataObject answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(50);
                throw new DataStoreException("Test");
            }
        });
        when(remoteStoreService.get(key, host3)).thenReturn(dataObject1);
        doThrow(new DataStoreException("Test")).when(localStoreService).put(dataObject1);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        try {
            coordinationService.get(key);
            fail("No DataStoreException was thrown");
        } catch (DataStoreException ignore) {
        }
        Thread.sleep(1000);
        verify(localStoreService).put(dataObject1);
        verify(remoteStoreService).put(dataObject1, host2);
    }

    @Test
    public void testPut() throws Exception {
        LocalStoreService localStoreService = mock(LocalStoreService.class);

        RemoteStoreService remoteStoreService = mock(RemoteStoreService.class);

        coordinationService.setLocalStoreService(localStoreService);
        coordinationService.setRemoteStoreService(remoteStoreService);
        coordinationService.afterPropertiesSet();

        coordinationService.put(dataObject1);
        Thread.sleep(1000);

        verify(localStoreService).put(dataObject1);
        verify(remoteStoreService).put(dataObject1, host2);
        verify(remoteStoreService).put(dataObject1, host3);
        verifyNoMoreInteractions(localStoreService);
        verifyNoMoreInteractions(remoteStoreService);
    }
}
