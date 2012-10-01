package org.zeroturnaround.jenkins.updateModes;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;


public class RollingRestarts extends UpdateMode {
  public final int sessionDrain;
  public RollingRestarts() {sessionDrain = 0;}
  @DataBoundConstructor
  public RollingRestarts(int sessionDrain) {
    this.sessionDrain = sessionDrain;
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(RollingRestarts.class);

  @Override
  public String toString() {
    return "Rolling Restarts";
  }
}
