package org.zeroturnaround.jenkins.updateModes;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

public class FailBuild extends UpdateMode{

  @DataBoundConstructor
  public FailBuild() {}

  @Override
  public String toString() {
    return "Fail Build";
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(FailBuild.class);
}
