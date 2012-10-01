package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.jenkins.util.ArtifactAndMetadataDescriptor;
import org.zeroturnaround.liverebel.plugins.ServersUtil;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;

public class Upload extends LiveRebelDeployBuilder.ActionWrapper {

  public final String artifact;
  public final String app;
  public final String ver;
  public final String metadata;
  public final boolean isOverride;

  @DataBoundConstructor
  public Upload(String artifact, String metadata, OverrideForm overrideForm) {
    this.artifact = trimToNull(artifact);

    this.metadata = trimToNull(metadata);
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
