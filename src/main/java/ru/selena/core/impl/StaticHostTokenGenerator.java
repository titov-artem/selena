package ru.selena.core.impl;

import org.springframework.beans.factory.annotation.Required;
import ru.selena.core.HostTokenGenerator;
import ru.selena.utils.collections.ArrayUtils;

import java.util.List;

/**
 * Date: 3/5/13
 * Time: 1:02 PM
 *
 * @author Artem Titov
 */
public class StaticHostTokenGenerator implements HostTokenGenerator {

    private byte[] token;

    @Required
    public void setToken(final String hexadecimalToken) {
        this.token = ArrayUtils.toByteArray(hexadecimalToken);
    }

    @Override
    public byte[] generateToken(final List<byte[]> currentClusterTokens) {
        return token;
    }
}
