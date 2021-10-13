package com.lib.index_server;

import java.util.*;

public class QueryHit {
  public MessageID messageId;
  public int timeToLive;
  public String filename;

  // Slightly different than specfication
  // because this system runs on localhost only.
  // Instead of port and IP, we use
  // this information to connect to a peer
  public ArrayList<Integer> superpeerId;
  public ArrayList<Integer> peerId;
}
