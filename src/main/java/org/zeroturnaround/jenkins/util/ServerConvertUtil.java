package org.zeroturnaround.jenkins.util;

import org.zeroturnaround.jenkins.ServerCheckbox;
import org.zeroturnaround.liverebel.plugins.Server;

import com.zeroturnaround.liverebel.util.ServerKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerConvertUtil {

  public static List<ServerCheckbox> serverToServerCheckBox(List<Server> servers) {
    List<ServerCheckbox> serverCheckboxList = new ArrayList<ServerCheckbox>();
    for (Server server : servers) {
      serverCheckboxList.add(new ServerCheckbox(server.getId(), server.getTitle(), server.getParentNames(), server.getIndentDepth(), server.isChecked(), server.isConnected(),
          server.isGroup(), server.getType(), server.isVirtualHostsSupported(), server.getDefaultVirtualHostName(), server.getVirtualHostNames()));
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

  private final static String defaultVirtualHostName = "default host";

  private static boolean vhostAdded(String vhost, List<String> vhosts) {
    for (int i = 0; i < vhosts.size(); i++)
      if (vhosts.get(i).equalsIgnoreCase(vhost)) {
        return true;
      }
    return false;
  }

  public static List<Server> filterProxyServers(List<Server> servers) {
    List<Server> serverList = new ArrayList<Server>();
    for (int i = 0; i < servers.size(); i++) {
      Server server = servers.get(i);
      if (canBeSchemaTargetProxy(server))
        serverList.add(new ServerCheckbox(server.getId(), server.getTitle(), server.getParentNames(), server.getIndentDepth(),
            false, server.isConnected(),
            server.isGroup(), server.getType(),
            server.isVirtualHostsSupported(), server.getDefaultVirtualHostName(), server.getVirtualHostNames()));
    }
    return serverList;
  }

  public static List<Server> filterFileServers(List<Server> servers, List<ServerCheckbox> checkedServers) {
    List<Server> serverList = new ArrayList<Server>();
    List<Integer> fsItems = new ArrayList<Integer>(servers.size());

    for (int i = 0; i < servers.size(); i++) {
      Server server = servers.get(i);
      if (isFileOrWebProxy(server)) {
        fsItems.add(i);
        int l = server.getIndentDepth();
        for (int j = i - 1; j > -1; j--) {
          if (fsItems.contains(j))
            break;
          if (servers.get(j).getIndentDepth() < l) {
            fsItems.add(j);
            l--;
          }
        }
      }
    }

    Collections.sort(fsItems);
    for (int i = 0; i < fsItems.size(); i++) {
      Server server = servers.get(fsItems.get(i));
      serverList.add(new ServerCheckbox(server.getId(), server.getTitle(), server.getParentNames(), server.getIndentDepth(),
          findIfServerIsChecked(checkedServers, server), server.isConnected(),
          server.isGroup(), server.getType(),
          server.isVirtualHostsSupported(), server.getDefaultVirtualHostName(), server.getVirtualHostNames()));
    }

    return serverList;
  }

  private static boolean findIfServerIsChecked(List<ServerCheckbox> checkedServers, Server server) {
    if (checkedServers == null)
      return false;
    for (int i = 0; i < checkedServers.size(); i++)
      if (checkedServers.get(i).isChecked() && checkedServers.get(i).getTitle().equalsIgnoreCase(server.getTitle()))
        return true;
    return false;
  }

  private static boolean isFileOrWebProxy(Server server) {
    return !server.isGroup() && (server.getType() == ServerKind.FILE || server.getType() == ServerKind.WEB_PROXY);
  }

  private static boolean canBeSchemaTargetProxy(Server server) {
    return !server.isGroup() && server.getType() != ServerKind.DATABASE;
  }
}
