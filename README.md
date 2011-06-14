					LiveRebel Deploy Plugin for Jenkins

How to install/run locally?
----------------------------------------

1. Clone it.
2. Change your maven settings.xml according to https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial
	Nexus users should follow these steps https://wiki.jenkins-ci.org/display/JENKINS/HudsonWithNexus
	If you are using nexus but do not want to 	mess with Nexus Manager and configure all of the proxy repositories that hudson requires, then use the
	first link, but make sure you comment mirrors tag before running this project for the first time.
3. mvn install/package
4. To see the current plugin in action you have to have running LiveRebel.
		4a. Run "mvn hpi:run" and check the localhost:8080 - there should be jenkins running.
		4b. Go to global properties of jenkins and set Authentication Token and LiveRebel Url.
		4c. Create job. In Post-build Actions there will be a Deploy artifacts using LiveRebel field.
					Provided artifact will be uploaded to the running LiveRebel.
