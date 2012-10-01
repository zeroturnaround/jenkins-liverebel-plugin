package org.zeroturnaround.jenkins.updateModes;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class LiveRebelDefault extends UpdateMode{

  @DataBoundConstructor
  public LiveRebelDefault() {}
  @Override
  public String toString() {
    return "LiveRebel default";
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(LiveRebelDefault.class);
}
