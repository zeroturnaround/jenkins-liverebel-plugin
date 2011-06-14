package org.zeroturnaround.jenkins;

import com.zeroturnaround.liverebel.api.*;
import com.zeroturnaround.liverebel.api.Error;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class LiveRebelDeployPublisher extends Notifier implements Serializable {

	private final String artifact;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public LiveRebelDeployPublisher(String artifact) {
		this.artifact = artifact;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getArtifact() {
		return artifact;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		try {
			CommandCenter commandCenter = new CommandCenterFactory().
				setUrl(getDescriptor().getLrUrl()).
				setVerbose(true).
				authenticate(getDescriptor().getAuthToken()).
				newCommandCenter();
				UploadInfo upload = commandCenter.upload(new File(build.getWorkspace().child(artifact).toURI()));
				listener.getLogger().println("SUCCESS: " + upload.getApplicationId() + " " + upload.getVersionId() + " was uploaded.");
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

		private String authToken = "9248c21e-27fc-4d9b-9df3-d7b08c51aee2";
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
			return "Deploy artifacts using LiveRebel.";
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
	}
}

