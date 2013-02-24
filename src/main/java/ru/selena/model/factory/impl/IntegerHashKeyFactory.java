package ru.selena.model.factory.impl;

import ru.selena.model.Key;
import ru.selena.model.factory.KeyFactory;
import ru.selena.model.impl.IntegerHasKey;

/**
 * Date: 12/18/12
 * Time: 11:56 PM
 *
 * @author Artem Titov
 */
public class IntegerHashKeyFactory implements KeyFactory {

    @Override
    public Key createKey(final byte[] key) {
        return new IntegerHasKey(key);
    }
}
