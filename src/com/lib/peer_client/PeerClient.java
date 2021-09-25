package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.lib.interfaces.RMIClientInterface;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  public PeerClient(String rmiInterfaceString) throws RemoteException {
    System.out.println("Peer client created.");

    try{
      System.out.println("Binding Client RMI Interface to " + rmiInterfaceString);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString, this);
    }
    catch (Exception ex) {
      System.err.println("PeerClient Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }
  }

  @Override
  public int retrieve(String filename) throws RemoteException {
    System.out.println("Client Retrieve is called.");
    return 0;
  }
}
