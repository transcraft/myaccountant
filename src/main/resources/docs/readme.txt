Transcraft My Accountant project

Included here is the source code for the Transcraft Book Keeper application. I had to change the name to Transcraft My Accountant to make it more Google friendly.

The whole directory is Eclipse IDE ready. All you need to do is point your Eclipse import at the src directory. However, before this can be done, you need to do one manual step, which is to configure the Eclipse classpath to match your OS. I have included three files prefixed with classpath, namely classpath.win32.Eclipse3.0, classpath.win32.Eclipse3.1, and classpath.win32.linux (Eclipse 3.1 only). You need to first copy this file into a file called .classpath in the same directory, then manually edit it to point to your installtion of Eclipse accordingly. Only then should you start up Eclipse and select the "Import existing project into workspace" option.

You can also invoke the install target in the Ant script to build for your Windows or Linux environment at deployment time. This Ant script will automatically exract the correct native SWT library for your platform, altrhough you will have to change it to point correctly at the location of your Eclipse installation.

Note for Linux

You need to install the Mozilla librabries in order to use the Browser feature. The feature is used to display HTML and PDF ad hoc reports. Extracted from the SWT FAQ at http://www.eclipse.org/swt/faq.php

Q: What do I need to run the SWT Browser in a standalone application on Linux GTK or Linux Motif?
    A: Follow the steps below to use the SWT Browser widget in your standalone SWT application.

       1. A supported version of Mozilla or Firefox must be installed (instructions here).
       2. Set the environment variable MOZILLA_FIVE_HOME to your Mozilla/Firefox installation folder. e.g. setenv MOZILLA_FIVE_HOME /usr/lib/mozilla
       3. Set the environmnent variable LD_LIBRARY_PATH to include MOZILLA_FIVE_HOME. e.g. setenv LD_LIBRARY_PATH ${MOZILLA_FIVE_HOME}:${LD_LIBRARY_PATH}
       4. Your standalone SWT application can now use the Browser widget. 



If you have any problem, contact me at david.tran@transcraft.co.uk
