package ru.selena.util.collections;

import org.junit.Test;
import ru.selena.util.NumberUtils;

import java.util.Arrays;

/**
 * Date: 12/18/12
 * Time: 9:12 AM
 *
 * @author Artem Titov
 */
public class ArrayUtilsTest {
    @Test
    public void testToHexString() throws Exception {
        final int n = 255;
        final byte[] bytes = NumberUtils.toByteArray(n);
        System.out.println(Integer.toHexString(n));
        System.out.println(ArrayUtils.toHexString(bytes));
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void testToByteArray() throws Exception {
        System.out.println(Arrays.toString(ArrayUtils.toByteArray("000000ff")));
    }
}
