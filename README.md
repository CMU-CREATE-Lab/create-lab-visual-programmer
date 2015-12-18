# Create-Lab-Visual-Programmer
The CREATE Lab Visual Programmer application.

The Create Lab Visual Programmer is a simple IDE for programming the [Hummingbird Robot](http://www.hummingbirdkit.com/). The Visual Programmer uses a story-boarding approach to programming that is quick and easy to learn. The interface allows you to rapidly create expressions  by selecting outputs and setting them on or off. Once several expressions have been built, you can create a sequence that uses these expressions to make a program.

The Visual Programmer requires Java SE 6 or higher to run. 

This branch is devoted to bugfixes from Birdbrain Technologies LLC. See below for a list of bugs that have been fixed/features that have been added.

Bugs Fixed: 
  * **CM** Issue [#192](/../../issues/192) is dealt with in this branch. This was fixed by [C3M20](https://github.com/C3M20) (Cristina Morales). Projects are now zip files stored inside of a folder.
  * **BP** Issue [#188](/../../issues/188) & [#186](/../../issues/186): Files starting with numbers are now okay. A letter is prepended onto the start automatically
  * **BP** Issue [#196](/../../issues/196): Sequences can now be reordered with arrows that move blocks up or down. 
  * **BP** Issues [#126](/../../issues/126) & [#128](/../../issues/128): Make audio player better. Adding support for all types of wav and mp3s. 
  * **BP** Fixed an issue where servo values were not being properly scaled

Features Added/General Improvements: 
  * **BP** Can open a project by selecting the folder the zip is in or directly selecting the zip file
  * **BP** Issue [#179](/../../issues/179) Make file explorer look nicer.

In Progress: 
  * Issue [#81](/../../issues/81): Stopping a sequence stops audio from playing
      * Current State: A function exists in the audio managing code that is called when a sequence is stopped. It needs to be filled in with a way to stop all audio. Some sort of list of active threads playing audio needs to stored in the same class as that function and those threads have to be setup to stop audio when they're interrupted

To Fix:
  
