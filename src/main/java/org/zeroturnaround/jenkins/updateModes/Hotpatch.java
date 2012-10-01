package org.zeroturnaround.jenkins.updateModes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;


public class Hotpatch extends UpdateMode{
  public final boolean updateWithWarnings;
  public final int requestPause;
  public final UpdateMode fallback;

  public Hotpatch() {
    this.fallback = null;
    updateWithWarnings = false;
    requestPause = 0;
  }
  @DataBoundConstructor
  public Hotpatch(boolean updateWithWarnings, int requestPause, UpdateMode fallback) {
    this.updateWithWarnings = updateWithWarnings;
    this.requestPause = requestPause;
    this.fallback = fallback;
  }

  @Override
  public String toString() {
    return "Hotpatch";
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<UpdateMode> {

    public Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> getDefaultFallbackUpdate() {
      DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
      Iterator<Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> it = allDescriptors.iterator();
      while (it.hasNext()) {
        Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
        if (next.clazz == RollingRestarts.class) {
          return next;
        }
      }
      return null;
    }

    public List<Descriptor<UpdateMode>> getFallbackUpdateModes() {
      DescriptorExtensionList<org.zeroturnaround.jenkins.updateModes.UpdateMode, Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode>> allDescriptors = Hudson.getInstance().getDescriptorList(org.zeroturnaround.jenkins.updateModes.UpdateMode.class);
      List<Descriptor<UpdateMode>> fallbackUpdateModes = new ArrayList<Descriptor<UpdateMode>>();
      Iterator<Descriptor<UpdateMode>> it = allDescriptors.iterator();

      while (it.hasNext()) {
        Descriptor<org.zeroturnaround.jenkins.updateModes.UpdateMode> next = it.next();
        if (next.clazz != Hotpatch.class) {
          fallbackUpdateModes.add(next);
        }
      }
      return fallbackUpdateModes;
    }

    @Override
    public String getDisplayName() {
      return "Hotpatch";
    }
  }
}
