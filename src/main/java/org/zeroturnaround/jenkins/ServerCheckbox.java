package org.zeroturnaround.jenkins;

import java.util.Set;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.Server;

import com.zeroturnaround.liverebel.util.ServerKind;

public class ServerCheckbox implements Describable<ServerCheckbox>, Server {

  private final String title;
  private final String id;
  private final String parentNames;

  private final int indentDepth;

  private final boolean checked;

  private final boolean connected;
  private final boolean isGroup;

  private final ServerKind type;
  private final Set<String> virtualHostNames;
  private final String defaultVirtualHostName;
  private final boolean virtualHostsSupported;

  @DataBoundConstructor
  public ServerCheckbox(String server, String title, String parentNames, int indentDepth, boolean selected, boolean online, boolean isGroup, ServerKind type,
                        boolean virtualHostsSupported, String defaultVirtualHostName, Set<String> virtualHostNames) {
    this.id = server;
    this.parentNames = parentNames;
    this.indentDepth = indentDepth;
    this.connected = online;
    this.title = title;
    this.checked = selected;
    this.isGroup = isGroup;
    this.type = type;
    this.virtualHostsSupported = virtualHostsSupported;
    this.defaultVirtualHostName = defaultVirtualHostName;
    this.virtualHostNames = virtualHostNames;
  }

  public String getIndentDepthAsCSSClass() {
    switch (indentDepth) {
      case 0:
        return "topLevel";
      case 1:
        return "firstLevel";
      case 2:
        return "secondLevel";
      case 3:
        return "thirdLevel";
      default:
        return "";
    }
  }

  public int getIndentDepth() {
    return indentDepth;
  }

  public String getParentNames() {
    return StringUtils.trimToEmpty(parentNames);
  }

  public boolean isGroup() {
    return isGroup;
  }

  public String getServer() {
    return id;
  }

  public boolean isOnline() {
    return connected;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    throw new UnsupportedOperationException("setTitle");
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    throw new UnsupportedOperationException("setId");
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    throw new UnsupportedOperationException("setConnected");
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    throw new UnsupportedOperationException("setChecked");
  }

  public boolean isSelected() {
    return checked;
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Override
  public String toString() {
    return "{ GROUP_NAME="+getTitle()+" checked="+isSelected()+" isGroup="+isGroup() + " connected="+isOnline() + " parentNames="+parentNames+" }";
  }

  public ServerKind getType() {
    return type;
  }

  public void setType(String type) {
    throw new UnsupportedOperationException("setType");
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<ServerCheckbox> {

    @Override
    public String getDisplayName() {
      return "Server checkbox";
    }
  }

  public Set<String> getVirtualHostNames() {
    return virtualHostNames;
  }

  public String getDefaultVirtualHostName() {
    return defaultVirtualHostName;
  }

  public boolean isVirtualHostsSupported() {
    return virtualHostsSupported;
  }
}