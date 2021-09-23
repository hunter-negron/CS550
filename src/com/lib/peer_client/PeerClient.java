package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.lib.interfaces.RMIClientInterface;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  public PeerClient() throws RemoteException {
    System.out.println("Peer client created.");
  }

  @Override
  public int retrieve(String filename) throws RemoteException {
    System.out.println("Client Retrieve is called.");
    return 0;
  }
}
