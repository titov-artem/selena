package ru.selena.model.impl;

import org.apache.commons.lang.Validate;
import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.model.Version;

/**
 * Date: 12/17/12
 * Time: 1:01 AM
 *
 * @author Artem Titov
 */
public class DataObjectImpl implements DataObject {

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private final Key key;
    private final Version version;
    private final byte[] value;
    private final boolean isStub;
    private final long creationTime;

    /**
     * Create data object.
     *
     * @param key     key
     * @param version version
     * @param value   value
     */
    public DataObjectImpl(final Key key, final Version version, final byte[] value) {
        this(key, version, false, value);
    }

    /**
     * Create stub data object.
     *
     * @param key     key
     * @param version version
     */
    public DataObjectImpl(final Key key, final Version version) {
        this(key, version, true, EMPTY_ARRAY);
    }

    private DataObjectImpl(final Key key, final Version version, final boolean isStub, final byte[] value) {
        Validate.isTrue(key != null, "Key can't be null");
        Validate.isTrue(version != null, "Version can't be null");
        Validate.isTrue(value != null, "Value can't be null");

        this.key = key;
        this.version = version;
        this.value = value;
        this.isStub = isStub;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean isStub() {
        return isStub;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + key.hashCode();
        result += 31 * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DataObjectImpl)) {
            return false;
        }
        final DataObjectImpl d = (DataObjectImpl) obj;
        return key.equals(d.key) && version.equals(d.version);
    }

    @Override
    public String toString() {
        return String.format("%s (v. %s)", key.toString(), version.toString());
    }
}
