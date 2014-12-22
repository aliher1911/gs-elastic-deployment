gs-elastic-deployment
=====================

Starting GS
-----------
To run, start agent on two nodes using:
export GSA_JAVA_OPTIONS="-Dcom.gs.zones=zone1 -Xmx256M"; gs-agent.sh gsa.global.lus 2 gsa.global.gsm 1 gsa.global.esm 1 gsa.gsc 0

Configuring test
----------------
Add host locators into files with main().
Alternatively, if multicast works, then use appropriate group in locators.

Build
-----
Build, including artifacts is based on Idea project atm.
Paths to artifacts generated and jars used are hardcoded.

