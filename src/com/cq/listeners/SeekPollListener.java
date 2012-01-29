package com.cq.listeners;

public interface SeekPollListener {

  public static final int NewSeekRequests = 0;
  public static final int UnreadSeekRequests = 1;
  public static final int NewSeekResponses = 2;
  
  public void processPollResult(int[] unreadAndNewSeeks);
}
