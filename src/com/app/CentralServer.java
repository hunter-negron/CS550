package com.app.server;

import java.rmi.Naming;

import com.lib.interfaces.RMIServerInterface;
import com.lib.index_server.IndexServer;
import com.lib.peer_client.PeerClient;

public class CentralServer {
  public static void main(String[] args) {
    try{
      RMIServerInterface is = new IndexServer("//localhost/central_server");
    }
    catch (Exception ex) {
      System.err.println("CentralServer Exception while creating server: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      PeerClient pc = new PeerClient("//localhost/MyServer");
    }
    catch (Exception ex) {
      System.err.println("CentralServer Exception while creating client: " + ex.toString());
      ex.printStackTrace();
    }

  }
}