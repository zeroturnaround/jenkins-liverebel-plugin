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

import com.zeroturnaround.liverebel.api.ApplicationInfo;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.DuplicationException;
import com.zeroturnaround.liverebel.api.UploadInfo;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Event;
import com.zeroturnaround.liverebel.api.diff.Item;
import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
import com.zeroturnaround.liverebel.util.LiveRebelXml;
import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Juri Timoshin
 */
public class LiveRebelProxy {

	private final CommandCenter commandCenter;
	private final FilePath[] wars;
	private final boolean useCargo;
	private final BuildListener listener;
	private final DeployPluginProxy deployPluginProxy;

	public LiveRebelProxy(CommandCenter center, FilePath[] warFiles, boolean useCargoIfIncompatible, BuildListener listener, DeployPluginProxy deployPluginProxy) {
		commandCenter = center;
		wars = warFiles;
		this.useCargo = useCargoIfIncompatible;
		this.listener = listener;
		this.deployPluginProxy = deployPluginProxy;
	}

	public void performRelease() throws IOException, InterruptedException {
		for (FilePath warFile : wars){
			LiveRebelXml lrXml = getLiveRebelXml(warFile);
			ApplicationInfo applicationInfo = commandCenter.getApplication(lrXml.getApplicationId());
			uploadIfNeeded(applicationInfo, lrXml.getVersionId(), warFile);
			update(lrXml, applicationInfo, warFile);
		}
	}

	private boolean isFirstRelease( ApplicationInfo applicationInfo ){
		return applicationInfo == null;
	}

	private void update(LiveRebelXml lrXml, ApplicationInfo applicationInfo, FilePath warfile) throws IOException, InterruptedException {
		listener.getLogger().println("Starting updating application on servers:");
		if (isFirstRelease(applicationInfo))
			for (String server : commandCenter.getServers().keySet())
				updateOnServer(lrXml, server, "", warfile);
		else
			for (Map.Entry<String, String> versionWithServer : applicationInfo.getActiveVersionPerServer().entrySet())
				updateOnServer(lrXml, versionWithServer.getKey(), versionWithServer.getValue(), warfile);
	}

	private boolean updateOnServer(LiveRebelXml lrXml, String server, String activeVersion, FilePath warfile) throws IOException, InterruptedException {
		if (activeVersion.isEmpty()){
			listener.getLogger().printf("There is no such application on server %s.\n", server);
			return cargoDeploy(warfile);
		}
		else {
			listener.getLogger().printf("Server: %s, active version on server: %s.\n", server, activeVersion);
		
			if (activeVersion.equals(lrXml.getVersionId())){
				listener.getLogger().println("Current version is already running on server. No need to update.");
				return true;
			}
			else {
				DiffResult diffResult = getDifferences(lrXml, activeVersion);
				if (diffResult.getCompatibility().startsWith("compatible")){
					listener.getLogger().printf("Activating version %s on %s server.\n", lrXml.getVersionId(), server );
					commandCenter.update(lrXml.getApplicationId(), lrXml.getVersionId()).execute();
					listener.getLogger().printf("SUCCESS: Version %s activated on %s server.\n", lrXml.getVersionId(), server);
				}
				else {
					return cargoDeploy(warfile);
				}
				return false;
			}
		}
	}

	private boolean cargoDeploy(FilePath warfile) throws IOException, InterruptedException {
		if (useCargo) return deployPluginProxy.cargoDeploy(warfile);
		listener.getLogger().println("Fallback to cargo deploy is disabled. Doing nothing.");
		return false;
	}

	private DiffResult getDifferences(LiveRebelXml lrXml, String activeVersion) {
		DiffResult diffResult = commandCenter.compare(lrXml.getApplicationId(), activeVersion, lrXml.getVersionId(), false);
		listener.getLogger().println("Compatibility: " + diffResult.getCompatibility());
		for (Item item : diffResult.getItems()) {
			listener.getLogger().printf("%s\t%s\t%s\n", item.getDirection(), item.getPath(), item.getElement());
			for (Event event : item.getEvents())
				listener.getLogger().printf(" - %s\t%s\t%s\n", event.getLevel(), event.getDescription(), event.getEffect());
		listener.getLogger().println();
	}
		return diffResult;
	}

	private void uploadIfNeeded(ApplicationInfo applicationInfo, String currentVersion, FilePath warFile) throws IOException, InterruptedException {
		if (applicationInfo == null) return;
		if ( applicationInfo.getVersions().contains(currentVersion)){
			listener.getLogger().println("Current version of application is already uploaded. Skipping upload.");
		}
		else{
			uploadArtifact(new File(warFile.toURI()));
		}
	}

	private boolean uploadArtifact(File artifact) throws IOException, InterruptedException {
		try {
			UploadInfo upload = commandCenter.upload(artifact);
			listener.getLogger().printf("SUCCESS: %s %s was uploaded.\n", upload.getApplicationId(), upload.getVersionId());
			return true;
		}
		catch ( DuplicationException e ){
			listener.getLogger().println(e.getMessage());
			return false;
		}
	}

	private LiveRebelXml getLiveRebelXml(FilePath warFile) throws IOException, InterruptedException {
		LiveRebelXml lrXml = LiveApplicationUtil.findLiveRebelXml(new File(warFile.toURI()));
		listener.getLogger().printf("Found LiveRebel xml. Current application is: %s %s.\n", lrXml.getApplicationId(), lrXml.getVersionId());
		return lrXml;
	}
}
