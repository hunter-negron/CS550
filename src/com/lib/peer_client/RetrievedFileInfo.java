package com.lib.peer_client;

import java.util.*;
import java.io.Serializable;

public class RetrievedFileInfo implements Serializable {
  int version;
  int originServerId;
  int originPeerId;
  boolean valid;
  Date lastVerfied;
  int timeToRefresh; // in minutes
}
