package org.zeroturnaround.jenkins;

import hudson.model.BuildListener;
import org.zeroturnaround.liverebel.plugins.PluginLogger;

public class JenkinsLogger implements PluginLogger {

  private final BuildListener listener;

  public JenkinsLogger(BuildListener listener) {this.listener = listener;}

  public void log(String message) {
    listener.getLogger().println(message);
  }
}
