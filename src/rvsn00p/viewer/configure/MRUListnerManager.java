/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer.configure;

import rvsn00p.util.rv.RvParameters;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * <p>MRUListnerManager handles the storage and retrival the most
 * recently opened log files.
 *
 * @author Örjan Lundberg
 *
 * Based on Logfactor5 By
 *
 * @author Brad Marlborough
 * @author Richard Hurst
 */

// Contributed by ThoughtWorks Inc.

public class MRUListnerManager {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------
    private static final String CONFIG_FILE_NAME = "mru_listner_manager";
    private static final int DEFAULT_MAX_SIZE = 3;

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private int _maxSize = 0;
    private LinkedList _mruListnerList;


    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    public MRUListnerManager() {
        load();
        setMaxSize(DEFAULT_MAX_SIZE);
    }

    public MRUListnerManager(int maxSize) {
        load();
        setMaxSize(maxSize);
    }
    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    /**
     * Saves a list of MRU files out to a file.
     */
    public void save() {
        File file = new File(getFilename());

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new
                    FileOutputStream(file));
            oos.writeObject(_mruListnerList);
            oos.flush();
            oos.close();
        }
        catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
    }

    /**
     * Gets the size of the MRU file list.
     */
    public int size() {
        return _mruListnerList.size();
    }

    /**
     * Returns a particular listenter stored in a Listener
     * list based on an index value.
     */
    public Object getListner(int index) {
        if (index < size()) {
            return _mruListnerList.get(index);
        }

        return null;
    }

    /**
     * Returns a input stream to the resource at the specified index
     */
    public InputStream getInputStream(int index) throws IOException,
            FileNotFoundException {
        if (index < size()) {
            Object o = getListner(index);
            if (o instanceof File) {
                return getInputStream((File) o);
            } else {
                return getInputStream((URL) o);
            }
        }
        return null;
    }

    /**
     * Adds a file name to the Listener list.
     */
    public void set(RvParameters p) {
        setMRU(p);
    }


    /**
     * Gets the list of files stored in the Listener list.
     */
    public String[] getMRUFileList() {
        if (size() == 0) {
            return null;
        }

        String[] ss = new String[size()];


        for (int i = 0; i < size(); ++i) {
            String Tstring = new String();
            RvParameters p = (RvParameters) getListner(i);
            Tstring += p.getSubject();
            Tstring += "|";
            Tstring += p.getService();
            Tstring += "|";
            Tstring += p.getNetwork();

            ss[i] = Tstring;


        }

        return ss;
    }

    /**
     * Moves the the index to the top of the MRU List
     *
     * @param index The index to be first in the mru list
     */
    public void moveToTop(int index) {
        _mruListnerList.add(0, _mruListnerList.remove(index));
    }

    /**
     * Creates the directory where the Listener list will be written.
     * The ".rvsnoop" directory is created in the Documents and Settings
     * directory on Windows 2000 machines and where ever the user.home
     * variable points on all other platforms.
     */
    public static void createConfigurationDirectory() {
        String home = System.getProperty("user.home");
        String sep = System.getProperty("file.separator");
        File f = new File(home + sep + ".rvsnoop");
        if (!f.exists()) {
            try {
                f.mkdir();
            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
        }

    }
    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------
    /**
     * Gets an input stream for the corresponding file.
     *
     * @param file The file to create the input stream from.
     * @return InputStream
     */
    protected InputStream getInputStream(File file) throws IOException,
            FileNotFoundException {
        BufferedInputStream reader =
                new BufferedInputStream(new FileInputStream(file));

        return reader;
    }

    /**
     * Gets an input stream for the corresponding URL.
     *
     * @param url The url to create the input stream from.
     * @return InputStream
     */
    protected InputStream getInputStream(URL url) throws IOException {
        return url.openStream();
    }

    /**
     * Adds an object to the mru.
     */
    protected void setMRU(Object o) {
        int index = _mruListnerList.indexOf(o);

        if (index == -1) {
            _mruListnerList.add(0, o);
            setMaxSize(_maxSize);
        } else {
            moveToTop(index);
        }
    }

    /**
     * Loads the Listener list in from a file and stores it in a LinkedList.
     * If no file exists, a new LinkedList is created.
     */
    protected void load() {
        createConfigurationDirectory();
        File file = new File(getFilename());
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(file));
                _mruListnerList = (LinkedList) ois.readObject();
                ois.close();

                // check that only RvParameters
                Iterator it = _mruListnerList.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (!(o instanceof RvParameters)) {
                        it.remove();
                    }
                }
            }
            catch (Exception e) {
                _mruListnerList = new LinkedList();
            }
        } else {
            _mruListnerList = new LinkedList();
        }

    }

    protected String getFilename() {
        String home = System.getProperty("user.home");
        String sep = System.getProperty("file.separator");

        return home + sep +  ".rvsnoop" + sep + CONFIG_FILE_NAME;
    }

    /**
     * Ensures that the MRU list will have a MaxSize.
     */
    protected void setMaxSize(int maxSize) {
        if (maxSize < _mruListnerList.size()) {
            for (int i = 0; i < _mruListnerList.size() - maxSize; ++i) {
                _mruListnerList.removeLast();
            }
        }

        _maxSize = maxSize;
    }
    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces
    //--------------------------------------------------------------------------
}