package com.valor.mercury.elasticsearch.web.configs;

import com.mfc.config.ConfigTools3;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ElasticsearchWebConfiguration {

    public List<String> getElasticsearchAddress(){
        return ConfigTools3.getConfigAsList("mercury.elasticsearch.web.elasticsearch.address",",");
    }

    public int getElasticsearchDataViewSize(){
        return ConfigTools3.getConfigAsInt("mercury.elasticsearch.web.data.view.size",10);
    }

    public int getElasticsearchBasicQuerySize(){
        return ConfigTools3.getConfigAsInt("mercury.elasticsearch.web.basic.query.size",15);
    }

    public int getElasticsearchBasicQueryDays(){
        return ConfigTools3.getConfigAsInt("mercury.elasticsearch.web.basic.query.days",8);
    }

    public String getElasticsearchDateField(){
        return ConfigTools3.getConfigAsString("mercury.elasticsearch.web.date.field","LocalCreateTime");
    }

    public String getUserConfigFiles() {
        return ConfigTools3.getConfigAsString("mercury.elasticsearch.web.user.config","cfg/users.txt");
    }
}
