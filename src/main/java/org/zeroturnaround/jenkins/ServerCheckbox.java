package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;


import hudson.model.Hudson;

import org.kohsuke.stapler.DataBoundConstructor;

public class ServerCheckbox implements Describable<ServerCheckbox> {

  private final String title;
  private final boolean selected;
  private final String server;
  private final boolean online;

  @DataBoundConstructor
  public ServerCheckbox(final String server, final String title, final boolean selected, final boolean online) {
    this.server = server;
    this.online = online;
    this.title = title;
    this.selected = selected;
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

  public String getServer() {
    return server;
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<ServerCheckbox> {

    @Override
    public String getDisplayName() {
      return "Server checkbox";
    }
  }
}