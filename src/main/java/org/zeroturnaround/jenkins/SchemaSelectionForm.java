package org.zeroturnaround.jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

public class SchemaSelectionForm implements Describable<SchemaSelectionForm> {

  private String selectedSchema;
  private String targetProxy;

  @DataBoundConstructor
  public SchemaSelectionForm(final String selectedSchema, final String targetProxy) {
    this.selectedSchema = selectedSchema;
    this.targetProxy = targetProxy;
  }

  public void setSelectedSchema(String selectedSchema) {
    this.selectedSchema = selectedSchema;
  }

  public void setTargetProxy(String targetProxy) {
    this.targetProxy = targetProxy;
  }

  public String getSelectedSchema() {
    return selectedSchema;
  }

  public String getTargetProxy() {
    return targetProxy;
  }

  public SchemaSelectionDescriptorImpl getDescriptor() {
    return (SchemaSelectionDescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
  }

  @Extension
  public static class SchemaSelectionDescriptorImpl extends Descriptor<SchemaSelectionForm> {

    @Override
    public String getDisplayName() {
      return "Schema selection form";
    }
  }
}
