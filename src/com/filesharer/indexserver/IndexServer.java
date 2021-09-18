package com.filesharer.indexserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class IndexServer extends UnicastRemoteObject implements IndexServerInterface {

    private static final long serialVersionUID = 1L;

    protected IndexServer()
      throws RemoteException {
      super();
    }

    @Override
    public int register(String[] filenames) throws RemoteException {
      return 0;
    }
}
