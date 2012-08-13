package org.zeroturnaround.jenkins.util;

import org.zeroturnaround.jenkins.ServerCheckbox;
import org.zeroturnaround.liverebel.plugins.Server;

import java.util.ArrayList;
import java.util.List;

public class ServerConvertUtil {

  public static List<ServerCheckbox> serverToServerCheckBox (List<Server> servers) {
    List<ServerCheckbox> serverCheckboxList = new ArrayList<ServerCheckbox>();
    for (Server server : servers) {
      serverCheckboxList.add(new ServerCheckbox(server.getId(), server.getTitle(), server.getParentNames(), server.getIndentDepth(), server.isChecked(), server.isConnected(), server.isGroup()));
    }
    return serverCheckboxList;
  }

  public static List<Server> serverCheckBoxToServer(List<ServerCheckbox> servers) {
    List<Server> serversNew = new ArrayList<Server>();
    for (Server server : servers) {
      serversNew.add(server);
    }
    return serversNew;
  }
}
