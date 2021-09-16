package com.rmitest.rmiinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {

    public String helloTo(String name, String filename, byte[] data, int len) throws RemoteException;

}
