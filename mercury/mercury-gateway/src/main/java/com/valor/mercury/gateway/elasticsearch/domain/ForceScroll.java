package com.valor.mercury.gateway.elasticsearch.domain;

public class ForceScroll {

    boolean isForce ;
    int scrollSize;
    long keepAlive;
    String scrollId;
    boolean isFirstSearch ;

    public ForceScroll(boolean isForce, int scrollSize, long keepAlive, String scrollId, boolean isFirstSearch) {
        this.isForce = isForce;
        this.scrollSize = scrollSize;
        this.keepAlive = keepAlive;
        this.scrollId = scrollId;
        this.isFirstSearch = isFirstSearch;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean force) {
        isForce = force;
    }

    public int getScrollSize() {
        return scrollSize;
    }

    public void setScrollSize(int scrollSize) {
        this.scrollSize = scrollSize;
    }

    public long getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(long keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public boolean isFirstSearch() {
        return isFirstSearch;
    }

    public void setFirstSearch(boolean firstSearch) {
        isFirstSearch = firstSearch;
    }
}
