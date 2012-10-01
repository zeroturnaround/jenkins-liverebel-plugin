package org.zeroturnaround.jenkins.updateModes;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

public class UpdateMode implements Describable<UpdateMode> {

  public Descriptor<UpdateMode> getDescriptor() {
    return Hudson.getInstance().getDescriptor(getClass());
  }

  public static class UpdateModeDescriptor extends Descriptor<UpdateMode> {
    public UpdateModeDescriptor(Class<? extends UpdateMode> clazz) {
      super(clazz);
    }
    public String getDisplayName() {
      try {
        return clazz.newInstance().toString();
      } catch (InstantiationException ignored) {}
      catch (IllegalAccessException ignored) {}
      return clazz.getSimpleName(); //fallback to class name
    }
  }
}
