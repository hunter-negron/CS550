package com.filesharer.indexserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IndexServerInterface extends Remote {
  public int register(String[] filenames) throws RemoteException;
}
