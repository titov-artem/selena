package ru.selena.net.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.model.Version;
import ru.selena.util.NumberUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static ru.selena.model.Factories.Instances.*;

/**
 * Date: 12/18/12
 * Time: 12:41 PM
 *
 * @author Artem Titov
 */
public final class SerializationUtils {
    private static final Logger log = LoggerFactory.getLogger(SerializationUtils.class);

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final int INT_BYTE_SIZE = Integer.SIZE / Byte.SIZE;

    public static DataObject deserializeDataObject(final byte[] buffer, final int offset) {
        return deserializeDataObject(buffer, new AtomicInteger(offset));
    }

    /**
     * Deserialize data object from buffer starting from offset. After operation offset will be moved to the next
     * element after last read one.
     *
     * @param buffer buffer
     * @param offset offset
     * @return deserialized data object
     * @throws RuntimeException if something going wrong
     */
    public static DataObject deserializeDataObject(final byte[] buffer, final AtomicInteger offset) {
        log.debug("Deserializing object");
        final Key key = deserializeKey(buffer, offset);
        final Version version = deserializeVersion(buffer, offset);
        final boolean isStub = buffer[offset.getAndIncrement()] == 1;
        if (isStub) {
            return getDataObjectFactory().createDataObject(key, version);
        } else {
            return getDataObjectFactory().createDataObject(key, version, deserializeMeasuredArray(buffer, offset));
        }
    }

    public static Key deserializeKey(final byte[] buffer, final int offset) {
        return deserializeKey(buffer, new AtomicInteger(offset));
    }

    /**
     * Deserialize key object from buffer starting from offset. After operation offset will be moved to the next element
     * after last read one.
     *
     * @param buffer buffer
     * @param offset offset
     * @return deserialized key
     * @throws RuntimeException if something going wrong
     */
    public static Key deserializeKey(final byte[] buffer, final AtomicInteger offset) {
        log.debug("Deserializing key");
        final byte[] value = deserializeMeasuredArray(buffer, offset);
        return getKeyFactory().createKey(value);
    }

    public static Version deserializeVersion(final byte[] buffer, final int offset) {
        return deserializeVersion(buffer, new AtomicInteger(offset));
    }

    /**
     * Deserialize version object from buffer starting from offset. After operation offset will be moved to the next
     * element after last read one.
     *
     * @param buffer buffer
     * @param offset offset
     * @return deserialized version
     * @throws RuntimeException if something going wrong
     */
    public static Version deserializeVersion(final byte[] buffer, final AtomicInteger offset) {
        log.debug("Deserializing version");
        final byte[] value = deserializeMeasuredArray(buffer, offset);
        return getVersionFactory().createVersion(value);
    }

    public static void serializeDataObject(final DataObject dataObject, final byte[] buffer, final int offset) {
        log.debug("Serializing object");
        final int keyRequiredSize = getRequiredSize(dataObject.getKey());
        final int versionRequiredSize = getRequiredSize(dataObject.getVersion());
        final AtomicInteger currentOffset = new AtomicInteger(offset);

        serializeKey(dataObject.getKey(), buffer, currentOffset.getAndAdd(keyRequiredSize));
        serializeVersion(dataObject.getVersion(), buffer, currentOffset.getAndAdd(versionRequiredSize));

        buffer[currentOffset.getAndIncrement()] =
                dataObject.isStub() ? (byte) 1 : (byte) 0;
        if (!dataObject.isStub()) {
            NumberUtils.toByteArray(dataObject.getValue().length, buffer, currentOffset.getAndAdd(INT_BYTE_SIZE));
            System.arraycopy(dataObject.getValue(), 0, buffer, currentOffset.get(), dataObject.getValue().length);
        }
    }

    public static void serializeKey(final Key key, final byte[] buffer, final int offset) {
        log.debug("Serializing key");
        NumberUtils.toByteArray(key.getValue().length, buffer, offset);
        System.arraycopy(key.getValue(), 0, buffer, offset + INT_BYTE_SIZE, key.getValue().length);
    }

    public static void serializeVersion(final Version version, final byte[] buffer, final int offset) {
        log.debug("Serializing version");
        final byte[] rawVersion = version.getRawVersion();
        NumberUtils.toByteArray(rawVersion.length, buffer, offset);
        System.arraycopy(rawVersion, 0, buffer, offset + INT_BYTE_SIZE, rawVersion.length);
    }

    public static int getRequiredSize(final DataObject dataObject) {
        return getRequiredSize(dataObject.getKey()) + getRequiredSize(dataObject.getVersion()) + 1
                + (!dataObject.isStub() ? dataObject.getValue().length + INT_BYTE_SIZE : 0);
    }

    public static int getRequiredSize(final Key key) {
        return key.getValue().length + INT_BYTE_SIZE;
    }

    public static int getRequiredSize(final Version version) {
        return version.getRawVersion().length + INT_BYTE_SIZE;
    }

    public static byte[] deserializeMeasuredArray(final byte[] buffer, final AtomicInteger offset) {
        final int length = NumberUtils.toInt(buffer, offset.get());
        final byte[] value = new byte[length];
        System.arraycopy(buffer, offset.get() + INT_BYTE_SIZE, value, 0, length);
        offset.addAndGet(INT_BYTE_SIZE + length);
        return value;
    }
}
