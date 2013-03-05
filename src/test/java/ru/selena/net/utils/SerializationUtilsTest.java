package ru.selena.net.utils;

import org.junit.Test;
import ru.selena.TestModelFactories;
import ru.selena.model.DataObject;
import ru.selena.util.NumberUtils;
import ru.selena.util.collections.ArrayUtils;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 12/18/12
 * Time: 10:20 PM
 *
 * @author Artem Titov
 */
public class SerializationUtilsTest {

    @Test
    public void testSerializeDataObject() throws Exception {
        final byte[] key = new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8};
        final byte[] version = new byte[] {(byte) 10, (byte) 20, (byte) 30, (byte) 40, (byte) 50, (byte) 60, (byte) 70, (byte) 80};
        final byte[] value = new byte[] {(byte) -10, (byte) -20, (byte) -30, (byte) -40, (byte) -50, (byte) -60, (byte) -70, (byte) -80};
        final DataObject data = TestModelFactories.createDataObject(TestModelFactories.createKey(key), TestModelFactories.createVersion(version), value);
        final byte[] buffer = new byte[SerializationUtils.getRequiredSize(data)];
        SerializationUtils.serializeDataObject(data, buffer, 0);
        System.out.println(ArrayUtils.toHexString(key));
        System.out.println(ArrayUtils.toHexString(version));
        System.out.println(ArrayUtils.toHexString(value));
        System.out.println(ArrayUtils.toHexString(NumberUtils.toByteArray(8)));
        System.out.println(ArrayUtils.toHexString(buffer));
        final DataObject deserializedData = SerializationUtils.deserializeDataObject(buffer, 0);
        assertEquals(data, deserializedData);
    }
}
