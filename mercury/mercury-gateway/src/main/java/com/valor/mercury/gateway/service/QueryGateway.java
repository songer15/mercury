package com.valor.mercury.gateway.service;


import com.valor.mercury.gateway.model.dto.goose.GooseQueryResponseDTO;
import com.valor.mercury.gateway.model.dto.goose.GooseQueryRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryGateway {
    @Autowired
    private ElasticSearchQueryService elasticSearchQueryService;

    public GooseQueryResponseDTO query(GooseQueryRequestDTO dto) {
        return elasticSearchQueryService.queryForGoose(dto);
    }

}
