package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.jenkins.util.ServersUtil;

import java.util.List;

public class Undeploy implements Describable<Undeploy> {

  public final List<ServerCheckbox> servers;

  @DataBoundConstructor
  public Undeploy(List<ServerCheckbox> servers) {this.servers = servers;}

  public List<ServerCheckbox> getServers() {
    return new ServersUtil(servers).getServers();
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Override
  public String toString() {
    return "{ servers=" + servers + " }";
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<Undeploy> {

    @Override
    public String getDisplayName() {
      return "Undeploy";
    }

    public List<ServerCheckbox> getDefaultServers() {
      return new ServersUtil(null).getDefaultServers();
    }
  }
}
