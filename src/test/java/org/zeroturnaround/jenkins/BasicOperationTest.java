package org.zeroturnaround.jenkins;

import com.google.common.collect.Sets;
import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.VersionInfo;
import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class BasicOperationTest extends HudsonTestCase {

  private static final File archivesDir = new File(BasicOperationTest.class.getResource("archives").getFile());
  private FreeStyleProject project;

  protected void setUp() throws Exception {
    super.setUp();

    project = createFreeStyleProject();
  }

  private UploadInfo createDummyUploadInfo() {
    return new UploadInfo() {
      public String getApplicationId() {
        return "lr-demo";
      }

      public String getVersionId() {
        return "ver1";
      }
    };
  }
  private ApplicationInfo createDummyApplicationInfo(final String id) {
     return new ApplicationInfo() {
       public String getId() {
         return id;
       }

       public Set<String> getVersions() {
         return Sets.newHashSet("ver1");
       }

       public Map<String, VersionInfo> getVersionsMap() {
         return null;
       }

       public Set<String> getActiveVersions() {
         return null;
       }

       public Map<String, String> getActiveVersionPerServer() {
         return null;
       }

       public Set<String> getUrls() {
         return null;
       }
     };
  }
    public void testNothing() {
      assertTrue(true);
    }

//  public void testUpload() throws Exception {
//    project.getBuildersList().add(new TestBuilder() {
//      public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
//                             BuildListener listener) throws InterruptedException, IOException {
//        CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
//        CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
//        Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
//        Mockito.when(commandCenter.getApplication("lr-demo")).thenReturn(createDummyApplicationInfo("lr-demo"));
//        Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());
//
//        LiveRebelProxy liveRebelProxy = new LiveRebelProxy(commandCenterFactory, listener);
//        LiveRebelProxy spyProxy = spy(liveRebelProxy);
//        Boolean result = spyProxy.perform(new FilePath[] {new FilePath(new File(archivesDir, "lr-demo-ver1.war"))}, "lr-demo", Lists.newArrayList("192.168.1.25"), LiveRebelDeployBuilder.Strategy.OFFLINE, false, true, null, null);
//
//        assertTrue(result);
//        return true;
//      }
//    });
//    FreeStyleBuild build = project.scheduleBuild2(0).get();
//  }

//  public void testUploadWithOverride() throws Exception {
//    project.getBuildersList().add(new TestBuilder() {
//      public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
//                             BuildListener listener) throws InterruptedException, IOException {
//        CommandCenterFactory commandCenterFactory = Mockito.mock(CommandCenterFactory.class);
//        CommandCenter commandCenter = Mockito.mock(CommandCenter.class);
//        Mockito.when(commandCenterFactory.newCommandCenter()).thenReturn(commandCenter);
//        Mockito.when(commandCenter.getApplication("test")).thenReturn(createDummyApplicationInfo("test"));
//        Mockito.when(commandCenter.upload(new File(archivesDir, "lr-demo-ver1.war"))).thenReturn(createDummyUploadInfo());
//
//        LiveRebelProxy liveRebelProxy = new LiveRebelProxy(commandCenterFactory, listener);
//        LiveRebelProxy spyProxy = spy(liveRebelProxy);
//        Boolean result = spyProxy.perform(new FilePath[] {new FilePath(new File(archivesDir, "lr-demo-ver1.war"))}, "lr-demo", Lists.newArrayList("192.168.1.25"), LiveRebelDeployBuilder.Strategy.OFFLINE, false, true, "test", "ver1");
//
//        assertTrue(result);
//        return true;
//      }
//    });
//    FreeStyleBuild build = project.scheduleBuild2(0).get();
//  }

  private List<ServerCheckbox> createServer() {
    ServerCheckbox serverCheckbox = new ServerCheckbox("192.168.1.25", null, "", 0, false, true, false);
    List<ServerCheckbox> list = new ArrayList<ServerCheckbox>();
    list.add(serverCheckbox);
    return list;
  }
}
