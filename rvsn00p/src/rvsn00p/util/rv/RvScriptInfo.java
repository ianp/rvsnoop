/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import  com.tibco.rvscript.tibrvXmlConvert;

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
