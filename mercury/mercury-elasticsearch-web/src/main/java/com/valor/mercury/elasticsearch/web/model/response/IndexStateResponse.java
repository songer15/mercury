package com.valor.mercury.elasticsearch.web.model.response;

import com.valor.mercury.elasticsearch.web.model.BaseResponse;
import com.valor.mercury.elasticsearch.web.model.indexState.IndexState;

public class IndexStateResponse extends BaseResponse {
    private String index;
    private IndexState indexPrimaries = new IndexState();
    private IndexState indexTotal = new IndexState();


    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }


    public IndexState getIndexPrimaries() {
        return indexPrimaries;
    }

    public void setIndexPrimaries(IndexState indexPrimaries) {
        this.indexPrimaries = indexPrimaries;
    }

    public IndexState getIndexTotal() {
        return indexTotal;
    }

    public void setIndexTotal(IndexState indexTotal) {
        this.indexTotal = indexTotal;
    }
}
