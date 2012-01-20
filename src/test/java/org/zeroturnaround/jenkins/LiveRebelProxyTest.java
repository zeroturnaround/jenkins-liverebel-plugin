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
import hudson.model.BuildListener;
import hudson.model.Hudson;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import junit.framework.TestCase;
import mockit.Expectations;
import mockit.Mocked;

import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.zeroturnaround.liverebel.api.*;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
import com.zeroturnaround.liverebel.util.LiveRebelXml;

/**
 * @author Juri Timoshin
 */
public class LiveRebelProxyTest extends TestCase {

  @Mocked
  final LiveApplicationUtil unused = null;
  @Mock
  private CommandCenterFactory ccfMock;
  @Mock
  private CommandCenter commandCenterMock;
  @Mock
  private BuildListener listenerMock;
  @Mock
  private PrintStream printStreamMock;
  private final LiveRebelProxy lrProxy;
  private final FilePath war = new FilePath(Hudson.MasterComputer.localChannel, "/some/irrelevant/path");
  private final LiveRebelXml lrXml = new LiveRebelXml("TestApplication", "1.4");
  private final List<String> serverIds = new ArrayList<String>();

  public LiveRebelProxyTest(String name) {
    super(name);
    MockitoAnnotations.initMocks(this);
    when(listenerMock.getLogger()).thenReturn(printStreamMock);
    lrProxy = new LiveRebelProxy(ccfMock, listenerMock);

  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    serverIds.add("randomservername");
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPerformNoWars() throws Exception {
    assertFalse(lrProxy.perform(new FilePath[]{}, serverIds, false));
    verify(printStreamMock).println("Could not find any artifact to deploy. Please, specify it in job configuration.");
  }

  public void testPerformFailInitCommandCenter() throws Exception {
    LiveRebelProxy lrProxySpy = spy(lrProxy);
    doReturn(false).when(lrProxySpy).initCommandCenter();
    assertFalse(lrProxySpy.perform(new FilePath[]{war}, serverIds, false));
  }

  public void testPerformSuccess() throws Exception {
    LiveRebelProxy lrProxySpy = spy(lrProxy);
    doReturn(lrXml).when(lrProxySpy).getLiveRebelXml(war);
    ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
    doReturn(applicationInfoMock).when(commandCenterMock).getApplication(lrXml.getApplicationId());
    doReturn(true).when(lrProxySpy).initCommandCenter();
    lrProxySpy.commandCenter = commandCenterMock;
    doNothing().when(lrProxySpy).uploadIfNeeded(applicationInfoMock, "1.4", war);
    doReturn(true).when(lrProxySpy).update(lrXml, applicationInfoMock, war, serverIds);

    lrProxySpy.perform(new FilePath[]{war}, serverIds, false);

    verify(printStreamMock).printf("Processing artifact: %s\n", war);
    verify(lrProxySpy).getLiveRebelXml(war);
    verify(commandCenterMock).getApplication(lrXml.getApplicationId());
    verify(lrProxySpy).uploadIfNeeded(applicationInfoMock, "1.4", war);
    verify(lrProxySpy).update(lrXml, applicationInfoMock, war, serverIds);
    verify(printStreamMock).printf(LiveRebelProxy.ARTIFACT_DEPLOYED_AND_UPDATED, war);
  }

  public void testInitCommandCenterSuccess() throws Exception {
    when(ccfMock.newCommandCenter()).thenReturn(commandCenterMock);
    assertTrue(lrProxy.initCommandCenter());
    verify(ccfMock).newCommandCenter();
  }

  public void testInitCommandCenterForbidden() throws Exception {
    when(ccfMock.newCommandCenter()).thenThrow(new Forbidden("irrelevant", 111, "irrelevant"));
    assertFalse(lrProxy.initCommandCenter());

    verify(ccfMock).newCommandCenter();
    verify(printStreamMock).println("ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
  }

  public void testInitCommandCenterConnectException() throws Exception {
    when(ccfMock.newCommandCenter()).thenThrow(new ConnectException("urlValue", "Some Reason"));
    assertFalse(lrProxy.initCommandCenter());

    verify(ccfMock).newCommandCenter();

    verify(printStreamMock).println("ERROR! Unable to connect to server.");
    verify(printStreamMock).println("URL: urlValue");
    verify(printStreamMock).println("Reason: Some Reason");
  }

  public void testInitCommandCenterConnectExceptionEmptyUrl() throws Exception {
    when(ccfMock.newCommandCenter()).thenThrow(new ConnectException("https://", "Some Reason"));
    assertFalse(lrProxy.initCommandCenter());

    verify(ccfMock).newCommandCenter();

    verify(printStreamMock).println("ERROR! Unable to connect to server.");
    verify(printStreamMock).println("URL: https://");
    verify(printStreamMock).println("Please, navigate to Jenkins Configuration to specify running LiveRebel Url.");
  }

  public void testIsFirstRelease() throws Exception {
    assertTrue(lrProxy.isFirstRelease(null));
    assertFalse(lrProxy.isFirstRelease(mock(ApplicationInfo.class)));
  }

  public void testUpdateSuccess() throws Exception {
    lrProxy.commandCenter = commandCenterMock;
    ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
    when(applicationInfoMock.getActiveVersionPerServer()).thenReturn(
        new HashMap<String, String>() {

          {
            put("firstKey", "firstValue");
            put("secondKey", "secondValue");
          }
        });

    LiveRebelProxy lrProxySpy = spy(lrProxy);
    doReturn(true).when(lrProxySpy).activate(Matchers.<LiveRebelXml>any(), Matchers.<String>any(),
        Matchers.<String>any(), Matchers.<FilePath>any());

    lrProxySpy.update(lrXml, applicationInfoMock, war, serverIds);

    verify(printStreamMock).println("Starting updating application on servers:");
    verify(lrProxySpy).activate(lrXml, "firstKey", "firstValue", war);
    verify(lrProxySpy).activate(lrXml, "secondKey", "secondValue", war);
  }

  public void testUpdateWhenFirstReleaseSuccess() throws Exception {
    lrProxy.commandCenter = commandCenterMock;

    LiveRebelProxy lrProxySpy = spy(lrProxy);
    doReturn(true).when(lrProxySpy).activate(Matchers.<LiveRebelXml>any(), Matchers.<String>any(),
        Matchers.<String>any(), Matchers.<FilePath>any());

    lrProxySpy.update(lrXml, null, war, serverIds);

    verify(printStreamMock).println("Starting updating application on servers:");
    verify(lrProxySpy).deploy(lrXml, war, serverIds);
  }

  public void testGetDifferences() throws Exception {
    lrProxy.commandCenter = commandCenterMock;
    DiffResult diffResultMock = mock(DiffResult.class);
    doReturn(diffResultMock).when(commandCenterMock).compare(lrXml.getApplicationId(), "1.3", lrXml.getVersionId(), false);

    assertEquals(diffResultMock, lrProxy.getDifferences(lrXml, "1.3"));
    verify(diffResultMock).print(printStreamMock);
  }

  public void testUploadIfNeededAlreadyUploaded() throws Exception {
    ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
    doReturn(new HashSet<String>() {

      {
        add("1.3");
        add("1.4");
      }
    }).when(applicationInfoMock).getVersions();

    lrProxy.uploadIfNeeded(applicationInfoMock, "1.4", war);
    verify(printStreamMock).println("Current version of application is already uploaded. Skipping upload.");
  }

  public void testUploadIfNeededUpload() throws Exception {
    ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
    doReturn(new HashSet<String>() {

      {
        add("1.3");
      }
    }).when(applicationInfoMock).getVersions();

    LiveRebelProxy lrProxySpy = spy(lrProxy);
    doReturn(true).when(lrProxySpy).uploadArtifact(Matchers.<File>any());

    lrProxySpy.uploadIfNeeded(applicationInfoMock, "1.4", war);
    verify(lrProxySpy).uploadArtifact(new File(war.getRemote()));
  }

  public void testUploadArtifact() throws Exception {
    UploadInfo uploadInfoMock = mock(UploadInfo.class);
    when(uploadInfoMock.getApplicationId()).thenReturn("TestApplication");
    when(uploadInfoMock.getVersionId()).thenReturn("1.4");
    when(commandCenterMock.upload(new File(war.getRemote()))).thenReturn(uploadInfoMock);

    lrProxy.commandCenter = commandCenterMock;
    assertTrue(lrProxy.uploadArtifact(new File(war.getRemote())));
    verify(commandCenterMock).upload(new File(war.getRemote()));
    verify(printStreamMock).printf("SUCCESS: %s %s was uploaded.\n", "TestApplication", "1.4");
  }

  public void testGetLiveRebelXml() throws Exception {
    new Expectations() {

      {
        LiveApplicationUtil.findLiveRebelXml(new File(war.getRemote()));
        result = lrXml;
      }
    };

    LiveRebelXml result = lrProxy.getLiveRebelXml(war);
    assertEquals(lrXml.getVersionId(), result.getVersionId());
    assertEquals(lrXml.getApplicationId(), result.getApplicationId());
  }
}
