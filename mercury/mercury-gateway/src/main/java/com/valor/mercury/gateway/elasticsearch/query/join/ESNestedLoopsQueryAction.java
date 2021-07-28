package com.valor.mercury.gateway.elasticsearch.query.join;

import com.valor.mercury.gateway.elasticsearch.domain.JoinSelect;
import com.valor.mercury.gateway.elasticsearch.domain.Where;
import com.valor.mercury.gateway.elasticsearch.domain.hints.Hint;
import com.valor.mercury.gateway.elasticsearch.domain.hints.HintType;
import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;
import org.elasticsearch.client.Client;

/**
 * Created by Eliran on 15/9/2015.
 */
public class ESNestedLoopsQueryAction extends ESJoinQueryAction {

    public ESNestedLoopsQueryAction(Client client, JoinSelect joinSelect) {
        super(client, joinSelect);
    }

    @Override
    protected void fillSpecificRequestBuilder(JoinRequestBuilder requestBuilder) throws SqlParseException {
        NestedLoopsElasticRequestBuilder nestedBuilder = (NestedLoopsElasticRequestBuilder) requestBuilder;
        Where where = joinSelect.getConnectedWhere();
        nestedBuilder.setConnectedWhere(where);

    }

    @Override
    protected JoinRequestBuilder createSpecificBuilder() {
        return new NestedLoopsElasticRequestBuilder();
    }

    @Override
    protected void updateRequestWithHints(JoinRequestBuilder requestBuilder) {
        super.updateRequestWithHints(requestBuilder);
        for(Hint hint : this.joinSelect.getHints()){
            if(hint.getType() ==  HintType.NL_MULTISEARCH_SIZE){
                Integer multiSearchMaxSize = (Integer) hint.getParams()[0];
                ((NestedLoopsElasticRequestBuilder) requestBuilder).setMultiSearchMaxSize(multiSearchMaxSize);
            }
        }
    }

    private String removeAlias(String field) {
        String alias = joinSelect.getFirstTable().getAlias();
        if(!field.startsWith(alias+"."))
            alias = joinSelect.getSecondTable().getAlias();
        return field.replace(alias+".","");
    }

}
