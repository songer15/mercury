package com.valor.mercury.gateway.controller;

import com.valor.mercury.gateway.model.query.QueryResult;
import com.valor.mercury.gateway.model.dto.QueryRequestDTO;
import com.valor.mercury.gateway.model.dto.QueryResponseDTO;
import com.valor.mercury.gateway.service.ElasticSearchQueryService;
import com.valor.mercury.gateway.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("mercury")
public class SqlQueryController {

    private static final Logger log = LoggerFactory.getLogger(SqlQueryController.class);
    @Autowired
    private ElasticSearchQueryService elasticSearchQueryService;

    @Autowired
    private FileService fileService;

    /**
     * 一般查询
     */
    @PostMapping(value = "api/query/sql/v1")
    public QueryResponseDTO search(HttpServletRequest request, HttpServletResponse response, @RequestBody QueryRequestDTO dto) {
        QueryResult result = elasticSearchQueryService.query(dto);
        return QueryResponseDTO.success(result);
    }


    /**
     * 下载文件
     */
    @PostMapping(value = "api/download/{filePath}")
    public FileSystemResource export(HttpServletRequest request, HttpServletResponse response, @PathVariable String filePath) {
        return fileService.download(filePath);
    }
}
