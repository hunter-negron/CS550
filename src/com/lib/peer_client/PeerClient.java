package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.io.FileInputStream;

import com.lib.interfaces.RMIClientInterface;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  private int id;
  String dir;

  public PeerClient(String rmiInterfaceString, int peerId, String directory) throws RemoteException {
    id = peerId;
    dir = directory;
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
  public FileInfo retrieve(String filename) throws RemoteException {
    System.out.println("Client " + id + " Retrieve is called.");

    // All the info the client needs to know to save the file
    FileInfo ret = new FileInfo();
    ret.filename = filename;
    ret.data = new byte[1024*1024]; // MAX FILE SIZE 1024x1024
    ret.len = 0;

    try {
      // Read file
      File f1 = new File(dir, filename);
      FileInputStream in = new FileInputStream(f1);
      ret.len = in.read(ret.data);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    return ret;
  }
}
