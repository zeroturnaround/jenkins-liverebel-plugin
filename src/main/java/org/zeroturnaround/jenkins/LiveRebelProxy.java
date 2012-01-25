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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.ConnectException;
import com.zeroturnaround.liverebel.api.DuplicationException;
import com.zeroturnaround.liverebel.api.Error;
import com.zeroturnaround.liverebel.api.Forbidden;
import com.zeroturnaround.liverebel.api.ParseException;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Level;
import com.zeroturnaround.liverebel.api.update.ConfigurableUpdate;
import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import java.util.HashSet;
import java.util.Set;
import org.zeroturnaround.jenkins.LiveRebelDeployPublisher.Strategy;

/**
 * @author Juri Timoshin
 */
public class LiveRebelProxy {

  public static final String ARTIFACT_DEPLOYED_AND_UPDATED = "SUCCESS. Artifact deployed and activated in all %s servers: %s\n";
  private final CommandCenterFactory commandCenterFactory;
  private final BuildListener listener;
  private CommandCenter commandCenter;
  private Strategy strategy;
  private boolean useFallbackIfCompatibleWithWarnings;

  public LiveRebelProxy(CommandCenterFactory centerFactory, BuildListener listener) {
    commandCenterFactory = centerFactory;
    this.listener = listener;
  }

  public boolean perform(FilePath[] wars, List<String> deployableServers, Strategy strategy, boolean useFallbackIfCompatibleWithWarnings) throws IOException, InterruptedException {
    if (wars.length == 0) {
      listener.getLogger().println("Could not find any artifact to deploy. Please, specify it in job configuration.");
      return false;
    }

    if (deployableServers.isEmpty()) {
      listener.getLogger().println("No servers specified in LiveRebel configuration.");
      return false;
    }

    this.strategy = strategy;
    this.useFallbackIfCompatibleWithWarnings = useFallbackIfCompatibleWithWarnings;

    if (!initCommandCenter()) {
      return false;
    }

    listener.getLogger().println("Deploying artifacts.");
    for (FilePath warFile : wars) {
      boolean result = false;
      try {
        listener.getLogger().printf("Processing artifact: %s\n", warFile);
        LiveRebelXml lrXml = getLiveRebelXml(warFile);
        ApplicationInfo applicationInfo = getCommandCenter().getApplication(lrXml.getApplicationId());
        uploadIfNeeded(applicationInfo, lrXml.getVersionId(), warFile);
        update(lrXml, applicationInfo, warFile, deployableServers);
        listener.getLogger().printf(ARTIFACT_DEPLOYED_AND_UPDATED, deployableServers, warFile);
        result = true;
      }
      catch (IllegalArgumentException e) {
        listener.getLogger().println("ERROR!");
        e.printStackTrace(listener.getLogger());
      }
      catch (Error e) {
        listener.getLogger().println("ERROR! Unexpected error received from server.");
        listener.getLogger().println();
        listener.getLogger().println("URL: " + e.getURL());
        listener.getLogger().println("Status code: " + e.getStatus());
        listener.getLogger().println("Message: " + e.getMessage());
      }
      catch (ParseException e) {
        listener.getLogger().println("ERROR! Unable to read server response.");
        listener.getLogger().println();
        listener.getLogger().println("Response: " + e.getResponse());
        listener.getLogger().println("Reason: " + e.getMessage());
      }
      catch (RuntimeException e) {
        if (e.getCause() instanceof ZipException) {
          listener.getLogger().printf(
              "ERROR! Unable to read artifact (%s). The file you trying to deploy is not an artifact or may be corrupted.\n",
              warFile);
        }
        else {
          listener.getLogger().println("ERROR! Unexpected error occured:");
          listener.getLogger().println();
          e.printStackTrace(listener.getLogger());
        }
      }
      catch (Throwable t) {
        listener.getLogger().println("ERROR! Unexpected error occured:");
        listener.getLogger().println();
        t.printStackTrace(listener.getLogger());
      }
      if (!result)
        return result;
    }
    return true;
  }

  boolean initCommandCenter() {
    try {
      this.commandCenter = commandCenterFactory.newCommandCenter();
      return true;
    }
    catch (Forbidden e) {
      listener.getLogger().println(
          "ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
      return false;
    }
    catch (ConnectException e) {
      listener.getLogger().println("ERROR! Unable to connect to server.");
      listener.getLogger().println();
      listener.getLogger().println("URL: " + e.getURL());
      if (e.getURL().equals("https://")) {
        listener.getLogger().println("Please, navigate to Jenkins Configuration to specify running LiveRebel Url.");
      }
      else {
        listener.getLogger().println("Reason: " + e.getMessage());
      }
      return false;
    }
  }

  boolean isFirstRelease(ApplicationInfo applicationInfo) {
    return applicationInfo == null;
  }

  void update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, FilePath warFile, List<String> selectedServers) throws IOException,
      InterruptedException {
    listener.getLogger().println("Starting updating application on servers:");

    Set<String> deployServers = getDeployServers(applicationInfo, selectedServers);
    if (!deployServers.isEmpty()) {
      deploy(lrXml, warFile, deployServers);
    }

    if (deployServers.size() != selectedServers.size()) {
      Set<String> activateServers = new HashSet<String>(selectedServers);
      activateServers.removeAll(deployServers);

      Level diffLevel = getMaxDifferenceLevel(applicationInfo, lrXml, activateServers);

      activate(lrXml, warFile, activateServers, diffLevel);
    }
  }

