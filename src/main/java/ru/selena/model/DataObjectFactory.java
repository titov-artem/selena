package ru.selena.model;

import ru.selena.model.DataObject;
import ru.selena.model.Key;
import ru.selena.model.Version;

/**
 * Date: 12/18/12
 * Time: 11:45 PM
 *
 * @author Artem Titov
 */
public interface DataObjectFactory {

    /**
     * Create data object.
     *
     * @param key     key
     * @param version version
     * @param value   value
     * @return data object
     */
    DataObject createDataObject(final Key key, final Version version, final byte[] value);

    /**
     * Create stub data object.
     *
     * @param key     key
     * @param version version
     * @return stub data object
     */
    DataObject createDataObject(final Key key, final Version version);
}
