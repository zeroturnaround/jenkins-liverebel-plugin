package org.zeroturnaround.jenkins;

/*****************************************************************
Copyright 2011 ZeroTurnaround OÜ

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*****************************************************************/

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.plugins.deploy.ContainerAdapter;
import hudson.plugins.deploy.tomcat.Tomcat6xAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.PrintStream;

import static org.mockito.Mockito.*;

/**
 * @author Juri Timoshin
 */

public class DeployPluginProxyTest extends HudsonTestCase {
	public DeployPluginProxyTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCargoDeploy() throws Exception {
		ContainerAdapter adapterMock = mock(Tomcat6xAdapter.class);
		Build buildMock = mock(Build.class);
		Launcher launcherMock = mock(Launcher.class);
		BuildListener listenerMock = mock(BuildListener.class);

		PrintStream printStreamMock = mock(PrintStream.class);
		when(listenerMock.getLogger()).thenReturn(printStreamMock);

		FilePath war = new FilePath( Hudson.MasterComputer.localChannel , "/some/irrelevant/path");

		DeployPluginProxy deployPluginProxy = new DeployPluginProxy(adapterMock, buildMock, launcherMock, listenerMock);
		deployPluginProxy.cargoDeploy(war);

		verify(printStreamMock).println("Deploying new artifact without LiveRebel...");
		verify(adapterMock).redeploy(war, buildMock, launcherMock, listenerMock);
	}

	public static Test suite() {
		return new TestSuite(DeployPluginProxyTest.class);
	}
}
