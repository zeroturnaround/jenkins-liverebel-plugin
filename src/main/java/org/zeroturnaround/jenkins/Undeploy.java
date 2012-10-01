package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.ServersUtil;

import java.util.List;
import java.util.UUID;


import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;

public class Undeploy extends LiveRebelDeployBuilder.ActionWrapper {

  public final List<ServerCheckbox> servers;
  public final String undeployID;

  @DataBoundConstructor
  public Undeploy(List<ServerCheckbox> servers, String undeployID) {
    this.servers = servers;
    this.undeployID = trimToNull(undeployID);
  }

  public List<ServerCheckbox> getServers() {
    return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), serverCheckBoxToServer(servers)).getServers());
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Override
  public String toString() {
    return "{ servers=" + servers + " }";
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<LiveRebelDeployBuilder.ActionWrapper> {

    @Override
    public String getDisplayName() {
      return "Undeploy";
    }

    public List<ServerCheckbox> getDefaultServers() {
      return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), null).getDefaultServers());
    }

    public String getUniqueId() {
      return UUID.randomUUID().toString();
    }
  }
}
