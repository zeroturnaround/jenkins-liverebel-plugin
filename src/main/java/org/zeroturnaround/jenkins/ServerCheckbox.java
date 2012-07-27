package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;


import hudson.model.Hudson;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class ServerCheckbox implements Describable<ServerCheckbox> {

  private final String title;
  private final String server;
  private final String parentNames;

  private final int indentDepth;

  private final boolean selected;

  private final boolean online;
  private final boolean isGroup;
  @DataBoundConstructor
  public ServerCheckbox(String server, String title, String parentNames, int indentDepth, boolean selected, boolean online, boolean isGroup) {
    this.server = server;
    this.parentNames = parentNames;
    this.indentDepth = indentDepth;
    this.online = online;
    this.title = title;
    this.selected = selected;
    this.isGroup = isGroup;
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
    return server;
  }

  public boolean isOnline() {
    return online;
  }

  public String getTitle() {
    return title;
  }

  public boolean isSelected() {
    return selected;
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Override
  public String toString() {
    return "{ GROUP_NAME="+getTitle()+" selected="+isSelected()+" isGroup="+isGroup() + " online="+isOnline() + " parentNames="+parentNames+" }";
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<ServerCheckbox> {

    @Override
    public String getDisplayName() {
      return "Server checkbox";
    }
  }
}