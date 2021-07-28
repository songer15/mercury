package com.valor.mercury.gateway.model.dto.goose;

import com.valor.mercury.common.model.AbstractPrintable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GooseQueryResponseDTO extends AbstractPrintable {
    private String errCode;
    private String message;
    private List<GooseMetric> result = new ArrayList<>();

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GooseMetric> getResult() { return result; }

    public void setResult(List<GooseMetric> result) {
        this.result = result;
    }

    public void addResult(List<Map<String, Object>> list, String metricField) {
        for (Map<String, Object> map : list) {
            GooseMetric gooseMetric = new GooseMetric();
            gooseMetric.setDate((String)map.get("date"));
            gooseMetric.setProductId((String)map.get("productId"));
            gooseMetric.setMetric((Number)map.get(metricField));
            result.add(gooseMetric);
        }
    }

    private class GooseMetric {
        String date;
        String productId;
        Number metric;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Number getMetric() {
            return metric;
        }

        public void setMetric(Number metric) {
            this.metric = metric;
        }
    }
}
