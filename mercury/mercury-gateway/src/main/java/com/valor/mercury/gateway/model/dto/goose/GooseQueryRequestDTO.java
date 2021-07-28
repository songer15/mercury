package com.valor.mercury.gateway.model.dto.goose;

import com.valor.mercury.common.model.AbstractPrintable;

import javax.validation.constraints.NotEmpty;



public class GooseQueryRequestDTO extends AbstractPrintable {
    @NotEmpty(message = "start cannot be empty")
    private String start;
    @NotEmpty(message = "end cannot be empty")
    private String end;
    private String productId;
    @NotEmpty(message = "queryId cannot be empty")
    private String queryId;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
}
