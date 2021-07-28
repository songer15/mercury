package com.valor.mercury.receiver.controller;

import com.valor.mercury.common.model.MetricMessage;
import com.valor.mercury.common.model.dto.ResponseEntity;
import com.valor.mercury.receiver.aop.EnableControllerLog;
import com.valor.mercury.receiver.service.ForwardService;
import com.valor.mercury.receiver.service.ServiceManger;
import com.valor.mercury.receiver.util.HttpUtils;
import com.valor.mercury.common.model.Constants;
import com.valor.mercury.common.model.exception.CustomException;
import com.valor.mercury.common.util.DeflateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

@Controller
public class MetricController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ForwardService forwardService;


    /**
     *
     */
    @EnableControllerLog
    @RequestMapping(value = {"/els/postData2"}, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postData2(@RequestParam("value") String value,  HttpServletRequest request, Class<?> clazz) {
        String senderIp = HttpUtils.getIpAddress(request);
        try {
//            ServiceManger.serviceMonitor.addInterceptedCount();
            String data = DeflateUtils.unCompress(Base64.getDecoder().decode(value));
            forwardService.processDataAsync(data, senderIp, clazz);
            return ResponseEntity.success("");
        } catch (CustomException e) {
            ResponseEntity<String> response =  ResponseEntity.fail(e.getMsg(), e.getCode(), null);
            logger.error("Metric controller error: {}", response.toString());
            return response;
        } catch (Exception e) {
            ResponseEntity<String> response =  ResponseEntity.fail(e.getMessage(), Constants.CustomCode.UNDEFINED_EROOR.getValue(), null);
            logger.error("Metric controller error: {}", response.toString());
            return response;
        }
    }


    @EnableControllerLog
    @RequestMapping(value = {"/metric/batchpost/v1"}, method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity metricPost(@RequestParam("value") String value, HttpServletRequest request) {
        return postData2(value, request, MetricMessage.class);
    }

    @EnableControllerLog
    @RequestMapping(value = "ping")
    @ResponseBody
    public String status() {
        return "OK";
    }



}
