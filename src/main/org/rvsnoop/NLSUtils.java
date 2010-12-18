// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Utility methods to load message values from property files and assign them
 * directly to the fields of a class. Originally based on a class from Equinox.
 */
@SuppressWarnings("unchecked")
public final class NLSUtils {

	private static final class PriviledgedFieldSetter implements PrivilegedAction {
        private final Field field;
        private final Object value;
        private PriviledgedFieldSetter(Field field, Object value) {
            this.field = field;
            this.value = value;
        }
        public Object run() {
            try {
                field.setAccessible(true);
                field.set(null, value);
            } catch (Exception e) {
                logger.error(e, "Error setting field %s to %s", field.getName(), value);
            } finally {
                field.setAccessible(false);
            }
            return null;
        }
    }

    /**
     * Class which sub-classes java.util.Properties and uses the #put method to
     * set field values rather than storing the values in the table.
     */
    private static class MessagesProperties extends Properties {

        private static final long serialVersionUID = 4566962090422045058L;

        // values can be either String or Field instances.
        private final Map namesToFields;

        private final String resourcePath;

        public MessagesProperties(Map namesToFields, String resourcePath) {
            super();
            this.namesToFields = namesToFields;
            this.resourcePath = resourcePath;
        }

        /**
         * Mark assigned fields to prevent them from being assigned twice.
         *
         * @see java.util.Hashtable#put(Object,Object)
         */
        @Override
        public synchronized Object put(Object name, Object value) {
            Object putObject = super.put(name, value); // value to return.
            Object previousFieldValue = namesToFields.put(name, ASSIGNED);
            // if already assigned, there is nothing to do
            if (previousFieldValue == ASSIGNED) {
                logger.debug("Ignoring duplicate property %s in %s", name, resourcePath);
                return putObject;
            }
            // if there is no field with a corresponding name, there is nothing to do
            if (previousFieldValue == null) {
                logger.debug("Unused message %s in %s", name, resourcePath);
                return putObject;
            }
            Field previousField = (Field) previousFieldValue;
            // Can only set value of public static non-final fields
            if ((previousField.getModifiers() & MOD_MASK) != Modifier.STATIC) {
                return putObject;
            }
            try {
                setField(previousField, value);
            } catch (Exception e) {
                logger.warn(e, "Could not set field %s", name);
            }
            return putObject;
        }
    }

    /**
     * This is assigned to the value of a field map to indicate that a
     * translated message has already been assigned to that field. It is a self
     * referential field, see the static load section for details.
     */
    static Field ASSIGNED;

    static final Logger logger = Logger.getLogger();

    static final int MOD_MASK = Modifier.STATIC | Modifier.FINAL;

    private static String[] nlSuffixes;

    static final String SUFFIX = ".properties"; //$NON-NLS-1$

    static {
        try {
            ASSIGNED = NLSUtils.class.getDeclaredField("ASSIGNED"); //$NON-NLS-1$
        } catch (Exception e) {
            logger.error(e, "Could not load internationalized strings correctly.");
        }
    }

    /**
     * Build an array of property files to search. The returned array contains
     * the property fields in order from most specific to most generic. So, in
     * the fr_FR locale, it will return file_fr_FR.properties, then
     * file_fr.properties, and finally file.properties.
     */
    private static String[] buildVariants(String resourcePath, String suffix) {
        if (nlSuffixes == null) {
            String nl = Locale.getDefault().toString();
            final List result = new ArrayList(4);
            int lastSeparator;
            while (true) {
                result.add('_' + nl);
                lastSeparator = nl.lastIndexOf('_');
                if (lastSeparator == -1) { break; }
                nl = nl.substring(0, lastSeparator);
            }
            // Add the empty (most general) suffix last
            result.add("");
            nlSuffixes = (String[]) result.toArray(new String[result.size()]);
        }
        String[] variants = new String[nlSuffixes.length];
        for (int i = 0; i < variants.length; i++) {
            variants[i] = resourcePath + nlSuffixes[i] + suffix;
        }
        return variants;
    }

