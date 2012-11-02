package org.zeroturnaround.jenkins.util;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.zeroturnaround.liverebel.api.CommandCenter;

import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

import static org.zeroturnaround.jenkins.LiveRebelDeployBuilder.DescriptorImpl.newCommandCenter;

public class ArtifactAndMetadataDescriptor<T extends Describable<T>> extends Descriptor<T> {

  public FormValidation doCheckArtifact(@AncestorInPath AbstractProject project, @QueryParameter String value)
      throws IOException, ServletException {

    if (StringUtils.trimToNull(value) == null || value.length() == 0) {
      return FormValidation.error("Please provide an artifact.");
    } else  if (value.contains(",")) {
      return FormValidation.error("Please provide only one artifact.");
    }
    else {
      return FilePath.validateFileMask(project.getSomeWorkspace(), value);
    }
  }

  public FormValidation doCheckTrace(@AncestorInPath AbstractProject project, @QueryParameter String value)
      throws IOException, ServletException {

    if (StringUtils.trimToNull(value) != null) {
      if (value.contains(",")) {
        return FormValidation.error("Please provide only one trace file.");
      }
      String fileExtension = null;
      try {
        fileExtension = value.substring(value.lastIndexOf('.') + 1);
      }
      catch (Exception e) {
        return FormValidation.error("Trace must be a text file!");
      }

      if (!fileExtension.equals("txt"))
        return FormValidation.error("Trace must be a text file!");
      return FilePath.validateFileMask(project.getSomeWorkspace(), value);
    }
    else {
      return FormValidation.ok();
    }
  }

  public boolean isMetadataSupported() {
    CommandCenter cc = newCommandCenter();
    return cc != null && !cc.getVersion().equals("2.0");
  }

  @Override
  public String getDisplayName() {
    throw new UnsupportedOperationException("getDisplayName() should be overridden");
  }
}
