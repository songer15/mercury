package com.valor.mercury.gateway.elasticsearch;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;
import com.valor.mercury.gateway.elasticsearch.query.SqlElasticDeleteByQueryRequestBuilder;
import com.valor.mercury.gateway.elasticsearch.query.SqlElasticRequestBuilder;
import com.valor.mercury.gateway.elasticsearch.query.join.JoinRequestBuilder;

import java.io.IOException;


public class ActionRequestRestExecuter {

	private RestChannel channel;
	private Client client;
	private SqlElasticRequestBuilder requestBuilder;

	public ActionRequestRestExecuter(SqlElasticRequestBuilder requestBuilder, RestChannel channel, final Client client) {
		this.requestBuilder = requestBuilder;
		this.channel = channel;
		this.client = client;
	}



    /**
	 * Execute the ActionRequest and returns the REST response using the channel.
	 */
	public void execute() throws Exception {
        ActionRequest request = requestBuilder.request();

        //todo: maby change to instanceof multi?
        if(requestBuilder instanceof JoinRequestBuilder){
            executeJoinRequestAndSendResponse();
        }
		else if (request instanceof SearchRequest) {
			client.search((SearchRequest) request, new RestStatusToXContentListener<SearchResponse>(channel));
		} else if (requestBuilder instanceof SqlElasticDeleteByQueryRequestBuilder) {
            throw new UnsupportedOperationException("currently not support delete on elastic 2.0.0");
        }
        else if(request instanceof GetIndexRequest) {
            this.requestBuilder.getBuilder().execute( new GetIndexRequestRestListener(channel, (GetIndexRequest) request));
        }


		else {
			throw new Exception(String.format("Unsupported ActionRequest provided: %s", request.getClass().getName()));
		}
	}

    private void executeJoinRequestAndSendResponse() throws IOException, SqlParseException {
        ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client,requestBuilder);
        executor.run();
        executor.sendResponse(channel);
    }

}
