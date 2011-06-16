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

import com.zeroturnaround.liverebel.api.*;
import com.zeroturnaround.liverebel.api.Error;
import com.zeroturnaround.liverebel.api.diff.DiffResult;
import com.zeroturnaround.liverebel.api.diff.Event;
import com.zeroturnaround.liverebel.api.diff.Item;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.plugins.deploy.ContainerAdapter;
import hudson.plugins.deploy.ContainerAdapterDescriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class LiveRebelDeployPublisher extends Notifier implements Serializable {

	public final String war;
	public final ContainerAdapter adapter;
	private String applicationId;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public LiveRebelDeployPublisher(String war, ContainerAdapter adapter) {
		this.war = war;
		this.adapter = adapter;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		try {
			CommandCenter commandCenter = new CommandCenterFactory().
				setUrl(getDescriptor().getLrUrl()).
				setVerbose(true).
				authenticate(getDescriptor().getAuthToken()).
				newCommandCenter();

			for (FilePath warFile : build.getWorkspace().list(war)){
				UploadInfo upload = uploadArtifact(commandCenter, new File(warFile.toURI()), listener);
				applicationId = upload.getApplicationId();
				String activeVersion = getApplicationCurrentVersion(commandCenter, listener);


				if (activeVersion.equals(upload.getVersionId())){
					listener.getLogger().println("You are trying to upload the same version. Not doing anything.");
					return true;
				}

				DiffResult diffResult = compareVersions(activeVersion, upload.getVersionId(), commandCenter, listener);

				if (diffResult.getCompatibility().startsWith("compatible")){
					updateApplication(upload.getVersionId(), commandCenter, listener);
				}
				else {
					listener.getLogger().println("Deploying new artifact without LiveRebel...");
					return adapter.redeploy(warFile, build, launcher, listener);
				}
			}

		}
		catch (IllegalArgumentException e) {
			listener.getLogger().println("ERROR! " + e.getMessage());
			return false;
		}
		catch (Conflict e) {
			listener.getLogger().println("ERROR! " + e.getMessage());
			return false;
		}
		catch (Forbidden e) {
			listener.getLogger().println("ERROR! Access denied, invalid username or password.");
			return false;
		}
		catch (Error e) {
			listener.getLogger().println("ERROR! Unexpected error received from server.");
			listener.getLogger().println();
			listener.getLogger().println("URL: " + e.getURL());
			listener.getLogger().println("Status code: " + e.getStatus());
			listener.getLogger().println("Nessage: " + e.getMessage());
			return false;
		}
		catch (ParseException e) {
			listener.getLogger().println("ERROR! Unable to read server response.");
			listener.getLogger().println();
			listener.getLogger().println("Response: " + e.getResponse());
			listener.getLogger().println("Reason: " + e.getMessage());
			return false;
		}
		catch (ConnectException e) {
			listener.getLogger().println("ERROR! Unable to connect to server.");
			listener.getLogger().println();
			listener.getLogger().println("URL: " + e.getURL());
			listener.getLogger().println("Reason: " + e.getMessage());
			return false;
		}
		catch (Throwable t) {
			listener.getLogger().println("ERROR! Unexpected error occured:");
			listener.getLogger().println();
			t.printStackTrace(listener.getLogger());
			return false;
		}
		return true;
	}

	private void updateApplication(String newVersion, CommandCenter commandCenter, BuildListener listener) {
		listener.getLogger().println("Activating version "+ newVersion);
		commandCenter.update(applicationId, newVersion).execute();
		listener.getLogger().printf("Version %s activated.\n", newVersion);
	}

	private DiffResult compareVersions(String activeVersion, String newVersion, CommandCenter commandCenter, BuildListener listener) {
		DiffResult diffResult = commandCenter.compare(applicationId, activeVersion, newVersion, false);
		listener.getLogger().println("Compatibility: " + diffResult.getCompatibility());
		for (Iterator it = diffResult.getItems().iterator(); it.hasNext();) {
			Item item = (Item) it.next();
			listener.getLogger().printf("%s\t%s\t%s\n", item.getDirection(), item.getPath(), item.getElement());
			for (Event event : item.getEvents())
				listener.getLogger().printf(" - %s\t%s\t%s\n", event.getLevel(), event.getDescription(), event.getEffect());
			if (it.hasNext())
				listener.getLogger().println();
		}
		return diffResult;
	}

	private String getApplicationCurrentVersion(CommandCenter commandCenter, BuildListener listener) {
		ApplicationInfo applicationInfo = commandCenter.getApplication(applicationId);
		String activeVersion = StringUtils.join(applicationInfo.getActiveVersions(), ", ");
		listener.getLogger().printf("Currently active version: %s\n", activeVersion);
		return activeVersion;
	}

	private UploadInfo uploadArtifact(CommandCenter commandCenter, File artifact, BuildListener listener) throws IOException, InterruptedException {
		UploadInfo upload = commandCenter.upload(artifact);
		listener.getLogger().printf("SUCCESS: %s %s was uploaded.\n", upload.getApplicationId(), upload.getVersionId());
		return upload;
	}

	// Overridden for better type safety.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private String authToken;
		private String lrUrl = "https://localhost:9001";

		public FormValidation doCheckArtifact(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please, set artifact");
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Deploy artifacts with LiveRebel";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			authToken = formData.getString("authToken");
			lrUrl = "https://" + formData.getString("lrUrl").replaceFirst("http://", "").replaceFirst("https://", "");
			save();
			return super.configure(req,formData);
		}

		public String getAuthToken() {
			return authToken;
		}
		public String getLrUrl(){
			return lrUrl;
		}

		public List<ContainerAdapterDescriptor> getContainerAdapters() {
			List<ContainerAdapterDescriptor> r = new ArrayList<ContainerAdapterDescriptor>(ContainerAdapter.all());
			Collections.sort(r, new Comparator<ContainerAdapterDescriptor>() {
				public int compare(ContainerAdapterDescriptor o1, ContainerAdapterDescriptor o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}
			});
			return r;
		}
	}
}