    private static void computeMissingMessages(String resourcePath, Map namesToFields, Field[] fields) {
        // Make sure that there aren't any empty fields
        for (int i = 0, imax = fields.length; i < imax; ++i) {
            Field field = fields[i];
            if ((field.getModifiers() & MOD_MASK) != Modifier.STATIC)
                continue;
            // If the field has a a value assigned, there is nothing to do
            if (namesToFields.get(field.getName()) == ASSIGNED) { continue; }
            try {
                logger.warn("Missing message: %s in: %s", field.getName(), resourcePath);
                setField(field, "!" + field.getName() + "!"); //$NON-NLS-1$ $NON-NLS-2$
            } catch (Exception e) {
                logger.error(e, "Could not set missing message for %s", field.getName());
            }
        }
    }

    /**
     * Locate a "localized" file. For example a license file could be located by
     * calling <code>NLS.findNLSResource("/META-INF/license.txt", clazz);</code>.
     *
     * @param resourcePath The resource to load.
     * @param clazz The context ({@link java.lang.ClassLoader}) to load from.
     * @return The resource, or <code>null</code> if it could not be found.
     */
    public static URL findNLSResource(String resourcePath, Class clazz) {
        logger.debug("Loading NLS resource from path: %s", resourcePath);
        final int lastIndex = resourcePath.lastIndexOf(".");
        final String basePath = resourcePath.substring(0, lastIndex);
        final String suffix = resourcePath.substring(lastIndex);
        final String[] variants = buildVariants(basePath, suffix);
        for (int i = 0, imax = variants.length; i < imax; ++i) {
            URL url = clazz.getResource(variants[i]);
            if (url != null) { return url; }
        }
        return null;
    }

    /** Internationalize a class. */
    public static void internationalize(Class clazz) {
        String resource = "/" + clazz.getName().replaceAll("\\.", "/");
        internationalize(resource, clazz);
    }

    /**
     * Internationalize a class.
     * <p>
     * This will load strings from the file at <code>resourcePath</code> and
     * set any <code>static String</code> fields on <code>clazz</code>. The
     * class' <code>ClassLoader</code> will be used to locate the resource
     * file.
     */
    public static void internationalize(final String resourcePath, Class clazz) {
        long start = System.currentTimeMillis();
        logger.debug("Loading NLS bundle for %s from %s", clazz.getSimpleName(), resourcePath);
        Field[] fields = clazz.getDeclaredFields();
        // Strip out compiler generated synthetic fields
        Field[] tmpFields = new Field[fields.length];
        int numFields = 0;
        for (int i = 0, imax = fields.length; i < imax; ++i) {
            final Field field = fields[i];
            if (field.getType() == String.class && !(field.getName().indexOf('$') >= 0)) { //$NON-NLS-1$
                tmpFields[numFields++] = field;
            }
        }
        fields = new Field[numFields];
        System.arraycopy(tmpFields, 0, fields, 0, fields.length);
        // OK, ready to use the field array now.
        // Build a map of field names to Field objects
        final int len = fields.length;
        final Map namesToFields = new HashMap(len * 2);
        for (int i = 0; i < len; i++) {
            namesToFields.put(fields[i].getName(), fields[i]);
        }
        // Search the variants from most specific to most general
        final String[] variants = buildVariants(resourcePath, SUFFIX);
        for (int i = 0, imax = variants.length; i < imax; ++i) {
            String variant = variants[i];
            final InputStream input = clazz.getResourceAsStream(variant);
            if (input == null) { continue; }
            try {
                final MessagesProperties properties = new MessagesProperties(
                        namesToFields, resourcePath);
                properties.load(input);
            } catch (IOException e) {
                logger.error(e, "Error loading message resource %s", variant);
            } finally {
                closeQuietly(input);
            }
        }
        computeMissingMessages(resourcePath, namesToFields, fields);
        logger.debug("Message bundle %s loaded in %s msecs.", resourcePath, System.currentTimeMillis() - start);
    }

    static void setField(final Field field, final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (field.isAccessible()) {
            field.set(null, value);
        } else {
            AccessController.doPrivileged(new PriviledgedFieldSetter(field, value));
        }
    }

    /** Private constructor, do not instantiate. */
    private NLSUtils() { throw new UnsupportedOperationException(); }

}
