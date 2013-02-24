package ru.selena.model.impl;

import ru.selena.model.Key;
import ru.selena.util.NumberUtils;
import ru.selena.util.collections.ArrayUtils;

import java.util.Arrays;

/**
 * Date: 12/17/12
 * Time: 12:49 AM
 *
 * @author Artem Titov
 */
public class IntegerHasKey implements Key {

    private final byte[] value;
    private final int calculatedHashCode;

    public IntegerHasKey(final byte[] value) {
        this.value = value;
        this.calculatedHashCode = calculateHashCode();
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public byte[] getHash() {
        return NumberUtils.toByteArray(calculatedHashCode);
    }

    @Override
    public int hashCode() {
        return calculatedHashCode;
    }

    private int calculateHashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IntegerHasKey)) {
            return false;
        }
        final IntegerHasKey k = (IntegerHasKey) obj;
        return Arrays.equals(value, k.value);
    }

    @Override
    public String toString() {
        return calculateToString();
    }

    private String calculateToString() {
        return ArrayUtils.toHexString(value);
    }
}
