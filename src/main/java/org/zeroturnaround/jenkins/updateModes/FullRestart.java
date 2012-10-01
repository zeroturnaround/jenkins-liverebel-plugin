package org.zeroturnaround.jenkins.updateModes;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class FullRestart extends UpdateMode{

  @DataBoundConstructor
  public FullRestart() {}
  @Override
  public String toString() {
    return "Full restart";
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(FullRestart.class);
}
