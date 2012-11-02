package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.jenkins.util.ArtifactAndMetadataDescriptor;
import org.zeroturnaround.liverebel.plugins.ServersUtil;

import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;

public class DeployOrUpdate extends LiveRebelDeployBuilder.ActionWrapper {

  public final String contextPath;
  public final String artifact;
  public final String app;
  public final String ver;
  public final String trace;
  private String contextPathWithEnvVarReplaced;

  public final UpdateStrategiesImpl updateStrategies;
  public final List<ServerCheckbox> servers;
  public final boolean isOverride;

  @DataBoundConstructor
  public DeployOrUpdate(String contextPath, String artifact, String trace, UpdateStrategiesImpl updateStrategies, List<ServerCheckbox> servers, OverrideForm overrideForm) {
    this.artifact = trimToNull(artifact);
    this.trace = trimToNull(trace);
    this.contextPath = trimToNull(contextPath);
    this.updateStrategies = updateStrategies;
    this.servers = servers;
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

  public String getContextPath() {
    return contextPath;
  }

  public String getContextPathWithEnv() {
    if (contextPathWithEnvVarReplaced == null) {
      return contextPath;
    }
    else {
      return contextPathWithEnvVarReplaced;
    }
  }

  public void setContextPathWithEnvVarReplaced(String contextPathWithEnvVarReplaced) {
    this.contextPathWithEnvVarReplaced = contextPathWithEnvVarReplaced;
  }

  @Override
  public String toString() {
    return "{ contextPath=" + contextPath + "; updateStrategies=" + updateStrategies + "; servers=" + servers + " }";
  }

  public List<ServerCheckbox> getServers() {
    return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), serverCheckBoxToServer(servers)).getServers());
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class DescriptorImpl extends ArtifactAndMetadataDescriptor<LiveRebelDeployBuilder.ActionWrapper> {

    @Override
    public String getDisplayName() {
      return "Deploy or update";
    }

    public List<ServerCheckbox> getDefaultServers() {
      return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getDefaultServers());
    }

    public String getUniqueId() {
      return UUID.randomUUID().toString();
    }
  }

}
