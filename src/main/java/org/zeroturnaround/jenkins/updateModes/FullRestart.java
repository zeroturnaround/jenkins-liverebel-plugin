package org.zeroturnaround.jenkins.updateModes;

import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.PluginUtil;

import hudson.Extension;

public class FullRestart extends UpdateMode{
  public final int connectionPause;

  public FullRestart() {
    this.connectionPause = PluginUtil.DEFAULT_REQUEST_PAUSE;
  }

  @DataBoundConstructor
  public FullRestart(int connectionPause) {
    this.connectionPause = connectionPause;
  }

  @Override
  public String toString() {
    return "Full restart";
  }

  @Extension
  public static final UpdateMode.UpdateModeDescriptor D = new UpdateMode.UpdateModeDescriptor(FullRestart.class);
}
