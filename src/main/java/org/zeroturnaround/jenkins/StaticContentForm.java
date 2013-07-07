package org.zeroturnaround.jenkins;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

public class StaticContentForm implements Describable<StaticContentForm> {

  private List<ServerCheckbox> staticServers;
  private String filePath;

  @DataBoundConstructor
  public StaticContentForm(final List<ServerCheckbox> staticServers, final String filePath) {
    this.setStaticServers(staticServers);
    this.setFilePath(filePath);
  }

  public List<ServerCheckbox> getStaticServers() {
    return staticServers;
  }

  public void setStaticServers(List<ServerCheckbox> staticServers) {
    this.staticServers = staticServers;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public StaticContentDescriptorImpl getDescriptor() {
    return (StaticContentDescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class StaticContentDescriptorImpl extends Descriptor<StaticContentForm> {

    @Override
    public String getDisplayName() {
      return "Static content form";
    }
  }

}