  void deploy(LiveRebelXml lrXml, FilePath warfile, Set<String> serverIds) {
    listener.getLogger().printf("Deploying new application on %s.\n", serverIds);
    getCommandCenter().deploy(lrXml.getApplicationId(), lrXml.getVersionId(), null, serverIds);
    listener.getLogger().printf("SUCCESS: Application deployed to %s.\n", serverIds);
  }

  void activate(LiveRebelXml lrXml, FilePath warfile, Set<String> serverIds, Level diffLevel) throws IOException,
      InterruptedException {
    ConfigurableUpdate update = getCommandCenter().update(lrXml.getApplicationId(), lrXml.getVersionId());
    if (diffLevel == Level.ERROR || diffLevel == Level.WARNING && useFallbackIfCompatibleWithWarnings) {
      if (strategy == Strategy.OFFLINE)
        update.enableOffline();
      else if (strategy == Strategy.ROLLING)
        update.enableRolling();
    }
    update.on(serverIds);
    update.execute();
  }

  DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
    DiffResult diffResult = getCommandCenter().compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);
    diffResult.print(listener.getLogger());
    listener.getLogger().println();
    return diffResult;
  }

  void uploadIfNeeded(ApplicationInfo applicationInfo, String currentVersion, FilePath warFile) throws IOException,
      InterruptedException {
    if (applicationInfo != null && applicationInfo.getVersions().contains(currentVersion)) {
      listener.getLogger().println("Current version of application is already uploaded. Skipping upload.");
    }
    else {
      uploadArtifact(new File(warFile.getRemote()));
      listener.getLogger().printf("Artifact uploaded: %s\n", warFile);
    }
  }

  boolean uploadArtifact(File artifact) throws IOException, InterruptedException {
    try {
      UploadInfo upload = getCommandCenter().upload(artifact);
      listener.getLogger().printf("SUCCESS: %s %s was uploaded.\n", upload.getApplicationId(), upload.getVersionId());
      return true;
    }
    catch (DuplicationException e) {
      listener.getLogger().println(e.getMessage());
      return false;
    }
  }

  LiveRebelXml getLiveRebelXml(FilePath warFile) throws IOException, InterruptedException {
    LiveRebelXml lrXml = LiveApplicationUtil.findLiveRebelXml(new File(warFile.getRemote()));
    if (lrXml != null) {
      listener.getLogger().printf("Found LiveRebel xml. Current application is: %s %s.\n", lrXml.getApplicationId(), lrXml.getVersionId());
      if (lrXml.getApplicationId() == null) {
        throw new RuntimeException("application name is not set in liverebel.xml");
      }
      if (lrXml.getVersionId() == null) {
        throw new RuntimeException("application version is not set in liverebel.xml");
      }
      return lrXml;
    }
    else {
      throw new RuntimeException("Didn't find liverebel.xml");
    }
  }

  Set<String> getDeployServers(ApplicationInfo applicationInfo, List<String> selectedServers) {
    Set<String> deployServers = new HashSet<String>();

    if (isFirstRelease(applicationInfo)) {
      deployServers.addAll(selectedServers);
      return deployServers;
    }

    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();

    for (String server : selectedServers) {
      if (!activeVersions.containsKey(server))
        deployServers.add(server);
    }
    return deployServers;
  }

  private Level getMaxDifferenceLevel(ApplicationInfo applicationInfo, LiveRebelXml lrXml, Set<String> activateServers) {
    Map<String, String> activeVersions = applicationInfo.getActiveVersionPerServer();
    activeVersions.keySet().retainAll(activateServers);

    Set<String> diffVersions = new HashSet<String>(activeVersions.values());
    Level diffLevel = Level.NOP;

    for (String version : diffVersions) {
      DiffResult differences = getDifferences(lrXml, version);
      if (differences.getMaxLevel().compareTo(diffLevel) > 0) {
        diffLevel = differences.getMaxLevel();
      }
      if (diffLevel.compareTo(Level.ERROR) == 0)
        break;
    }
    return diffLevel;
  }

  /**
   * @return the commandCenter
   */
  public CommandCenter getCommandCenter() {
    return commandCenter;
  }
}
