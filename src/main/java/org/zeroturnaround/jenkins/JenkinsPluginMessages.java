package org.zeroturnaround.jenkins;

import org.zeroturnaround.liverebel.plugins.PluginMessages;

public class JenkinsPluginMessages extends PluginMessages {

  public JenkinsPluginMessages() {
    this.liveRebelXmlNotFound = "liverebel.xml not found in provided archive (%s) and override information also not specified! " +
          "Easiest way to fix this is to specify override information in LiveRebel build step configuration, by checking the " +
          "'Override the version information in the artifact' checkbox.";
  }

}
