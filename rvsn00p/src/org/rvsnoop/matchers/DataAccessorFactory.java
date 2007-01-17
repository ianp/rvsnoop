/*
 * Class:     DataAccessorFactory
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.rvsnoop.NLSUtils;

/**
 * A factory for creating data accessors.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class DataAccessorFactory {

    static { NLSUtils.internationalize(DataAccessorFactory.class); }

    private static DataAccessorFactory instance;

    static String ERROR_BAD_IDENTIFIER, ERROR_BAD_NAME;

    public static synchronized DataAccessorFactory getInstance() {
        if (instance == null) { instance = new DataAccessorFactory(); }
        return instance;
    }

    private final Map identifiersToAccessorsMap = new LinkedHashMap();

    private final Map namesToAccessorsMap = new LinkedHashMap();

    private DataAccessorFactory() {
        identifiersToAccessorsMap.put(DataAccessor.FieldContents.IDENTIFIER, DataAccessor.FieldContents.class);
        identifiersToAccessorsMap.put(DataAccessor.FieldNames.IDENTIFIER, DataAccessor.FieldNames.class);
        identifiersToAccessorsMap.put(DataAccessor.ReplySubject.IDENTIFIER, DataAccessor.ReplySubject.class);
        identifiersToAccessorsMap.put(DataAccessor.SendSubject.IDENTIFIER, DataAccessor.SendSubject.class);
        identifiersToAccessorsMap.put(DataAccessor.TrackingId.IDENTIFIER, DataAccessor.TrackingId.class);
        namesToAccessorsMap.put(DataAccessor.FIELD_CONTENTS, DataAccessor.FieldContents.class);
        namesToAccessorsMap.put(DataAccessor.FIELD_NAMES, DataAccessor.FieldNames.class);
        namesToAccessorsMap.put(DataAccessor.REPLY_SUBJECT, DataAccessor.ReplySubject.class);
        namesToAccessorsMap.put(DataAccessor.SEND_SUBJECT, DataAccessor.SendSubject.class);
        namesToAccessorsMap.put(DataAccessor.TRACKING_ID, DataAccessor.TrackingId.class);
    }

    public DataAccessor createSendSubjectAccessor() {
        return new DataAccessor.SendSubject();
    }

    public DataAccessor createFromDisplayName(String name) {
        return createFromString(namesToAccessorsMap, name, ERROR_BAD_NAME);
    }

    public DataAccessor createFromIdentifier(String identifier) {
        return createFromString(identifiersToAccessorsMap, identifier, ERROR_BAD_IDENTIFIER);
    }

    private DataAccessor createFromString(Map map, String string, String errorMessage) {
        Validate.notNull(string);
        Class clazz = (Class) map.get(string);
        if (clazz == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format(errorMessage, new Object[] { string }));
        }
        try {
            return (DataAccessor) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getDisplayNames() {
        final Set names = namesToAccessorsMap.keySet();
        return (String[]) names.toArray(new String[names.size()]);
    }

}
