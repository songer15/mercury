package com.valor.mercury.elasticsearch.web.controller;


import com.valor.mercury.elasticsearch.web.aop.CommonApiCall;
import com.valor.mercury.elasticsearch.web.model.APIException;
import com.valor.mercury.elasticsearch.web.model.JsonResult;
import com.valor.mercury.elasticsearch.web.model.PageResult;
import com.valor.mercury.elasticsearch.web.model.response.ElasticsearchDataResponse;
import com.valor.mercury.elasticsearch.web.model.response.IndexStateResponse;
import com.valor.mercury.elasticsearch.web.service.ElasticsearchService;
import com.valor.mercury.elasticsearch.web.service.PageTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Controller
public class ElasticsearchViewController {
    private final Logger LOG = LoggerFactory.getLogger(ElasticsearchViewController.class);

    @Autowired
    private ElasticsearchService elasticsearchService;


    @RequestMapping("/elasticsearchView")
    public String elasticsearchQueryView() {
        return ReqRediectItem.ELASTICSEARCH_VIEW;
    }

    @RequestMapping("/elasticsearchBasicQuery")
    public String elasticsearchBasicQueryView() {
        return ReqRediectItem.ELASTICSEARCH_BASIC_QUERY;
    }

    @RequestMapping("/elasticsearchDataDetail")
    public String elasticsearchDataDetailView() {
        return ReqRediectItem.ELASTICSEARCH_DATA_DETAIL;
    }


    @CommonApiCall
    @ResponseBody
    @RequestMapping("/elasticsearch/searchIndexs")
    public PageResult<IndexStateResponse> searchIndexs(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam(name = "index", defaultValue = "") String index,
                                                       @RequestParam(name = "page", defaultValue = "1") String page,
                                                       @RequestParam(name = "limit", defaultValue = "10") String limit) throws Exception {
        List<IndexStateResponse> list = elasticsearchService.searchIndexs(index);
        return PageTools.pageResult(list, Integer.parseInt(limit), Integer.parseInt(page));
    }

    @CommonApiCall
    @ResponseBody
    @RequestMapping("/elasticsearch/searchIndexMapping")
    public JsonResult searchIndexMapping(HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(name = "index", defaultValue = "") String index) throws Exception {
        if (StringUtils.isEmpty(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX_MSG);

        Map<String, String> mapping = elasticsearchService.searchIndexMapping(index);
        return JsonResult.ok().put("data", mapping);
    }


    @CommonApiCall
    @ResponseBody
    @RequestMapping("/elasticsearch/dataView")
    public JsonResult dataView(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(name = "index", defaultValue = "") String index) throws Exception {
        if (StringUtils.isEmpty(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_DATA_VIEW, ResponseHttpCode.ERR_ELASTICSEARCH_DATA_VIEW_EMPTY_INDEX, ResponseHttpCode.ERR_ELASTICSEARCH_DATA_VIEW_EMPTY_INDEX_MSG);

        ElasticsearchDataResponse data = elasticsearchService.dataView(index);
        return JsonResult.ok().put("data", data);
    }

    @CommonApiCall
    @ResponseBody
    @RequestMapping("/elasticsearch/basicQuery")
    public JsonResult basicQuery(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(name = "index", defaultValue = "") String index,
                                 @RequestParam(name = "requestJsonStr", defaultValue = "") String requestJsonStr) throws Exception {
        if (StringUtils.isEmpty(index))
            throw new APIException(ResponseHttpCode.RET_ELASTICSEARCH_BASIC_QUERY, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX, ResponseHttpCode.ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX_MSG);

        ElasticsearchDataResponse data = elasticsearchService.basicQuery(index,requestJsonStr);
        return JsonResult.ok().put("data", data);
    }


}