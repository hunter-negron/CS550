package com.lib.interfaces;

import java.util.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.lib.index_server.Query;
import com.lib.index_server.QueryHit;
import com.lib.index_server.Invalidation;

public interface RMIServerInterface extends Remote {
  public String register(String lookupString, Vector<String> filenames, String identifier) throws RemoteException;
  public ArrayList<Integer> search(String filename) throws RemoteException;
  public int deregister(String identifier, Vector<String> filenames) throws RemoteException;
  public void testCall() throws RemoteException;
  public QueryHit forwardQuery(Query q) throws RemoteException;
  /* --- start PA3 change --- */
  public void forwardInvalidation(Invalidation inv) throws RemoteException;
  /* ---- end PA3 change ---- */
}
