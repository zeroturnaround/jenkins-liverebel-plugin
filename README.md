LiveRebel Deploy Plugin for Jenkins
==================================

LiveRebel Deploy Plugin for Jenkins/Hudson helps to run updates to your JEE containers faster. LiveRebel is tool for hot updates without downtime, lost sessions and OutOfMemoryErrors. This plugin will take a WAR/EAR file and find out if LiveRebel supports the update and if it does it will use LiveRebel to run the update. If not then the Deploy Plugin is used instead. The plugin requires a LiveRebel Command Center running and security token to interact with it.

Installation
--------------

You can install the latest version from inside jenkins (Home -> Manage Jenkins -> Manage plugins).

Installation for development
----------------------------------------

1. Clone it.
2. Change your maven settings.xml according to https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial
  Nexus users should follow these steps https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+Development+Environment+with+Nexus
  If you are using nexus but do not want to mess with Nexus Manager and configure all of the proxy repositories that hudson requires, then use the first link, but make sure you comment mirrors tag before running this project for the first time.

3. mvn install/package
4. To see the current plugin in action you have to have running LiveRebel.
  * Run "mvn hpi:run" and check the localhost:8080 - there should be jenkins running.
  * Go to global properties of jenkins and set Authentication Token and LiveRebel Url.
  * Create job. In Post-build Actions there will be a Deploy artifacts using LiveRebel field. Provided artifact will be uploaded to the running LiveRebel.
