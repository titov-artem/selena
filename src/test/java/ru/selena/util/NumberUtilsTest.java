package ru.selena.util;

import org.junit.Test;
import ru.selena.util.collections.ArrayUtils;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 12/18/12
 * Time: 8:43 AM
 *
 * @author Artem Titov
 */
public class NumberUtilsTest {

    @Test
    public void testToInt() throws Exception {
        final int n = 1368728465;
        final int r = NumberUtils.toInt(NumberUtils.toByteArray(n));
        assertEquals(n, r);
    }

    @Test
    public void testToByteArrayFromInt() throws Exception {
        final int n = 1368728465;
        final byte[] bytes = NumberUtils.toByteArray(n);
        assertEquals(Integer.toHexString(n), ArrayUtils.toHexString(bytes));
    }

    @Test
    public void testToLong() throws Exception {
        final long n = 1368728465312313123l;
        final long r = NumberUtils.toLong(NumberUtils.toByteArray(n));
        assertEquals(n, r);
    }

    @Test
    public void testToByteArrayFromLong() throws Exception {
        final long n = 1368728465312313123l;
        final byte[] bytes = NumberUtils.toByteArray(n);
        assertEquals(Long.toHexString(n), ArrayUtils.toHexString(bytes));
    }

}
