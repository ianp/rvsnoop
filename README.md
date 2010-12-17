# About RvSnoop

RvSnoop is a graphical application that provides developers and operations support personnel with a sophisticated and feature-rich interface for tracing tibco Rendezvous™ messages. If you are reading an off-line copy of this document it may be out of date. The RvSnoop SourceForge site always holds the latest copy of this documentation.

RvSnoop allows you to filter out subjects that you are not interested in making it easier to spot important information and messages. Reduce the time required to locate specific messages by using customizable highlight colours.

RvSnoop is cross platform and will run anywhere there is a suitable virtual machine. RvSnoop requires Java 1.4.2 or later.

# Building

RvSnoop is built with Ant, just run `ant` from the root directory.

All of the open-source dependencies are already included in the lib directory so there is nothing to worry about there.

The TIBCO proprietary dependencies are expected to be in the standard `/opt/tibco` location, if they are not then you may create a build.properties file with alternate paths, look at the `init` target in `build.xml` for information about how to set this up.

