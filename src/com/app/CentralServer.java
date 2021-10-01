package com.app.server;

import java.util.*;
import java.rmi.Naming;

import com.lib.interfaces.RMIServerInterface;
import com.lib.index_server.IndexServer;
import com.lib.peer_client.PeerClient;

public class CentralServer {
  final static String rmiServerStr = "//localhost/central_server"; // hardcoded for now

  public static void main(String[] args) {
    try {
      // create index server
      RMIServerInterface is = new IndexServer(rmiServerStr);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating server: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      // Register with itself as any other peer, right now just use current directory
      String hostname = "localhost";
      PeerClient pc = new PeerClient(hostname, ".");
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating client: " + ex.toString());
      ex.printStackTrace();
    }

  }
}
