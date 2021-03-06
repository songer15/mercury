package com.valor.mercury.gateway.elasticsearch.query.multi;

import org.elasticsearch.client.Client;
import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;
import com.valor.mercury.gateway.elasticsearch.query.QueryAction;

/**
 * Created by Eliran on 19/8/2016.
 */
public class ESMultiQueryActionFactory {

    public static QueryAction createMultiQueryAction(Client client, MultiQuerySelect multiSelect) throws SqlParseException {
        switch (multiSelect.getOperation()){
            case UNION_ALL:
            case UNION:
                return new MultiQueryAction(client,multiSelect);
            default:
                throw new SqlParseException("only supports union and union all");
        }
    }
}
