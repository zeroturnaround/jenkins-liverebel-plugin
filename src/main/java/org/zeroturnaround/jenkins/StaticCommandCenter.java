package org.zeroturnaround.jenkins;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.jenkins.LiveRebelDeployBuilder.DescriptorImpl;

import com.zeroturnaround.liverebel.api.CommandCenter;
import com.zeroturnaround.liverebel.api.CommandCenterFactory;
import com.zeroturnaround.liverebel.api.ConnectException;
import com.zeroturnaround.liverebel.api.Forbidden;

/**
 * Class to cache the Command Center instance.
 */
public class StaticCommandCenter {

  private static String _authToken;
  private static String _lrUrl;
  private static boolean _isDebugLoggingEnabled = false;

  private static volatile CommandCenter instance;

  public static CommandCenter getCommandCenter() {
    CommandCenter cc = instance;
    if (cc == null) {
      cc = newCommandCenter();
      instance = cc;
    }
    return cc;
  }

  private static CommandCenter newCommandCenter() {
    Logger log = LoggerFactory.getLogger(DescriptorImpl.class);
    if (_authToken == null || _lrUrl == null) {
      log.warn("Please, navigate to Jenkins Configuration to specify running LiveRebel Url and Authentication Token.");
      return null;
    }

    try {
      return newCommandCenterFactory().newCommandCenter();
    }
    catch (Forbidden e) {
      log.warn("ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
    }
    catch (ConnectException e) {
      log.warn("ERROR! Unable to connect to server.");
      log.warn("URL: {}", e.getURL());
      if (e.getURL().equals("https://")) {
        log.warn("Please, navigate to Jenkins Configuration to specify running LiveRebel URL.");
      }
      else {
        log.warn("Reason: {}", e.getMessage());
      }
    }
    return null;
  }

  public static CommandCenterFactory newCommandCenterFactory() {
    return new CommandCenterFactory().authenticate(_authToken).setUrl(_lrUrl).setVerbose(_isDebugLoggingEnabled);
  }

  public static void configure(String authToken, String lrUrl, boolean isDebugLoggingEnabled) {
    _authToken = authToken;
    _lrUrl = lrUrl;
    _isDebugLoggingEnabled = isDebugLoggingEnabled;
    //clean up old instance
    close();
    //initialize new instance
    getCommandCenter();
  }

  public static boolean isDebugLoggingEnabled() {
    return _isDebugLoggingEnabled;
  }

  public static void close() {
    CommandCenter cc = instance;
    instance = null;
    if (cc != null) {
      try {
        cc.close();
      }
      catch (IOException e) {
        LoggerFactory.getLogger(StaticCommandCenter.class).error("should never happen", e);
      }
    }
  }

}
