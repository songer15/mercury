package com.valor.mercury.manager.controller;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.model.system.JsonResult;
import com.valor.mercury.manager.model.system.PageResult;
import com.valor.mercury.manager.model.ddo.DataBaseConfig;
import com.valor.mercury.manager.model.ddo.UploadFileConfig;
import com.valor.mercury.manager.service.ConfigService;
import com.valor.mercury.manager.service.ExecutorService;
import com.valor.mercury.manager.tool.HttpTool;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;

/**
 * @author Gavin
 * 2020/7/31 10:13
 */
@Controller
@RequestMapping("/config")
public class ConfigController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConfigService configService;
    private final ExecutorService executorService;

    @Autowired
    public ConfigController(ConfigService configService, ExecutorService executorService) {
        this.configService = configService;
        this.executorService = executorService;
    }

    @RequestMapping("database")
    public String databasePage() {
        return "main/database.html";
    }

    @RequestMapping("file")
    public String filePage() {
        return "main/file.html";
    }

    @RequestMapping("database/editForm")
    public String editFormPage() {
        return "main/database_form.html";
    }

    @RequestMapping("database/list")
    @ResponseBody
    public PageResult<DataBaseConfig> list(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return configService.list(DataBaseConfig.class, page, limit);
    }

    @RequestMapping("file/list")
    @ResponseBody
    public PageResult<UploadFileConfig> listFile(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return configService.list(UploadFileConfig.class, page, limit);
    }

    @RequestMapping("database/add")
    @ResponseBody
    public JsonResult add(DataBaseConfig databaseConfig) {
        databaseConfig.setCreateTime(new Date());
        databaseConfig.setLastModifyTime(new Date());
        if (configService.addEntity(databaseConfig)) {
            return JsonResult.ok();
        } else
            return JsonResult.error("taskExecutor Name conflict");
    }

    @RequestMapping("database/edit")
    @ResponseBody
    public JsonResult edit(DataBaseConfig databaseConfig) {
        DataBaseConfig preConfig = configService.getEntityById(DataBaseConfig.class, databaseConfig.getId());
        databaseConfig.setCreateTime(preConfig.getCreateTime());
        if (configService.update(databaseConfig))
            return JsonResult.ok();
        else
            return JsonResult.error("taskExecutor Name conflict");
    }

    @RequestMapping("database/delete")
    @ResponseBody
    public JsonResult deleteDatabase(@RequestParam Long id) {
        logger.info("request deleteDatabase:{}", id);
        if (configService.deleteById(DataBaseConfig.class, id))
            return JsonResult.ok();
        else
            return JsonResult.error("删除失败");
    }

    @RequestMapping("file/delete")
    @ResponseBody
    public JsonResult deleteFile(@RequestParam Long id) {
        logger.info("request deleteFile:{}", id);
        Pair<Boolean, String> result = configService.deleteFile(id);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error("删除失败:" + result.getValue());
    }

    @RequestMapping("database/queryList")
    @ResponseBody
    public PageResult<DataBaseConfig> queryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name) {
        Criterion criterion = null;
        if (!Strings.isEmpty(name)) criterion = Restrictions.like("name", name);
        return configService.list(DataBaseConfig.class, page, limit, criterion);
    }

    @RequestMapping("file/queryList")
    @ResponseBody
    public PageResult<UploadFileConfig> fileQueryList(@RequestParam(value = "page", defaultValue = "1") Integer page
            , @RequestParam(value = "limit", defaultValue = "10") Integer limit, String name) {
        Criterion criterion = null;
        if (!Strings.isEmpty(name)) criterion = Restrictions.like("fileName", name);
        return configService.list(UploadFileConfig.class, page, limit, criterion);
    }

    @RequestMapping("file/upload")
    @ResponseBody
    public JsonResult uploadFile(@RequestParam("file") MultipartFile sourceFile, @RequestParam("targetType") String targetType) {
        Pair<Boolean, String> result = configService.uploadFile(sourceFile, targetType);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("file/download")
    public void downloadFile(@RequestParam String clientName, @RequestParam String clientPsd, @RequestParam String fileMd5,
                             HttpServletRequest request, HttpServletResponse response) {
        String clientIP = HttpTool.getIpAddress(request);
        logger.info("request download file, executor name:{},ip:{}", clientName, clientIP);
        Pair<Boolean, String> verifyResult = executorService.verifyExecutor(clientName, clientPsd, null, clientIP);
        if (verifyResult.getKey()) {
            try {
                UploadFileConfig uploadFileConfig = configService.getEntityByCriterion(UploadFileConfig.class,
                        Restrictions.eq("fileMd5", fileMd5));
                response.setContentType("text/html; charset=UTF-8");
                response.setContentType("application/x-msdownload");
                response.setHeader("Content-disposition", "attachment;filename=" + uploadFileConfig.getFileName());
                OutputStream out = response.getOutputStream();
                File file = new File(MercuryConstants.BaseFilePath
                        + "/" + uploadFileConfig.getFileName());
                if (file.exists()) {
                    InputStream fis = new BufferedInputStream(new FileInputStream(file));
                    byte[] buff = new byte[4096];
                    int size;
                    while ((size = fis.read(buff)) != -1) {
                        out.write(buff, 0, size);
                    }
                    out.flush();
                    out.close();
                    fis.close();
                }
            } catch (Exception e) {
                logger.error("downloadFile error:{}", e);
            }
        } else
            logger.error("downloadFile error:verifyExecutor fail");
    }

    @RequestMapping("file/syncAdd")
    @ResponseBody
    public String syncFileAdd(@RequestParam("file") MultipartFile sourceFile, @RequestParam("fileId") Long fileId) {
        logger.info("request syncAdd file,fileID:{}", fileId);
        UploadFileConfig fileConfig = configService.getEntityById(UploadFileConfig.class, fileId);
        if (fileConfig == null)
            return "fail";
        Boolean result = configService.saveFile(sourceFile);
        if (result)
            return "success";
        else
            return "fail";
    }

    @RequestMapping("file/sync")
    @ResponseBody
    public JsonResult syncFile(@RequestParam("fileId") Long fileId) {
        logger.info("request syncFile,fileID:{}", fileId);
        Pair<Boolean, String> result = configService.syncFile(fileId);
        if (result.getKey())
            return JsonResult.ok();
        else
            return JsonResult.error(result.getValue());
    }

    @RequestMapping("file/syncDelete")
    @ResponseBody
    public String syncFileDelete(@RequestParam("fileName") String fileName) {
        try {
            File file = new File(MercuryConstants.BaseFilePath
                    + "/" + fileName);
            file.deleteOnExit();
        } catch (Exception e) {
            logger.error("syncFileDelete error:{}", e);
            return "fail";
        }
        return "success";
    }

}
