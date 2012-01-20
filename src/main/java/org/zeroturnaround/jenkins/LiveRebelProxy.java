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
import java.util.ArrayList;
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
import org.zeroturnaround.jenkins.LiveRebelDeployPublisher.Strategy;

/**
 * @author Juri Timoshin
 */
public class LiveRebelProxy {

  public static final String ARTIFACT_DEPLOYED_AND_UPDATED = "SUCCESS. Artifact deployed and activated in all deployableServers servers: %s\n";
  private final CommandCenterFactory commandCenterFactory;
  private final BuildListener listener;
  CommandCenter commandCenter;
  private Strategy strategy;

  public LiveRebelProxy(CommandCenterFactory centerFactory, BuildListener listener) {
    commandCenterFactory = centerFactory;
    this.listener = listener;
  }

  public boolean perform(FilePath[] wars, List<String> deployableServers, Strategy strategy) throws IOException, InterruptedException {
    if (wars.length == 0) {
      listener.getLogger().println("Could not find any artifact to deploy. Please, specify it in job configuration.");
      return false;
    }

    this.strategy = strategy;
    
    if (!initCommandCenter()) {
      return false;
    }
    boolean result = true;

    listener.getLogger().println("Deploying artifacts.");
    for (FilePath warFile : wars) {
      try {
        listener.getLogger().printf("Processing artifact: %s\n", warFile);
        LiveRebelXml lrXml = getLiveRebelXml(warFile);
        ApplicationInfo applicationInfo = commandCenter.getApplication(lrXml.getApplicationId());
        uploadIfNeeded(applicationInfo, lrXml.getVersionId(), warFile);
        result = result && update(lrXml, applicationInfo, warFile, deployableServers);
        if(result) {
          listener.getLogger().printf(ARTIFACT_DEPLOYED_AND_UPDATED, warFile);
        }
      }
      catch (IllegalArgumentException e) {
        listener.getLogger().println("ERROR!");
        e.printStackTrace(listener.getLogger());
        result = false;
      }
      catch (Error e) {
        listener.getLogger().println("ERROR! Unexpected error received from server.");
        listener.getLogger().println();
        listener.getLogger().println("URL: " + e.getURL());
        listener.getLogger().println("Status code: " + e.getStatus());
        listener.getLogger().println("Message: " + e.getMessage());
        result = false;
      }
      catch (ParseException e) {
        listener.getLogger().println("ERROR! Unable to read server response.");
        listener.getLogger().println();
        listener.getLogger().println("Response: " + e.getResponse());
        listener.getLogger().println("Reason: " + e.getMessage());
        result = false;
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
        result = false;
      }
      catch (Throwable t) {
        listener.getLogger().println("ERROR! Unexpected error occured:");
        listener.getLogger().println();
        t.printStackTrace(listener.getLogger());
        result = false;
      }
    }
    return result;
  }

  boolean initCommandCenter() {
    try {
      commandCenter = commandCenterFactory.newCommandCenter();
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

  boolean update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, FilePath warfile,  List<String> deployableServers) throws IOException,
      InterruptedException {
    if (deployableServers.isEmpty()) {
      listener.getLogger().println("No servers specified in LiveRebel configuration.");
      return false;
    }
    listener.getLogger().println("Starting updating application on servers:");
    boolean result = true;
    if (isFirstRelease(applicationInfo)) {
      result = deploy(lrXml, warfile, deployableServers);
    }
    else {
      List<String> diff = new ArrayList<String>(deployableServers);
      diff.removeAll(applicationInfo.getActiveVersionPerServer().keySet());
      for (Map.Entry<String, String> versionWithServer : applicationInfo.getActiveVersionPerServer().entrySet()) {
        if (diff.contains(versionWithServer.getKey())) {
          continue;
        }
        result = result && activate(lrXml, versionWithServer.getKey(), versionWithServer.getValue(), warfile);
      }
      if (!diff.isEmpty()) {
        deploy(lrXml, warfile, diff);
      }
    }

    return result;
  }

  boolean deploy(LiveRebelXml lrXml, FilePath warfile, List<String> serverIds) {
    listener.getLogger().printf("Deploying new application on the %s.\n", serverIds.toString());
    commandCenter.deploy(lrXml.getApplicationId(), lrXml.getVersionId(), null, serverIds);
    listener.getLogger().printf("SUCCESS: Application deployed to %s.\n", serverIds.toString());
    return true;
  }

  boolean activate(LiveRebelXml lrXml, String server, String activeVersion, FilePath warfile) throws IOException,
      InterruptedException {
    listener.getLogger().printf("Server: %s, active version on server: %s.\n", server, activeVersion);

    if (activeVersion.equals(lrXml.getVersionId())) {
      listener.getLogger().println("Current version is already running on server. No need to update.");
      return true;
    }
    else {
      DiffResult diffResult = getDifferences(lrXml, activeVersion);
      listener.getLogger().printf("Activating version %s on %s server.\n", lrXml.getVersionId(), server);
      ConfigurableUpdate update = commandCenter.update(lrXml.getApplicationId(), lrXml.getVersionId());
      if (diffResult.getMaxLevel() == Level.ERROR || diffResult.getMaxLevel() == Level.WARNING) {
        update.enableOffline();
      }
      update.execute();
      listener.getLogger().printf("SUCCESS: Version %s activated on %s server.\n", lrXml.getVersionId(), server);
      return true;
    }
  }

  DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
    DiffResult diffResult = commandCenter.compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);
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
      UploadInfo upload = commandCenter.upload(artifact);
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
    if(lrXml!=null) {
      listener.getLogger().printf("Found LiveRebel xml. Current application is: %s %s.\n", lrXml.getApplicationId(), lrXml.getVersionId());
      if(lrXml.getApplicationId()==null) {
        throw new RuntimeException("application name is not set in liverebel.xml");
      }
      if(lrXml.getVersionId()==null) {
        throw new RuntimeException("application version is not set in liverebel.xml");
      }
      return lrXml;
    } else {
      throw new RuntimeException("Didn't find liverebel.xml");
    }
  }
}
