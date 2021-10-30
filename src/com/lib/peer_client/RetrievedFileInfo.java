package com.lib.peer_client;

import java.util.*;
import java.io.Serializable;

public class RetrievedFileInfo implements Serializable {
  public int version;
  public int originServerId;
  public int originPeerId;
  public boolean valid;
  public Date lastVerified;
  public int timeToRefresh; // in minutes.
  public boolean owner;
}
