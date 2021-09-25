package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.lib.interfaces.RMIClientInterface;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  private int id;

  public PeerClient(String rmiInterfaceString, int peerId) throws RemoteException {
    id = peerId;
    System.out.println("Peer client " + id + " created.");

    try{
      System.out.println("Binding Client RMI Interface to " + rmiInterfaceString + id);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString + id, this);
    }
    catch (Exception ex) {
      System.err.println("PeerClient Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }
  }

  @Override
  public int retrieve(String filename) throws RemoteException {
    System.out.println("Client " + id + " Retrieve is called.");
    return id;
  }
}
