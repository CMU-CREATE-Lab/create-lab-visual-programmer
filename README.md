# Create-Lab-Visual-Programmer
The CREATE Lab Visual Programmer application.

Visual Programmer
-----------
The Create Lab Visual Programmer is a simple IDE for programming the [Hummingbird Robot](http://www.hummingbirdkit.com/). The Visual Programmer uses a story-boarding approach to programming that is quick and easy to learn. The interface allows you to rapidly create expressions  by selecting outputs and setting them on or off. Once several expressions have been built, you can create a sequence that uses these expressions to make a program.

Dependencies
-----------
  * Java SE 6

About this Branch
-----------
This branch is devoted to bugfixes from Birdbrain Technologies LLC. See below for a list of bugs that have been fixed/features that have been added.

Bugs Fixed
----------- 
  * **BP** Issue [#188](/../../issues/188) & [#186](/../../issues/186): Files starting with numbers are now okay. A letter is prepended onto the start automatically
  * **BP** Issue [#196](/../../issues/196): Sequences can now be reordered with arrows that move blocks up or down. 
  * **BP** Issues [#126](/../../issues/126) & [#128](/../../issues/128): Make audio player better. Adding support for all types of wav and mp3s. 
  * **BP** Fixed an issue where servo values were not being properly scaled
  * **BP** Issue [#81](/../../issues/81): Stopping a sequence stops audio from playing
  * **BP** Fixed an issue where after disconnecting and reconnecting a Hummingbird, the sequence builder experienced graphical glitches

Features Added/General Improvements
-----------
  * **CM** Issue [#192](/../../issues/192) is dealt with in this branch. This was fixed by [C3M20](https://github.com/C3M20) (Cristina Morales). Projects are now zip files stored inside of a folder.
  * **BP** Can open a project by selecting the folder the zip is in or directly selecting the zip file
  * **BP** Issue [#179](/../../issues/179) Make file explorer look nicer.
  * **BP** Old projects (not in zip format) are now automatically ported to the new format.
  * **BP** Issue [#43](/../../issues/43) When the Hummingbird gets disconnected and reconnected, the old project directory is automatically loaded back up and the sequence being worked on is reloaded
  * **BP** Issue [#7](/../../issues/7) Threads now work in the "Split Block" (To be renamed to "Do Both")

Changes in Progress
-----------
  * Add ability to multi-thread/run asynchronously (only for Visual Programmer side, not on export)
  
TODO
----
  * Issue [#202](/../../issues/202) Built in audio recording
  * Issue [#204](/../../issues/204) Have outputs update while slider is being slid
  * Issue [#207](/../../issues/207) Undo Button
  * Issue [#208](/../../issues/208) Replace Java icon in windows
  * Issue [#209](/../../issues/209) Create Custom file extension (and filter file viewer for it)
  * Issue [#210](/../../issues/210) Hover text for buttons in Sequence Builder
  * Issue [#211](/../../issues/211) Large graphical arrows for "Do Both" expression
  * Issue [#213](/../../issues/213) Create "Link" Structure to directly link an input to an output
  * Issue [#214](/../../issues/214) Revise Expression Builder output treatment to reflect "Off-Set-Stay" Model
  
  
