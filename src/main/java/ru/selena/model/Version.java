package ru.selena.model;

/**
 * Date: 12/15/12
 * Time: 4:00 PM
 *
 * @author Artem Titov
 */
public interface Version {

    /**
     * Tell is this version is in conflict with specified version.
     *
     * @param version other version
     * @return true if this version is in conflict with other version and false otherwise
     * @throws IllegalArgumentException if version is null or current version implementation difference from passed one
     */
    boolean isConflict(final Version version);

    /**
     * Tell is this version before specified one.
     *
     * @param version other version
     * @return true if this version is before other version and false otherwise
     * @throws IllegalArgumentException if version is null or current version implementation difference from passed one
     */
    boolean isBefore(final Version version);

    /**
     * Tell is this after specified one.
     *
     * @param version other version
     * @return true if this version is after other version and false otherwise
     * @throws IllegalArgumentException if version is null or current version implementation difference from passed one
     */
    boolean isAfter(final Version version);

    /**
     * Return byte array representing version
     *
     * @return byte array
     */
    byte[] getRawVersion();

}
