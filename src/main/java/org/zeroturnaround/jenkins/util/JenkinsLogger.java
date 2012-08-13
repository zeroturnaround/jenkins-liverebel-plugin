package org.zeroturnaround.jenkins.util;

import hudson.model.BuildListener;
import org.zeroturnaround.liverebel.plugins.PluginLogger;

public class JenkinsLogger implements PluginLogger {

  private final BuildListener listener;

  public JenkinsLogger(BuildListener listener) {this.listener = listener;}

  public void log(String message) {
    listener.getLogger().println(message);
  }

  public void error(String error) {
    listener.getLogger().println(error);
  }
}
