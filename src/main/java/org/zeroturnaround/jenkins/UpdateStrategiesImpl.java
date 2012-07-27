package org.zeroturnaround.jenkins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.liverebel.plugins.UpdateStrategies;

public class UpdateStrategiesImpl implements Describable<UpdateStrategiesImpl>, UpdateStrategies {

  public final boolean hotpatch;
  public final boolean updateWithWarnings;
  public final int requestPause;

  public final boolean rolling;

  public final int sessionDrain;
  public final boolean fullRestart;

  @DataBoundConstructor
  public UpdateStrategiesImpl(boolean hotpatch, boolean updateWithWarnings, int requestPause, boolean rolling, int sessionDrain, boolean fullRestart) {
    this.hotpatch = hotpatch;
    this.updateWithWarnings = updateWithWarnings;
    this.requestPause = requestPause;
    this.rolling = rolling;
    this.sessionDrain = sessionDrain;
    this.fullRestart = fullRestart;
  }

  @Override
  public String toString() {
    return "{ hotpatch="+hotpatch+"; updateWithWarnings="+updateWithWarnings+
      "; requestPause="+requestPause+"; sessionDrain="+sessionDrain+"; rolling="+rolling+"; fullRestart="+fullRestart+"}";
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  public boolean isHotpatch() {
    return hotpatch;
  }

  public boolean isFullRestart() {
    return fullRestart;
  }

  public boolean isRolling() {
    return rolling;
  }

  public boolean updateWithWarnings() {
    return updateWithWarnings;
  }

  public boolean isDefault() {
    if (!isFullRestart() && !isHotpatch() && !isRolling())
      return true;
    else
      return false;
  }

  public int getSessionDrainTimeout() {
    return sessionDrain;
  }

  public int getRequestPauseTimeout() {
    return requestPause;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<UpdateStrategiesImpl> {

    @Override
    public String getDisplayName() {
      return "Update Strategies info";
    }
  }
}
