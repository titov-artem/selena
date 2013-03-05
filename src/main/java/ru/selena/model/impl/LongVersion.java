package ru.selena.model.impl;

import ru.selena.model.Version;
import ru.selena.utils.NumberUtils;

/**
 * Date: 12/17/12
 * Time: 1:02 AM
 *
 * @author Artem Titov
 */
public class LongVersion implements Version {

    private final long version;

    LongVersion(final byte[] version) {
        this.version = NumberUtils.toLong(version);
    }

    @Override
    public boolean isConflict(final Version version) {
        validateVersion(version);
        return false;
    }

    @Override
    public boolean isBefore(final Version version) {
        final LongVersion v = validateVersion(version);
        return this.version < v.version;
    }

    @Override
    public boolean isAfter(Version version) {
        final LongVersion v = validateVersion(version);
        return this.version > v.version;
    }

    @Override
    public byte[] getRawVersion() {
        return NumberUtils.toByteArray(version);
    }

    private LongVersion validateVersion(final Version version) {
        if (version == null) {
            throw new IllegalArgumentException("Version can't be null");
        }
        if (!(version instanceof LongVersion)) {
            throw new IllegalArgumentException(
                    String.format("Incompatible version implementations: %s and %s",
                            this.getClass().getCanonicalName(),
                            version.getClass().getCanonicalName()));
        }
        return (LongVersion) version;
    }

    @Override
    public int hashCode() {
        return new Long(version).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LongVersion)) {
            return false;
        }
        final LongVersion v = (LongVersion) obj;
        return version == v.version;
    }

    @Override
    public String toString() {
        return String.valueOf(version);
    }
}
