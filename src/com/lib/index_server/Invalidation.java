package com.lib.index_server;

import java.io.Serializable;

public class Invalidation implements Serializable {
  public MessageID messageId;
  public int timeToLive;
  public int originServerId;
  public String filename;
  public int version;
}
