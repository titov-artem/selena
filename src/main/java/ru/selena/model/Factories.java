package ru.selena.model;

import java.util.Properties;

/**
 * Contains information about model objects factories
 * <p/>
 * Date: 2/25/13
 * Time: 10:42 PM
 *
 * @author Artem Titov
 */
public final class Factories {
    private Factories() {
        throw new AssertionError("This class not for instantiation");
    }

    public static final String DATA_OBJECT_FACTORY_PROP_NAME = "ru.selena.model.factory.data.object";
    public static final String KEY_FACTORY_PROP_NAME = "ru.selena.model.factory.key";
    public static final String VERSION_FACTORY_PROP_NAME = "ru.selena.model.factory.version";

    /**
     * Contains names of default implementations for model objects factories
     */
    public static final class Defaults {
        private Defaults() {
            throw new AssertionError("This class not for instantiation");
        }

        public static final String DEFAULT_DATA_OBJECT_FACTORY = "ru.selena.model.impl.DataObjectFactoryImpl";
        public static final String DEFAULT_KEY_FACTORY = "ru.selena.model.impl.IntegerHashKeyFactory";
        public static final String DEFAULT_VERSION_FACTORY = "ru.selena.model.impl.LongVersionFactory";
    }

    /**
     * Contains factories for creating models objects
     */
    public static final class Instances {
        private static DataObjectFactory dataObjectFactory;
        private static KeyFactory keyFactory;
        private static VersionFactory versionFactory;

        static {
            initFactories();
        }

        /**
         * Initialize factories for model objects. As implementation use classes specified by:
         * <ul>
         * <li>DataObject factory - {@value Factories#DATA_OBJECT_FACTORY_PROP_NAME}</li>
         * <li>Key factory - {@value Factories#KEY_FACTORY_PROP_NAME}</li>
         * <li>Version factory - {@value Factories#VERSION_FACTORY_PROP_NAME}</li>
         * </ul>
         * properties. If some property doesn't specified then specified in {@link Factories.Defaults}
         * implementations will be used.
         */
        public static void initFactories() {
            final Properties properties = System.getProperties();
            final String dataObjectFactoryClassName =
                    properties.getProperty(Factories.DATA_OBJECT_FACTORY_PROP_NAME, Factories.Defaults.DEFAULT_DATA_OBJECT_FACTORY);
            final String keyFactoryClassName =
                    properties.getProperty(Factories.KEY_FACTORY_PROP_NAME, Factories.Defaults.DEFAULT_KEY_FACTORY);
            final String versionFactoryClassName =
                    properties.getProperty(Factories.VERSION_FACTORY_PROP_NAME, Factories.Defaults.DEFAULT_VERSION_FACTORY);
            try {
                final ClassLoader classLoader = Instances.class.getClassLoader();
                final Class<?> dataObjectFactoryClass = classLoader.loadClass(dataObjectFactoryClassName);
                final Class<?> keyFactoryClass = classLoader.loadClass(keyFactoryClassName);
                final Class<?> versionFactoryClass = classLoader.loadClass(versionFactoryClassName);

                dataObjectFactory = (DataObjectFactory) dataObjectFactoryClass.newInstance();
                keyFactory = (KeyFactory) keyFactoryClass.newInstance();
                versionFactory = (VersionFactory) versionFactoryClass.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize model factories for serialization utils", e);
            }
        }

        public static DataObjectFactory getDataObjectFactory() {
            return dataObjectFactory;
        }

        public static KeyFactory getKeyFactory() {
            return keyFactory;
        }

        public static VersionFactory getVersionFactory() {
            return versionFactory;
        }
    }
}
