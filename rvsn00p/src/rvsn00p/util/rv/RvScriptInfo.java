//:File:    RvScriptInfo.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import com.tibco.rvscript.tibrvXmlConvert;

/**
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvScriptInfo {

      public static boolean isAvaliable(){
          boolean retVal = true;
          try {
              new tibrvXmlConvert();
          } catch (Exception e) {
            retVal = false;
          }catch (Error e) {
            retVal = false;
          }

          return  retVal;
      }
}
