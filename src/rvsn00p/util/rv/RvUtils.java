package rvsn00p.util.rv;

import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvException;

/**
 * RvUtility methods.
 */
public class RvUtils {

    /**
     * Retrieve a tracking id from a TibrvMsg.
     * @param msg Rv Message to extract tracking id from
     * @return string containing tracing id
     * @throws TibrvException
     */
    public static String getTrackingId(TibrvMsg msg) throws TibrvException {
            TibrvMsgField f = null;
            f = msg.getField("^tracking^");
            if (f != null) {
                TibrvMsg tid = (TibrvMsg) f.data;
                return (String) tid.get("^id^");
            }

        return "";
    }

}
