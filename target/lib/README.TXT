Readme RvSnoop

* Copyright (C) The Apache Software Foundation. All rights reserved.
*
* This software is published under the terms of the Apache Software
* License version 1.1, a copy of which has been included with this
* distribution in the LICENSE.txt file.

RvSnoop is constructed by Örjan Lundberg <lundberg@home.se>

RvSnoop is based on Logfactor5

Project homepage is: http://rvsn00p.databasefocus.com

Source forge project is avaliable at 
rvsn00p.sourceforge.net

Please use the source forge page to submit bug reports, support request etc. 

USAGE
=====================

Example start command
java -classpath rvsn00p.jar;c:\tibco\tibrv\lib\tibrvj.jar;c:\tibco\adapter\sdk\java\Maverick4.jar;c:\tibco\im\java\jaxp.jar;c:\tibco\im\java\crimson.jar rvsn00p.StartRvSnooper "tcp:7500|7500||>" 

The argument "tcp:7500|7500||>"  means "daemon|service|network|subject" that RvSnoop should listen to. 








