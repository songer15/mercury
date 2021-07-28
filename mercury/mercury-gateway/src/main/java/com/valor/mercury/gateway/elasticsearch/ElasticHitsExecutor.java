package com.valor.mercury.gateway.elasticsearch;

import org.elasticsearch.search.SearchHits;
import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;

import java.io.IOException;

/**
 * Created by Eliran on 21/8/2016.
 */
public interface ElasticHitsExecutor {
    public void run() throws IOException, SqlParseException ;
    public SearchHits getHits();
}
