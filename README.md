ChemEQL
=======

ChemEQL calculates and draws **thermodynamic equilibrium** concentrations of species in complex chemical systems.
It handles **homogeneous solutions, dissolution, precipitation, titrations** with acids, bases, or other components.
**Adsorption** on up to five different particulate surfaces can be modeled with the choice of the Constant Capacitance,Diffuse Layer (Generalized Two Layer), Basic Stern Layer, or Triple Layer model to consider surface charges.
Corrections for ionic strength can be made and **activities** calculated.
**Kinetic reactions** can be simulated with one rate determining process in a system of otherwise fast thermodynamic chemical equilibrium.
**Two-dimensional logarithmic diagrams**, such as pe-pH, or generally pX vs. pY, can be calculated.
A drawing option is implemented.

A **library** with over 1750 thermodynamic stability constants allows quick access, comfortable creation of input matrices and hence easy use of the program.
A second library contains more than 300 solubility products can be introduced if **solid phases** are modeled.
Both libraries can be altered and extended with new reactions and stability constants.
Output data are formatted for .xls or for import in a graphic program.

ChemEQL 3.1 is a Java port (by freelancer Kai-Holger Brassel) of a native Mac OS 9 application that in turn was an extended and user-friendly version (by Dr. Beat Müller from EAWAG) of the original program MICROQL by John Westall.
ChemEQL 3.1 and its manual with many representative examples are available at: http://www.eawag.ch/en/department/surf/projects/chemeql/.

In 2013 this repository was created with the aim to modernize the application by using a recent Java version (Java 8 at that time), move from Swing to JavaFX for the UI toolkit and replace the outdated proprietary diagram library de.vseit.showit, again by JavaFX.
After donating the code and some first refactorings, this effort came to a halt, leaving the code base in a state that will not compile.

Meanwhile Beat Müller retired and Kai is busy with other projects.
Kai cannot afford to work on ChemEQL 4 without sponsering.
Also note, that given the developments in scientific computing over the last decade, rewriting the application in Python may be a worthwhile alternative to updating the Java version.
In any case, this repo will preserverve the legacy of ChemEQL in the meantime.
