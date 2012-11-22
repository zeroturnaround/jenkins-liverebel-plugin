package org.zeroturnaround.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.ServersUtil;
import com.zeroturnaround.liverebel.api.Forbidden;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.util.FormValidation;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverCheckBoxToServer;
import static org.zeroturnaround.jenkins.util.ServerConvertUtil.serverToServerCheckBox;

public class Undeploy extends LiveRebelDeployBuilder.ActionWrapper {

  public final List<ServerCheckbox> servers;
  public final String undeployID;

  @DataBoundConstructor
  public Undeploy(List<ServerCheckbox> servers, String undeployID) {
    this.servers = servers == null ? new ArrayList<ServerCheckbox>() : servers;
    this.undeployID = trimToNull(undeployID);
  }

  public List<ServerCheckbox> getServers() {
    return serverToServerCheckBox(new ServersUtil(LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter(), serverCheckBoxToServer(servers == null ? new ArrayList<ServerCheckbox>() : servers)).getServers());
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

    public FormValidation doCheckTestServers() throws IOException, ServletException {
      try {
        List<ServerCheckbox> availableServers = getDefaultServers();
        if (availableServers.isEmpty()) return FormValidation.error("No connected servers!");
        boolean anyOnline = false;
        for (ServerCheckbox serverCheckbox : availableServers) {
          if (serverCheckbox.isConnected()) anyOnline = true;
        }
        if (!anyOnline) return FormValidation.warning("No online servers!");
      } catch (Forbidden e) {
        if (e.getMessage().contains("MANAGE_GROUPS")) {
          return FormValidation.error("User whose authentication token is used must have MANAGE_GROUPS permission!");
        } else throw e;
      }
      return FormValidation.ok();
    }
  }
}
