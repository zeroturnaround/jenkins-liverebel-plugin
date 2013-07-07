package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

public class OverrideForm implements Describable<OverrideForm>{
  private String app;
  private String ver;

  @DataBoundConstructor
  public OverrideForm(final String app, final String ver) {
    this.ver = ver;
    this.app = app;
  }

  public String getApp() {
    return app;
  }

  public String getVer() {
    return ver;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public void setVer(String ver) {
    this.ver = ver;
  }

  public OverrideDescriptorImpl getDescriptor() {
    return (OverrideDescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class OverrideDescriptorImpl extends Descriptor<OverrideForm> {

    @Override
    public String getDisplayName() {
      return "Override form";
    }
  }
}
