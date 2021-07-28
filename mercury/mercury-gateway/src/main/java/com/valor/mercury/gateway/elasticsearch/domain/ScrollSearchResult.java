package com.valor.mercury.gateway.elasticsearch.domain;

import org.elasticsearch.search.SearchHits;

public class ScrollSearchResult {
    SearchHits hits;
    String FilePath;

    public SearchHits getHits() {
        return hits;
    }

    public void setHits(SearchHits hits) {
        this.hits = hits;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }
}
