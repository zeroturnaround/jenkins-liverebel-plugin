package org.zeroturnaround.jenkins;

import com.google.common.collect.Lists;
import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.ServerInfo;
import com.zeroturnaround.liverebel.api.impl.ServerGroup;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DeployOrDistribute implements Describable<DeployOrDistribute> {

  public final String contextPath;

  private String contextPathWithEnvVarReplaced;

  public final String value;
  public final UpdateStrategiesImpl updateStrategies;
  public final List<ServerCheckbox> servers;
  public final boolean distribute;
  public final boolean deployOrUpdate;
  private static final Logger LOGGER = Logger.getLogger(DeployOrDistribute.class.getName());

  @DataBoundConstructor
  public DeployOrDistribute(String contextPath, String value, UpdateStrategiesImpl updateStrategies, List<ServerCheckbox> servers) {
    this.contextPath = contextPath;
    this.value = value;
    this.updateStrategies = updateStrategies;
    this.servers = servers;

    distribute = false;
    deployOrUpdate = false;
  }

  public String getContextPath() {
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

  public boolean isDistribute() {
    return value.equals("distribute");
  }

  @Override
  public String toString() {
    return "{ contextPath=" + contextPath + "; value=" + value + "; updateStrategies=" + updateStrategies + "; servers=" + servers + " }";
  }

  public List<ServerCheckbox> getServers() {
    if (servers == null) {
      return getDescriptor().getDefaultServers();
    }
    else {
      List<ServerCheckbox> newServers = getDescriptor().getDefaultServers();
      Map<String, ServerCheckbox> oldServersMap = new HashMap<String, ServerCheckbox>();

      for (ServerCheckbox oldServer : servers) {
        oldServersMap.put(oldServer.getServer(), oldServer);
      }

      servers.clear();
      for(ServerCheckbox newServer : newServers) {
        if (oldServersMap.containsKey(newServer.getServer()))
          servers.add(new ServerCheckbox(newServer.getServer(), newServer.getTitle(),newServer.getParentNames(), newServer.getIndentDepth(), oldServersMap.get(newServer.getServer()).isSelected(), newServer.isOnline(), newServer.isGroup()));
        else
          servers.add(newServer);
      }
      return servers;
    }
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<DeployOrDistribute> {

    @Override
    public String getDisplayName() {
      return "Deploy or distribute";
    }

    public List<ServerCheckbox> getDefaultServers() {
      CommandCenter commandCenter = LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter();
      List<ServerCheckbox> serversLoc = new ArrayList<ServerCheckbox>();
      if (commandCenter != null) {
        String currentVersion = commandCenter.getVersion();
        if (isServerGroupsSupported(currentVersion)) {
          serversLoc = showServerGroups(commandCenter);
        }
        else {
          serversLoc = showServers(commandCenter);
        }
      }
      else {
        LOGGER.warning("Couldn't connect to the command center!");
      }
      return serversLoc;
    }

    private boolean isServerGroupsSupported(String currentVersion) {return !currentVersion.equals("2.0");}

    private List<ServerCheckbox> showServerGroups(CommandCenter commandCenter) {
      List<ServerGroup> topLevelServerGroups = commandCenter.getGroups("");
      List<ServerCheckbox> allCheckBoxes = new ArrayList<ServerCheckbox>();
      for (ServerGroup serverGroup : topLevelServerGroups) {
        if (hasServers(serverGroup).contains(true)) //do not add empty groups
          allCheckBoxes.addAll(processSiblings(serverGroup, "", 0));
      }

      return allCheckBoxes;
    }

    private List<Boolean> hasServers(ServerGroup serverGroup) {
      if (!serverGroup.getServers().isEmpty()) {
        return Lists.newArrayList(true);
      }
      ArrayList<Boolean> hasServers = new ArrayList<Boolean>();
      for (ServerGroup child : serverGroup.getChildren()) {
        hasServers.addAll(hasServers(child));
      }
      return hasServers;
    }

    private List<ServerCheckbox> processSiblings(ServerGroup serverGroup, String parentNames, int indentDepth) {
      ServerCheckbox serverCheckbox = new ServerCheckbox(serverGroup.getName(), serverGroup.getName(), parentNames, indentDepth, false, false, true);
      ArrayList<ServerCheckbox> serverCheckboxes = new ArrayList<ServerCheckbox>();
      serverCheckboxes.add(serverCheckbox);
      if (serverGroup.getChildren().size() != 0) {
        for (ServerGroup child : serverGroup.getChildren()) {
          if (hasServers(child).contains(true)) //do not add empty groups
            serverCheckboxes.addAll(processSiblings(child, "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1));
        }
      }

      if (serverGroup.getServers().size() != 0) {
        for (ServerInfo server : serverGroup.getServers()) {
          serverCheckboxes.add(new ServerCheckbox(server.getId(), server.getName(), "lr-" + serverGroup.getName().replaceAll("[^A-Za-z0-9]", "_"), indentDepth + 1, false, server.isConnected(), false));
        }
      }

      return serverCheckboxes;
    }

    private List<ServerCheckbox> showServers(CommandCenter commandCenter) {
      List<ServerCheckbox> servers = new ArrayList<ServerCheckbox>();
      for (ServerInfo server : commandCenter.getServers().values()) {
        servers.add(new ServerCheckbox(server.getId(), server.getName(), "", 0, false, server.isConnected(), false));
      }
      return servers;
    }
  }
}
