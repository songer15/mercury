package com.valor.mercury.gateway.elasticsearch.query;

import com.valor.mercury.gateway.elasticsearch.domain.*;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import com.valor.mercury.gateway.elasticsearch.exception.SqlParseException;
import com.valor.mercury.gateway.elasticsearch.query.maker.QueryMaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transform SQL query to standard Elasticsearch search query
 */
public class DefaultQueryAction extends QueryAction {

	private final Select select;
	private SearchRequestBuilder request;
	private ForceScroll forceScroll;

	public DefaultQueryAction(Client client, Select select) {
		super(client, select);
		this.select = select;
		this.forceScroll = new ForceScroll(true, 2000, 600_000, "", true);
	}

	public void intialize(SearchRequestBuilder request) throws SqlParseException {
		this.request = request;
	}

	/**
	 * 强制使用scroll_id查询
	 */
	@Override
	public SqlElasticSearchRequestBuilder explain() throws SqlParseException {
			if (forceScroll.isFirstSearch()) {
				this.request = new SearchRequestBuilder(client, SearchAction.INSTANCE);
				setIndicesAndTypes();
				setFields(select.getFields());
				setWhere(select.getWhere());
				setSorts(select.getOrderBys());
				setLimit(select.getOffset(), select.getRowCount());
				if (!select.isOrderdSelect())
					request.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
				request
						.setSize(forceScroll.getScrollSize())
						.setScroll(new TimeValue(forceScroll.getKeepAlive()));
				updateRequestWithIndexAndRoutingOptions(select, request);
				updateRequestWithHighlight(select, request);
				updateRequestWithCollapse(select, request);
				updateRequestWithPostFilter(select, request);
				return new SqlElasticSearchRequestBuilder(request);
			} else {
				//接下来的查询用 SearchScrollRequestBuilder，只需要带着scroll_id就可以
				return new SqlElasticSearchRequestBuilder(new SearchScrollRequestBuilder(client, SearchScrollAction.INSTANCE, forceScroll.getScrollId()).setScroll(new TimeValue(forceScroll.getKeepAlive())));
			}
	}

	public ForceScroll getForceScroll() {
		return forceScroll;
	}

	public void setForceScroll(ForceScroll forceScroll) {
		this.forceScroll = forceScroll;
	}

	/**
	 * Set indices and types to the search request.
	 */
	private void setIndicesAndTypes() {
		request.setIndices(query.getIndexArr());

		String[] typeArr = query.getTypeArr();
		if (typeArr != null) {
			request.setTypes(typeArr);
		}
	}

	/**
	 * Set source filtering on a search request.
	 * 
	 * @param fields
	 *            list of fields to source filter.
	 */
	public void setFields(List<Field> fields) throws SqlParseException {
		if (select.getFields().size() > 0) {
			ArrayList<String> includeFields = new ArrayList<String>();
			ArrayList<String> excludeFields = new ArrayList<String>();

			for (Field field : fields) {
				if (field instanceof MethodField) {
					MethodField method = (MethodField) field;
					if (method.getName().toLowerCase().equals("script")) {
						handleScriptField(method);
					} else if (method.getName().equalsIgnoreCase("include")) {
						for (KVValue kvValue : method.getParams()) {
							includeFields.add(kvValue.value.toString()) ;
						}
					} else if (method.getName().equalsIgnoreCase("exclude")) {
						for (KVValue kvValue : method.getParams()) {
							excludeFields.add(kvValue.value.toString()) ;
						}
					}
				} else if (field instanceof Field) {
					includeFields.add(field.getName());
				}
			}

			request.setFetchSource(includeFields.toArray(new String[includeFields.size()]), excludeFields.toArray(new String[excludeFields.size()]));
		}
	}

	private void handleScriptField(MethodField method) throws SqlParseException {
		List<KVValue> params = method.getParams();
		if (params.size() == 2) {
			request.addScriptField(params.get(0).value.toString(), new Script(params.get(1).value.toString()));
		} else if (params.size() == 3) {
			request.addScriptField(params.get(0).value.toString(), new Script(ScriptType.INLINE, params.get(1).value.toString(), params.get(2).value.toString(), Collections.emptyMap()));
		} else {
			throw new SqlParseException("scripted_field only allows script(name,script) or script(name,lang,script)");
		}
	}

	/**
	 * Create filters or queries based on the Where clause.
	 * 
	 * @param where
	 *            the 'WHERE' part of the SQL query.
	 * @throws SqlParseException
	 */
	private void setWhere(Where where) throws SqlParseException {
		if (where != null) {
			BoolQueryBuilder boolQuery = QueryMaker.explan(where,this.select.isQuery);
			request.setQuery(boolQuery);
		}
	}

	/**
	 * Add sorts to the elasticsearch query based on the 'ORDER BY' clause.
	 * 
	 * @param orderBys
	 *            list of Order object
	 */
	private void setSorts(List<Order> orderBys) {
		for (Order order : orderBys) {
            if (order.getNestedPath() != null) {
                request.addSort(SortBuilders.fieldSort(order.getName()).order(SortOrder.valueOf(order.getType())).setNestedSort(new NestedSortBuilder(order.getNestedPath())));
            } else {
                request.addSort(order.getName(), SortOrder.valueOf(order.getType()));
            }
		}
	}

	/**
	 * Add from and size to the ES query based on the 'LIMIT' clause
	 * 
	 * @param from
	 *            starts from document at position from
	 * @param size
	 *            number of documents to return.
	 */
	private void setLimit(int from, int size) {
		request.setFrom(from);

		if (size > -1) {
			request.setSize(size);
		}
	}

	public SearchRequestBuilder getRequestBuilder() {
		return request;
	}
}
