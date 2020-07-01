Transcraft My Accountant 
========================

Book Keeping application written in [Java 8](https://www.oracle.com/java/technologies/javase-downloads.html) and GUI uses SWT technology from [Eclipse](https://www.eclipse.org/downloads/), making it as native look and feel as possible.

Java 8 was chosen because it is widely deployed and is the minimum version which can be used to compile up this application. It has been tested against Java 14 without any problem, although some of the code have had to be downgraded back to Java 8 syntax to ensure bytecode and JVM compatibility. These instances are clearly marked in the comments, in case you want to change them to the latest syntax.

Report rendering uses [JasperReports](https://github.com/TIBCOSoftware/jasperreports). The version used is 6.12.2.

Underlying database techonolgy uses [db4o](http://supportservices.actian.com/versant/default.html). The version used is 8.0.276.

Build environment is [Gradle](https://gradle.org/). The version used is 6.14.0.

This application is used actively for my own book keeping operations.

Configurations have been added to prepare and generate a .deb package for Debian Linux using debreate, and a Windows setup file for Windows. Windows packaging uses [Launch4j](http://launch4j.sourceforge.net/) to generate the native executable wrapper and [NSIS](https://nsis.sourceforge.io/Download) to package up the self extracting archive file.

For more information and user manual, please navigate down to the src/main/resources/docs/ folder and click on the index.html file.


