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

import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import mockit.Expectations;
import mockit.Mocked;
import org.jvnet.hudson.test.HudsonTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.PrintStream;

import static org.mockito.Mockito.when;

/**
 * @author Juri Timoshin
 */
public class LiveRebelProxyTest extends HudsonTestCase {

	@Mocked private final LiveApplicationUtil unused = null;
	@Mock private CommandCenterFactory ccfMock;
	@Mock private BuildListener listenerMock;
	@Mock private DeployPluginProxy deployPluginProxyMock;
	@Mock private PrintStream printStreamMock;

	LiveRebelProxy lrProxy;
	FilePath war = new FilePath( Hudson.MasterComputer.localChannel , "/some/irrelevant/path");


	public LiveRebelProxyTest(String name) {
		super(name);
		MockitoAnnotations.initMocks(this);
		when(listenerMock.getLogger()).thenReturn(printStreamMock);
		lrProxy = new LiveRebelProxy(ccfMock, listenerMock, deployPluginProxyMock);
	}

	public void setUp() throws Exception {
		super.setUp();
	}

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     *
     * Method: perform(FilePath[] wars, boolean useCargoIfIncompatible, boolean useLiverebelIfCompatibleWithWarnings)
     *
     */
    public void testPerform() throws Exception {
        //TODO: Test goes here...
    }


    /**
     *
     * Method: initCommandCenter()
     *
     */
    public void testInitCommandCenter() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("initCommandCenter");
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: isFirstRelease(ApplicationInfo applicationInfo)
     *
     */
    public void testIsFirstRelease() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("isFirstRelease", ApplicationInfo.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, FilePath warfile)
     *
     */
    public void testUpdate() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("update", LiveRebelXml.class, ApplicationInfo.class, FilePath.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: updateOnServer(LiveRebelXml lrXml, String server, String activeVersion, FilePath warfile)
     *
     */
    public void testUpdateOnServer() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("updateOnServer", LiveRebelXml.class, String.class, String.class, FilePath.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: cargoDeploy(FilePath warfile)
     *
     */
    public void testCargoDeploy() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("cargoDeploy", FilePath.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: getDifferences(LiveRebelXml lrXml, String activeVersion)
     *
     */
    public void testGetDifferences() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("getDifferences", LiveRebelXml.class, String.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: uploadIfNeeded(ApplicationInfo applicationInfo, String currentVersion, FilePath warFile)
     *
     */
    public void testUploadIfNeeded() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("uploadIfNeeded", ApplicationInfo.class, String.class, FilePath.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

    /**
     *
     * Method: uploadArtifact(File artifact)
     *
     */
    public void testUploadArtifact() throws Exception {
        //TODO: Test goes here...
        /*
        try {
           Method method = LiveRebelProxy.class.getMethod("uploadArtifact", File.class);
           method.setAccessible(true);
           method.invoke(<Object>, <Parameters>);
        } catch(NoSuchMethodException e) {
        } catch(IllegalAccessException e) {
        } catch(InvocationTargetException e) {
        }
        */
        }

	public void testGetLiveRebelXml() throws Exception {
		final LiveRebelXml expected = new LiveRebelXml("TestApplication", "1.4");

		new Expectations(){
			{
				LiveApplicationUtil.findLiveRebelXml(new File(war.getRemote())); result = expected;
			}
		};

		LiveRebelXml result = lrProxy.getLiveRebelXml(war);
		assertEquals(expected.getVersionId(), result.getVersionId());
		assertEquals(expected.getApplicationId(), result.getApplicationId());
	}
}
