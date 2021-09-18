package com.filesharer.indexserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.ArrayList;

public class IndexServer extends UnicastRemoteObject implements IndexServerInterface {

    private static final long serialVersionUID = 1L;
    public int nextPeerId;
    public HashMap<String, ArrayList<Integer>> index;

    protected IndexServer() throws RemoteException {
      super();
      nextPeerId = 0;
      index = new HashMap<String, ArrayList<Integer>>();
    }

    @Override
    public int register(String[] filenames) throws RemoteException {
      int newPeerId = nextPeerId++; // ENSURE THAT THIS IS THREAD SAFE

      // Map each of the given filenames to a list of peers that has that file
      for(String f : filenames) {
        System.out.println("registering peer " + newPeerId + " file \"" + f + "\"");

        if(!index.containsKey(f)) {
          index.put(f, new ArrayList<Integer>());
        }
        index.get(f).add(newPeerId);
      }

      return newPeerId;
    }

    public static void main(String[] args) {
      // Basic server thread to wait for RMI requests
      try {
        Naming.rebind("//localhost/index", new IndexServer());
        System.err.println("Index server ready");
      }
      catch (Exception e) {
        System.err.println("Index server exception: " + e.toString());
        e.printStackTrace();
      }
    }
}
