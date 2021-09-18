package com.filesharer.peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
  public boolean requestFile(PeerInterface p, String filename) throws RemoteException;
  public boolean sendFile(String filename, byte[] data, int len) throws RemoteException;
}
