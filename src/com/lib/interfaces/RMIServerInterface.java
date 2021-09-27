package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {
  public int register(String lookupString, Vector<String> filenames) throws RemoteException;
  public ArrayList<Integer> search(String filename) throws RemoteException;
  public int deregister(int peerId, String filename) throws RemoteException;
}
