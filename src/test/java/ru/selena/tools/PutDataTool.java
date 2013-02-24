package ru.selena.tools;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.selena.core.impl.RemoteStoreServiceImpl;
import ru.selena.model.DataObject;
import ru.selena.model.factory.DataObjectFactory;
import ru.selena.model.factory.KeyFactory;
import ru.selena.model.factory.VersionFactory;
import ru.selena.model.factory.impl.DataObjectFactoryImpl;
import ru.selena.model.factory.impl.IntegerHashKeyFactory;
import ru.selena.model.factory.impl.LongVersionFactory;
import ru.selena.net.impl.HttpTransportService;
import ru.selena.net.model.Host;
import ru.selena.net.model.HostWithIntegerToken;

import java.util.Arrays;
import java.util.List;

/**
 * Date: 12/20/12
 * Time: 12:13 AM
 *
 * @author Artem Titov
 */
@Ignore
@RunWith(Parameterized.class)
public class PutDataTool {

    private static final DataObjectFactory DATA_OBJECT_FACTORY = new DataObjectFactoryImpl();
    private static final KeyFactory KEY_FACTORY = new IntegerHashKeyFactory();
    private static final VersionFactory VERSION_FACTORY = new LongVersionFactory();

    private final Host host;
    private final DataObject dataObject;

    private static final RemoteStoreServiceImpl REMOTE_STORE_SERVICE;

    static {
        REMOTE_STORE_SERVICE = new RemoteStoreServiceImpl();
        REMOTE_STORE_SERVICE.setTransportService(new HttpTransportService());
    }

    @Parameterized.Parameters
    public static List<Object[]> getData() {
        return Arrays.asList(new Object[][]{
                {new HostWithIntegerToken("localhost", 8080, 1),
                        DATA_OBJECT_FACTORY.createDataObject(
                                KEY_FACTORY.createKey(new byte[]{(byte) 0x12}),
                                VERSION_FACTORY.createVersion(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}),
                                new byte[(byte) 0x01]
                        ),},
                {new HostWithIntegerToken("localhost", 8081, 1),
                        DATA_OBJECT_FACTORY.createDataObject(
                                KEY_FACTORY.createKey(new byte[]{(byte) 0x12}),
                                VERSION_FACTORY.createVersion(new byte[]{0, 0, 0, 0, 0, 0, 0, 2}),
                                new byte[(byte) 0x01]
                        ),},
                {new HostWithIntegerToken("localhost", 8082, 1),
                        DATA_OBJECT_FACTORY.createDataObject(
                                KEY_FACTORY.createKey(new byte[]{(byte) 0x12}),
                                VERSION_FACTORY.createVersion(new byte[]{0, 0, 0, 0, 0, 0, 0, 3}),
                                new byte[(byte) 0x01]
                        ),}
        });
    }

    public PutDataTool(final Host host, final DataObject dataObject) {
        this.host = host;
        this.dataObject = dataObject;
    }

    @Test
    public void put() throws Exception {
        REMOTE_STORE_SERVICE.put(dataObject, host);
    }
}
