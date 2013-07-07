package org.zeroturnaround.jenkins.updateModes;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.PluginUtil;

public class AllAtOnce extends UpdateMode {

  public final int connectionPause;

  public AllAtOnce() {
    this.connectionPause = PluginUtil.DEFAULT_REQUEST_PAUSE;
  }

  @DataBoundConstructor
  public AllAtOnce(int connectionPause) {
    this.connectionPause = connectionPause;
  }

  @Override
  public String toString() {
    return "All at once (only for non-Java applications)";
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(AllAtOnce.class);
}
