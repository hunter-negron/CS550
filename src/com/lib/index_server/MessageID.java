package com.lib.index_server;

import java.io.Serializable;

public class MessageID implements Serializable {
  public int superpeerId;
  public int peerId;
  public int seq;

  public boolean equals(MessageID m) {
    return (
      this.superpeerId == m.superpeerId &&
      this.peerId == m.peerId &&
      this.seq == m.seq
    );
  }
}
