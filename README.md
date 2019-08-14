# jCVSim
Fork of the cardiovascular simulation program CVSim 1.16 with
simulation of intrathoracic pressure and  reflex effects of breathing added.

This is a program I worked on in 2016-2017, with advice on physiology
from Simon Wegerif, Pedro Fernandes Vargas and Alison McConnell of
Bournemouth University.  Their research field is in respiration and
they wanted to use the program to help them with that.

I took the CVSim cardiovascular simulation program originally written
by Thomas Heldt and extended by his collaborators and I added
simulation of intrathoracic pressure and the reflexes due to lung
expansion.

The original CVSim program is a Java GUI wrapped around a simulation
engine written in C.  I converted it to Java, making changes on the
way to improve readability.  The most important of these were to
change lots of array indices to symbolic names.  CVSim itself is
available from https://physionet.org/content/cvsim/1.0.0/ and there is
plenty of documentation about it, which is also largely applicable to
my version of it, there.

![screenshot](/screenshot.png)

Installation
------------

The code was developed for JDK 8 but compiles and runs OK with JDK 11.
These are the instructions for a Linux terminal window.

- Run ```git clone https://github.com/isopleth/jCVSim.git``` to obtain the source code
- ```cd jCVSim/jCVSim``` to change the default directory to the source tree
- Run ```ant``` to build the project using build.xml and the sources in the src tree
- Run ```java -jar dist/jcvsim.jar``` (or the Bash script ```run-jCVSim```) to run the program

Alternatively there is a copy of the jar file in the bin directory.
