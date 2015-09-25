#!/bin/bash

java -Djna.library.path=./code/applications/dist -Djava.library.path=./code/applications/dist -cp ./code/applications/dist/visual-programmer-applications.jar edu.cmu.ri.createlab.visualprogrammer.VisualProgrammer;
