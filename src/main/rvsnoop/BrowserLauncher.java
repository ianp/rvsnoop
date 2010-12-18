package rvsnoop;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Launch a web browser in a cross-platform manner.
 * <p>
 * <code>BrowserLauncher</code> is a class that provides one static method,
 * <code>openURL</code>, which opens the default web browser for the current
 * user of the system to the given URL. It may support other protocols depending
 * on the system -- mailto, ftp, etc. -- but that has not been rigorously tested
 * and is not guaranteed to work.
 * <p>
 * Yes, this is platform-specific code, and yes, it may rely on classes on
 * certain platforms that are not part of the standard JDK. What we're trying to
 * do, though, is to take something that's frequently desirable but inherently
 * platform-specific -- opening a default browser -- and allow programmers (you,
 * for example) to do so without worrying about dropping into native code or
 * doing anything else similarly evil.
 * <p>
 * This version of the class does not handle very old operating systems (e.g Mac
 * Classic or win9x) since they are not supported by RvSnoop. This version of
 * the class has had a lot of functionality removed, if you are considering
 * using this class in an application you should probably grab the full version
 * from SourceForge rather than using this one.
 * <p>
 * This code is Copyright 1999-2001 by Eric Albert (ejalbert@cs.stanford.edu)
 * and may be redistributed or modified in any form without restrictions as long
 * as the portion of this comment from this paragraph through the end of the
 * comment is not removed. The author requests that he be notified of any
 * application, applet, or other binary that makes use of this code, but that's
 * more out of curiosity than anything and is not required. This software
 * includes no warranty. The author is not repsonsible for any loss of data or
 * functionality or any adverse or unexpected effects of using this software.
 *
 * @author <a href="mailto:ejalbert@cs.stanford.edu">Eric Albert</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Release$ $Date$
 */
public class BrowserLauncher {

	/**
     * The Java virtual machine that we are running on.
     * <p>
     * Actually, in most cases we only care about the operating system, but some
     * operating systems require us to switch on the VM.
     */
	private static int jvm;

	/** The browser for the system */
	private static Object browser;

    /** The openURL method of com.apple.mrj.MRJFileUtils */
    private static Method openURL;

	/** JVM constant for Macintosh */
	private static final int MACOSX = 0;

	/** JVM constant for Windows */
	private static final int WINDOWS = 1;

	/** JVM constant for any other platform */
	private static final int OTHER = 2;

	/**
	 * The first parameter that needs to be passed into Runtime.exec() to open the default web
	 * browser on Windows.
	 */
    private static final String FIRST_WINDOWS_PARAMETER = "/c";

    /** The second parameter for Runtime.exec() on Windows. */
    private static final String SECOND_WINDOWS_PARAMETER = "start";

    /**
     * The third parameter for Runtime.exec() on Windows.  This is a "title"
     * parameter that the command line expects.  Setting this parameter allows
     * URLs containing spaces to work.
     */
    private static final String THIRD_WINDOWS_PARAMETER = "\"\"";

	/**
	 * The shell parameters for Netscape that opens a given URL in an already-open copy of Netscape
	 * on many command-line systems.
	 */
	private static final String NETSCAPE_REMOTE_PARAMETER = "-remote";
	private static final String NETSCAPE_OPEN_PARAMETER_START = "'openURL(";
	private static final String NETSCAPE_OPEN_PARAMETER_END = ")'";

	/**
	 * The message from any exception thrown throughout the initialization process.
	 */
	private static String errorMessage;

	/**
	 * An initialization block that determines the operating system and loads the necessary
	 * runtime data.
	 */
	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
		    jvm = MACOSX;
            try {
                final Class mrjFileUtilsClass = Class.forName("com.apple.mrj.MRJFileUtils");
                openURL = mrjFileUtilsClass.getDeclaredMethod("openURL", new Class[] { String.class });
            } catch (ClassNotFoundException cnfe) {
                errorMessage = cnfe.getMessage();
            } catch (NoSuchMethodException nsme) {
                errorMessage = nsme.getMessage();
            }
        } else if (osName.startsWith("Windows")) {
		    jvm = WINDOWS;
        } else {
			jvm = OTHER;
        }
	}

	/**
	 * This class should be never be instantiated; this just ensures so.
	 */
	private BrowserLauncher() {
        throw new UnsupportedOperationException();
    }

	/**
	 * Attempts to locate the default web browser on the local system.  Caches results so it
	 * only locates the browser once for each use of this class per JVM instance.
	 * @return The browser for the system.  Note that this may not be what you would consider
	 *			to be a standard web browser; instead, it's the application that gets called to
	 *			open the default web browser.  In some cases, this will be a non-String object
	 *			that provides the means of calling the default browser.
	 */
	private static Object locateBrowser() {
		if (browser != null)
			return browser;
		switch (jvm) {
			case MACOSX:
				browser = "";	// Return something non-null
				break;
			case WINDOWS:
				browser = "cmd.exe";
				break;
			case OTHER:
			default:
				browser = "netscape";
				break;
		}
		return browser;
	}

	/**
	 * Attempts to open the default web browser to the given URL.
	 * @param url The URL to open
	 * @throws IOException If the web browser could not be located or does not run
	 */
	public static void openURL(String url) throws IOException {
		final Object browser = locateBrowser();
		if (browser == null)
			throw new IOException("Unable to locate browser: " + errorMessage);
		switch (jvm) {
			case MACOSX:
				try {
					openURL.invoke(null, url);
				} catch (InvocationTargetException ite) {
					throw new IOException("InvocationTargetException while calling openURL: " + ite.getMessage());
				} catch (IllegalAccessException iae) {
					throw new IOException("IllegalAccessException while calling openURL: " + iae.getMessage());
				}
				break;
		    case WINDOWS:
                 // Add quotes around the URL to allow ampersands and other special
                 // characters to work.
				Process process = Runtime.getRuntime().exec(new String[] { (String) browser,
																FIRST_WINDOWS_PARAMETER,
																SECOND_WINDOWS_PARAMETER,
																THIRD_WINDOWS_PARAMETER,
																'"' + url + '"' });
				// This avoids a memory leak on some versions of Java on Windows.
				// That's hinted at in <http://developer.java.sun.com/developer/qow/archive/68/>.
				try {
					process.waitFor();
					process.exitValue();
				} catch (InterruptedException ie) {
					throw new IOException("InterruptedException while launching browser: " + ie.getMessage());
				}
				break;
			case OTHER:
				// Assume that we're on Unix and that Netscape is installed

				// First, attempt to open the URL in a currently running session of Netscape
				process = Runtime.getRuntime().exec(new String[] { (String) browser,
													NETSCAPE_REMOTE_PARAMETER,
													NETSCAPE_OPEN_PARAMETER_START +
													url +
													NETSCAPE_OPEN_PARAMETER_END });
				try {
					final int exitCode = process.waitFor();
					if (exitCode != 0) {	// if Netscape was not open
						Runtime.getRuntime().exec(new String[] { (String) browser, url });
					}
				} catch (InterruptedException ie) {
					throw new IOException("InterruptedException while launching browser: " + ie.getMessage());
				}
				break;
			default:
				// This should never occur, but if it does, we'll try the simplest thing possible
				Runtime.getRuntime().exec(new String[] { (String) browser, url });
				break;
		}
	}

}
