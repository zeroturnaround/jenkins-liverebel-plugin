package org.zeroturnaround.jenkins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.zeroturnaround.jenkins.updateModes.AllAtOnce;
import org.zeroturnaround.jenkins.updateModes.FailBuild;
import org.zeroturnaround.jenkins.updateModes.FullRestart;
import org.zeroturnaround.jenkins.updateModes.Hotpatch;
import org.zeroturnaround.jenkins.updateModes.LiveRebelDefault;
import org.zeroturnaround.jenkins.updateModes.RollingRestarts;
import org.zeroturnaround.liverebel.plugins.PluginUtil;
import org.zeroturnaround.liverebel.plugins.UpdateMode;
import org.zeroturnaround.liverebel.plugins.UpdateStrategies;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

public class UpdateStrategiesImpl implements Describable<UpdateStrategiesImpl>, UpdateStrategies {

  public final UpdateMode primary;
  public UpdateMode fallback;
  public boolean updateWithWarnings;
  public int sessionDrainTimeout;
  public int requestPauseTimeout;
  public int connectionPauseTimeout;
  public final org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode;

  @DataBoundConstructor
  public UpdateStrategiesImpl(org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode) {
    this.updateMode = updateMode;
    if (updateMode instanceof RollingRestarts) {
      primary = UpdateMode.ROLLING_RESTARTS;
      sessionDrainTimeout = ((RollingRestarts) updateMode).sessionDrain;
    } else if (updateMode instanceof Hotpatch) {
      primary = UpdateMode.HOTPATCH;
      setHotpatchConf((Hotpatch) updateMode);
    }  else if (updateMode instanceof FullRestart) {
      primary = UpdateMode.OFFLINE;
      connectionPauseTimeout = ((FullRestart) updateMode).connectionPause;
    }
    else if (updateMode instanceof AllAtOnce) {
      primary = UpdateMode.ALL_AT_ONCE_UPDATE;
      connectionPauseTimeout = ((AllAtOnce) updateMode).connectionPause;
    } else {
      primary = UpdateMode.LIVEREBEL_DEFAULT;
    }
    checkAndSetDefaultTimeouts();
  }

  private void checkAndSetDefaultTimeouts() {
    if (requestPauseTimeout == 0) requestPauseTimeout = PluginUtil.DEFAULT_REQUEST_PAUSE;
    if (connectionPauseTimeout == 0) connectionPauseTimeout = PluginUtil.DEFAULT_REQUEST_PAUSE;
    if (sessionDrainTimeout == 0) sessionDrainTimeout = PluginUtil.DEFAULT_SESSION_DRAIN;
  }

  private void setHotpatchConf(Hotpatch updateMode) {
    fallback = getFallback(updateMode.fallback);
    if (fallback == UpdateMode.ROLLING_RESTARTS) {
      sessionDrainTimeout = ((RollingRestarts) updateMode.fallback).sessionDrain;
    } else if (fallback == UpdateMode.OFFLINE) {
      connectionPauseTimeout = ((FullRestart) updateMode.fallback).connectionPause;
    }
    updateWithWarnings = updateMode.updateWithWarnings;
    requestPauseTimeout = updateMode.requestPause;
  }

  private UpdateMode getFallback(org.zeroturnaround.jenkins.updateModes.UpdateMode updateMode) {
    if (updateMode instanceof RollingRestarts)
      return UpdateMode.ROLLING_RESTARTS;
    else if (updateMode instanceof FullRestart)
      return UpdateMode.OFFLINE;
    else if (updateMode instanceof FailBuild)
      return UpdateMode.FAIL_BUILD;
    else if (updateMode instanceof LiveRebelDefault)
      return UpdateMode.LIVEREBEL_DEFAULT;
    return UpdateMode.LIVEREBEL_DEFAULT;
  }

  public UpdateMode getPrimaryUpdateStrategy() {
    return primary;
  }

  public UpdateMode getFallbackUpdateStrategy() {
    return fallback;
  }

  public boolean updateWithWarnings() {
    return updateWithWarnings;
  }

  public int getSessionDrainTimeout() {
    return sessionDrainTimeout;
  }

  public int getRequestPauseTimeout() {
    return requestPauseTimeout;
  }

  public int getConnectionPauseTimeout() {
    return connectionPauseTimeout;
  }

  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<UpdateStrategiesImpl> {

    @Override
    public String getDisplayName() {
      return "Update Strategies info";
    }

    public Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> getDefaultPrimaryUpdate() {
      DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
      Iterator<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> it = allDescriptors.iterator();
      while (it.hasNext()) {
        Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
        if (next.clazz == LiveRebelDefault.class) {
          return next;
        }
      }
      return null;
    }
    public List<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> getPrimaryUpdateModes() {
      DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
      List<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> primaryUpdateModes = new ArrayList<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>>();

      Iterator<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> it = allDescriptors.iterator();
      while (it.hasNext()) {
        Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
        if (next.clazz != FailBuild.class) {
          primaryUpdateModes.add(next);
        }
      }

     return primaryUpdateModes;
    }

    @Override
    public String getHelpFile(final String fieldName) {
      if (fieldName.equals("updateMode"))
        return "/plugin/liverebel-deploy/help-update-strategies.html";
      else
        return super.getHelpFile(fieldName);
    }
  }

  @Override
  public String toString() {
    return "UpdateStrategiesImpl{" +
        "primary=" + primary +
        ", fallback=" + fallback +
        ", updateWithWarnings=" + updateWithWarnings +
        ", sessionDrainTimeout=" + sessionDrainTimeout +
        ", requestPauseTimeout=" + requestPauseTimeout +
        ", connectionPauseTimeout=" + connectionPauseTimeout +
        ", updateMode=" + updateMode +
        '}';
  }
}
