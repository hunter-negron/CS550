package com.lib.index_server;

import java.util.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
// import java.rmi.server.hostname;

import com.lib.interfaces.RMIServerInterface;

public class IndexServer extends UnicastRemoteObject implements RMIServerInterface {
  static int peerId;

  private HashMap<String, ArrayList<Integer>> fileIndex; // to search for peer ids for a particular file
  private HashMap<Integer, RegisteredPeerInfo> rpiIndex; // to search for peer info from peer ids

  public IndexServer(String rmiInterfaceString) throws RemoteException {
    super();

    peerId = 0; // setting the initital count as 0
    fileIndex = new HashMap<String, ArrayList<Integer>>();
    rpiIndex = new HashMap<Integer, RegisteredPeerInfo>();

    try{
      System.out.println("Binding Server RMI Interface to " + rmiInterfaceString);
      // Binding to the RMI Interface
      Naming.rebind(rmiInterfaceString, this);
    }
    catch (Exception ex) {
      System.err.println("IndexServer Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }
  }

  @Override
  public int register(String lookupString, Vector<String> filenames) throws RemoteException {
    System.out.println("register called!");
    RegisteredPeerInfo rpi = new RegisteredPeerInfo();

    rpi.lookupString = lookupString;
    rpi.peerId = peerId++;
    rpi.filenames = filenames;

    rpiIndex.put(rpi.peerId, rpi);

    for(String f : filenames) {
      System.out.println("registering peer " + rpi.peerId + " file \"" + f + "\"");

      // check if the file is new in the index
      if(!fileIndex.containsKey(f)) {
        fileIndex.put(f, new ArrayList<Integer>());
      }

      // check if the peerId is already present for that particular file.
      if(!fileIndex.get(f).contains(rpi.peerId)){
        fileIndex.get(f).add(rpi.peerId);
      }
    }

    return rpi.peerId;
  }

  @Override
  public ArrayList<Integer> search(String filename) throws RemoteException {
    System.out.println("search called!");
    return fileIndex.get(filename);
    // return 0;
  }

  @Override
  public int deregister(int peerId, String filename) throws RemoteException {
    System.out.println("deregister called!");
    fileIndex.get(filename).remove(peerId);
    rpiIndex.get(peerId).filenames.remove(filename);
    return 0;
  }
}
