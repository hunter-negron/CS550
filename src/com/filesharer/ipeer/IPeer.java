package com.filesharer.ipeer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPeer extends Remote {
  public boolean requestFile(IPeer p) throws RemoteException;
  public boolean sendFile(String filename, byte[] data, int len) throws RemoteException;
}
