==============================================
CREATE Lab Visual Programmer (version @version.number@)
==============================================

INTRODUCTION
============

Thank you for downloading the CREATE Lab Visual Programmer.  This is the standalone version of the software, intended
for use on computers with no or restricted Internet access.  Please be aware that this version will not automatically
update itself.  To get the latest version, you will need to re-download it from the Visual Programmer's home page:

   http://artsandbots.com/visualprogrammer/

For users having computers with unrestricted Internet access, we encourage the use of the Java Web Start version of the
software, available by clicking the "Launch" button at the above URL.  The Java Web Start version of the software
automatically updates itself to the latest version available.

HOW TO USE
==========

Hummingbird
-----------

To run the Visual Programmer for Hummingbird, simply double-click the visual-programmer-applications.jar file in this directory.

If you wish, you can create an alias/shortcut to the visual-programmer-applications.jar file and place the
alias/shortcut in a location that's easier for you to access.

Finch
-----

To run the Visual Programmer for Finch, you will currently need to launch it from the command line (we'll fix this soon).
To do so, open a command prompt window and navigate to the directory containing all the jars that were in the zip.  Then
run the following command:

   java -DVisualProgrammerDevice.class=edu.cmu.ri.createlab.finch.visualprogrammer.FinchVisualProgrammerDevice -jar visual-programmer-applications.jar

ADVANCED USAGE
==============

By default, the Visual Programmer stores its files in a "CREATELab" subdirectory of the user's home directory.  This is
problematic for some users (e.g. schools with shared computers, security restrictions, etc.).  To change where files
are stored, you can launch the Visual Programmer and supply it with the "CreateLabHomeDirectory" system property. To do
so, open a command prompt window and navigate to the directory containing all the jars that were in the zip.  Then run
the following command, replacing PATH_TO_DESIRED_DIRECTORY with the path to the directory in which you want files to be
saved:

   java -DCreateLabHomeDirectory=PATH_TO_DESIRED_DIRECTORY -jar visual-programmer-applications.jar

Or, for the Visual Programmer for Finch:

   java -DCreateLabHomeDirectory=PATH_TO_DESIRED_DIRECTORY -DVisualProgrammerDevice.class=edu.cmu.ri.createlab.finch.visualprogrammer.FinchVisualProgrammerDevice -jar visual-programmer-applications.jar

