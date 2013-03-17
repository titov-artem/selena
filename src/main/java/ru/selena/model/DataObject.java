package ru.selena.model;

/**
 * Date: 12/15/12
 * Time: 4:04 PM
 *
 * @author Artem Titov
 */
public interface DataObject {

    /**
     * Return key for this object.
     *
     * @return key
     */
    Key getKey();

    /**
     * Return value for this object.
     *
     * @return not null byte array with value
     */
    byte[] getValue();

    /**
     * Return version of current object.
     *
     * @return version
     */
    Version getVersion();

    /**
     * Return is this is stub object that determine deleted data.
     *
     * @return true if it is stu of false otherwise
     */
    boolean isStub();

    /**
     * Return creation time for this version of data in ms. Creation time sets on each machine locally using there local
     * clock. So creation time can be different. It used only to remove too old stub objects.
     *
     * @return creation time
     */
    long getCreationTime();
}
