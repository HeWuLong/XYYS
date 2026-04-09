package com.vod.server.task;


public class SyncResult {
    private int newCount;
    private int updateCount;
    public SyncResult() {}
    public SyncResult(int newCount, int updateCount) {
        this.newCount = newCount;
        this.updateCount = updateCount;
    }
    // getter / setter
    public int getNewCount() { return newCount; }
    public void setNewCount(int newCount) { this.newCount = newCount; }
    public int getUpdateCount() { return updateCount; }
    public void setUpdateCount(int updateCount) { this.updateCount = updateCount; }
}