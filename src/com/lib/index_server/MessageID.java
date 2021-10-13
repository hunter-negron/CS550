package com.lib.index_server;

public class MessageID {
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
