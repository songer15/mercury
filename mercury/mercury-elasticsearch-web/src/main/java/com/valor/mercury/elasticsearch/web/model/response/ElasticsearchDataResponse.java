package com.valor.mercury.elasticsearch.web.model.response;

import com.valor.mercury.elasticsearch.web.model.BaseResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataResponse extends BaseResponse {
    private List<String> columns = new ArrayList<>();
    private List<Map<String, Object>> datas = new ArrayList<>();
    private long total;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getDatas() {
        return datas;
    }

    public void setDatas(List<Map<String, Object>> datas) {
        this.datas = datas;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
