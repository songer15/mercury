package com.valor.mercury.gateway.controller;

import com.valor.mercury.gateway.model.dto.goose.GooseQueryResponseDTO;
import com.valor.mercury.gateway.aop.GateWayApiCall;
import com.valor.mercury.gateway.model.dto.goose.GooseQueryRequestDTO;
import com.valor.mercury.gateway.service.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("mercury")
public class GooseQueryController {
    private static final Logger log = LoggerFactory.getLogger(SqlQueryController.class);
    @Autowired
    private QueryGateway queryGateway;

    /**
     * goose固定报表查询
     */
    @GateWayApiCall
    @PostMapping(value = "api/query/goose/v1")
    public GooseQueryResponseDTO search(HttpServletRequest request, HttpServletResponse response, @RequestBody GooseQueryRequestDTO dto) {
        return queryGateway.query(dto);
    }
}
