package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.jenkins.util.ArtifactAndMetadataDescriptor;

import static org.apache.commons.lang.StringUtils.trimToNull;

public class Upload extends LiveRebelDeployBuilder.ActionWrapper {

  public final String artifact;
  public final String app;
  public final String ver;
  public final String trace;
  public final boolean isOverride;

  @DataBoundConstructor
  public Upload(String artifact, String trace, OverrideForm overrideForm) {
    this.artifact = trimToNull(artifact);

    this.trace = trimToNull(trace);
    if (overrideForm != null) {
      this.app = trimToNull(overrideForm.getApp());
      this.ver = trimToNull(overrideForm.getVer());
      this.isOverride = true;
    } else {
      this.app = null;
      this.ver = null;
      this.isOverride = false;
    }

  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class DescriptorImpl extends ArtifactAndMetadataDescriptor<LiveRebelDeployBuilder.ActionWrapper> {

    @Override
    public String getDisplayName() {
      return "Upload";
    }

  }

}
