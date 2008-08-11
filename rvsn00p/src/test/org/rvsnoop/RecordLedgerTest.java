/*
 * Class:     RecordLedgerTest
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * Unit tests for the {@link RecordLedger} class.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
@SuppressWarnings({ "FeatureEnvy" })
public abstract class RecordLedgerTest extends TestCase {

    private class TestMatcher implements Matcher {
        final int target;
        TestMatcher(int target) {
            this.target = target;
            }
        public boolean matches(Object item) {
            return records[target].equals(item);
        }
    }

    private static final int BUFFER_SIZE = 1024;

    // A file containing 10 SAP invoices.
    private static final String TEST_DATA = "data/sap-invoices.rbz";

    private Connections connections;

    private RecordLedger ledger;

    private Record[] records;

    /**
     * Hook to allow creation and testing of different ledger implementations.
     *
     * @return The ledger to be tested.
     */
    protected abstract RecordLedger createRecordLedger();

    // FIXME: Create a RecordBundle class and put all of the logic to handle
    // record bundles into  it. Use the logic there instead of this copy/paste
    // hack. Do the same for RecordStream (or refactor RecordSelection).
    @Override
    public void setUp() throws IOException {
        connections = new Connections();
        ledger = createRecordLedger();
        final InputStream stream =
            new BufferedInputStream(ClassLoader.getSystemResource(TEST_DATA).openStream());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] bytes = new byte[BUFFER_SIZE];
        final ZipInputStream zip = new ZipInputStream(stream);
        final List<Record> recordList = new ArrayList<Record>();
        while (zip.getNextEntry() != null) {
            int count;
            buffer.reset();
            while ((count = zip.read(bytes, 0, BUFFER_SIZE)) != -1) {
                buffer.write(bytes, 0, count);
            }
            final DataInput in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            recordList.addAll(Arrays.asList(RecordSelection.read(in, connections)));
        }
        records = recordList.toArray(new Record[recordList.size()]);
        assertEquals(10, records.length);
        IOUtils.closeQuietly(stream);
    }

    public void testAdd() {
        assertEquals(0, ledger.size());
        ledger.add(records[0]);
        assertEquals(1, ledger.size());
        ledger.add(records[1]);
        assertEquals(2, ledger.size());
    }

    public void testAddAll() {
        assertEquals(0, ledger.size());
        ledger.addAll(Arrays.asList(records));
        assertEquals(10, ledger.size());
    }

    public void testClear() {
        ledger.addAll(Arrays.asList(records));
        assertEquals(10, ledger.size());
        ledger.clear();
        assertEquals(0, ledger.size());
    }

    /* should create a new model, with a new format, each time */
    public void testCreateTableModel() {
        EventTableModel m1 = ledger.createTableModel();
        EventTableModel m2 = ledger.createTableModel();
        assertNotSame(m1, m2);
        assertNotSame(m1.getTableFormat(), m2.getTableFormat());
    }

    public void testFind() {
        ledger.addAll(Arrays.asList(records));
        Matcher matcher = new TestMatcher(4);
        Record found = ledger.find(matcher, 0);
        assertSame(records[4], found);
        // Now test that the search wraps around correctly:
        found = ledger.find(matcher, 6);
        assertSame(records[4], found);
    }

    public void testFindAllIndices() {
        ledger.addAll(Arrays.asList(records));
        Matcher matcher = new TestMatcher(4);
        int[] indices = ledger.findAllIndices(matcher);
        assertEquals(1, indices.length);
        assertEquals(4, indices[0]);
    }

    public void testFindIndex() {
        ledger.addAll(Arrays.asList(records));
        Matcher matcher = new TestMatcher(4);
        assertEquals(4, ledger.findIndex(matcher, 0));
        // Now test that the search wraps around correctly:
        assertEquals(4, ledger.findIndex(matcher, 6));
    }

    public void testGet() {
        ledger.addAll(Arrays.asList(records));
        assertEquals(records[3], ledger.get(3));
    }

    public void testGetAll() {
        ledger.addAll(Arrays.asList(records));
        Record[] r = ledger.getAll(new int[] { 1, 3, 5 });
        assertEquals(records[1], r[0]);
        assertEquals(records[3], r[1]);
        assertEquals(records[5], r[2]);
    }

    public void testRemove() {
        ledger.addAll(Arrays.asList(records));
        assertTrue(ledger.contains(records[8]));
        assertTrue(ledger.remove(records[8]));
        assertFalse(ledger.contains(records[8]));
    }

    public void testRemoveAll() {
        ledger.addAll(Arrays.asList(records));
        assertEquals(10, ledger.size());
        Record[] toRemove = new Record[] {
            records[1], records[2], records[3], records[4], records[5]
        };
        ledger.removeAll(Arrays.asList(toRemove));
        assertEquals(5, ledger.size());
    }

    public void testSize() {
        assertEquals(0, ledger.size());
        ledger.addAll(Arrays.asList(records));
        assertEquals(10, ledger.size());
    }

}
