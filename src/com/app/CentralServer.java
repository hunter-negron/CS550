package com.app.server;

import java.util.*;
import java.rmi.Naming;

import com.lib.interfaces.RMIServerInterface;
import com.lib.index_server.IndexServer;
import com.lib.peer_client.PeerClient;

import org.json.*;

public class CentralServer {
  final static String rmiServerStr = "//localhost/central_server";
  static String dir;
  static String jsonConfig;
  static int id;

  public static void main(String[] args) {
    // Check if the name to the shared directory is provided
    if(args.length != 3) {
      System.err.println("ARGS: [working dir] [json config file] [superpeer id]");
      System.exit(0);
    }

    dir = args[0];
    jsonConfig = args[1];
    id = Integer.parseInt(args[2]);

    try{
      RMIServerInterface is = new IndexServer(rmiServerStr + id);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating server: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      // Register with itself as any other peer, right now just use current directory
      // String rmiPeerStr = "//localhost/peer";
      // RMIServerInterface centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr);
      // String myPeerIdStr = centralServer.register(rmiPeerStr, new Vector<String>());
      // PeerClient pc = new PeerClient(rmiPeerStr, myPeerId, ".");
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: CentralServer Exception while creating client: " + ex.toString());
      ex.printStackTrace();
    }
  }
}
