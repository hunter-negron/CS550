package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote {
  public int register(String ip, String lookupString, Vector<String> filenames) throws RemoteException;
  public int search(String filename) throws RemoteException;
  public int deregister(int peerId) throws RemoteException;
}
