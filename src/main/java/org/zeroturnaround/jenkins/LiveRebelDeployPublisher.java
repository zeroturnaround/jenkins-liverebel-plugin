package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class LiveRebelDeployPublisher extends Notifier implements Serializable {

	private final String artifact;
	private final String appName;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public LiveRebelDeployPublisher(String artifact, String appName) {
		this.artifact = artifact;
		this.appName = appName;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getArtifact() {
		return artifact;
	}
	public String getAppName() {
		return appName;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		try {
			


			Process proc=Runtime.getRuntime().exec("/home/jt/zt/Test/jenkins-plugin/liverebel-plugin/upload-activate-version.sh /home/jt/zt/Test/jenkins-plugin/liverebel-deploy-plugin/work/jobs/lr-demo-app/workspace/"+artifact+" "+ appName);
			BufferedReader read=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			proc.waitFor();
			while(read.ready()) {
				listener.getLogger().println(read.readLine());
			}
		}
		catch (IOException e){
			listener.getLogger().println(e.getStackTrace().toString());
			return false;
		}
		catch (InterruptedException e) {
			listener.getLogger().println(e.getStackTrace().toString());
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

		private String authToken;
		private String lrRunningAddress;

		public FormValidation doCheckAppName(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set application name");
			return FormValidation.ok();
		}

		public FormValidation doCheckArtifact(@QueryParameter String value) throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set artifact");
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
			lrRunningAddress = formData.getString("lrRunningAddress");
			save();
			return super.configure(req,formData);
		}

		public String getAuthToken() {
			return authToken;
		}
		public String getLRRunningAddress(){
			return lrRunningAddress;
		}
	}
}

