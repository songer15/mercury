package com.valor.mercury.manager.service;

import com.valor.mercury.manager.config.MercuryConstants;
import com.valor.mercury.manager.config.ConstantConfig;
import com.valor.mercury.manager.dao.MercuryManagerDao;
import com.valor.mercury.manager.model.ddo.UploadFileConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

/**
 * @author Gavin
 * 2020/7/31 10:14
 * 文件和数据库信息管理
 */
@Service
public class ConfigService extends BaseDBService {

    private final MercuryManagerDao dao;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static RestTemplate restTemplate = new RestTemplate();
    private final HAService haService;

    @Autowired
    public ConfigService(MercuryManagerDao dao, HAService haService) {
        this.dao = dao;
        this.haService = haService;
    }

    @Override
    public MercuryManagerDao getSchedulerDao() {
        return dao;
    }

    public Pair<Boolean, String> uploadFile(MultipartFile sourceFile, String targetType) {
        if (getEntityByCriterion(UploadFileConfig.class, Restrictions.eq("fileName", sourceFile.getOriginalFilename())) != null) {
            logger.error("the sam file name:{}", sourceFile.getOriginalFilename());
            return ImmutablePair.of(false, "the sam file name");
        }
        File file = new File(MercuryConstants.BaseFilePath);
        if (!file.exists() && !file.mkdirs()) {
            logger.error("create file path fail, filePath:{}", MercuryConstants.BaseFilePath);
            return ImmutablePair.of(false, "create file path fail");
        }
        try {
            FileOutputStream out = new FileOutputStream(MercuryConstants.BaseFilePath
                    + "/" + sourceFile.getOriginalFilename());
            out.write(sourceFile.getBytes());
            out.flush();
            out.close();

            UploadFileConfig fileConfig = new UploadFileConfig();
            fileConfig.setCreateTime(new Date());
            fileConfig.setFileMd5(DigestUtils.md5Hex(sourceFile.getInputStream()));
            fileConfig.setFileSize(sourceFile.getSize());
            fileConfig.setUploadSource(ConstantConfig.localIP());
            fileConfig.setRequestTimes(0);
            fileConfig.setFileName(sourceFile.getOriginalFilename());
            fileConfig.setTargetType(targetType);
            Long fileId = (long) dao.saveEntity(fileConfig);

            List<String> nodes = haService.getManagerNodes();
            nodes.remove(ConstantConfig.localIP());
            for (String node : nodes) {
                FileSystemResource fileSystemResource = new FileSystemResource(MercuryConstants.BaseFilePath
                        + "/" + sourceFile.getOriginalFilename());
                MediaType type = MediaType.parseMediaType("multipart/form-data");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(type);
                MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
                form.add("file", fileSystemResource);
                form.add("fileId", fileId);
                HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                        "http://" + node + ":8091/config/file/syncAdd", files, String.class);
                String result = responseEntity.getBody();
                if ("fail".equals(result)) {
                    logger.error("文件同步失败");
                    return ImmutablePair.of(false, "文件同步失败");
                }
                fileConfig.setUploadSource(fileConfig.getUploadSource() + ":" + node);
            }
            update(fileConfig);
            return ImmutablePair.of(true, null);
        } catch (Exception e) {
            logger.error("uploadFile error:{}", e);
            return ImmutablePair.of(false, "上传文件失败");
        }
    }

    public Boolean saveFile(MultipartFile sourceFile) {
        File file = new File(MercuryConstants.BaseFilePath);
        if (!file.exists() && !file.mkdirs()) {
            logger.error("create file path fail, filePath:{}", MercuryConstants.BaseFilePath);
            return false;
        }
        try {
            FileOutputStream out = new FileOutputStream(MercuryConstants.BaseFilePath
                    + "/" + sourceFile.getOriginalFilename());
            out.write(sourceFile.getBytes());
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            logger.error("saveFile error:{}", e);
            return false;
        }
    }

    public Pair<Boolean, String> deleteFile(Long id) {
        try {
            UploadFileConfig fileConfig = getEntityById(UploadFileConfig.class, id);
            File file = new File(MercuryConstants.BaseFilePath
                    + "/" + fileConfig.getFileName());
            if (!file.delete())
                logger.error("delete file error:{}", file.getName());
            //删除其他服务器中的文件
            List<String> nodes = haService.getManagerNodes();
            nodes.remove(ConstantConfig.localIP());
            for (String node : nodes) {
                MediaType type = MediaType.parseMediaType("multipart/form-data");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(type);
                MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
                form.add("fileName", fileConfig.getFileName());
                HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                        "http://" + node + ":8091/config/file/syncDelete", files, String.class);
                String result = responseEntity.getBody();
                if ("fail".equals(result)) {
                    logger.error("文件同步删除失败:{}", node);
                }
            }
            deleteEntity(fileConfig);
            return ImmutablePair.of(true, null);
        } catch (Exception e) {
            logger.error("deleteFile error:{}", e);
            return ImmutablePair.of(false, e.getMessage());
        }
    }

    public Pair<Boolean, String> syncFile(Long fileId) {
        UploadFileConfig fileConfig = getEntityById(UploadFileConfig.class, fileId);
        List<String> nodes = haService.getManagerNodes();
        for (String node : nodes) {
            if (!fileConfig.getUploadSource().contains(node)) {
                FileSystemResource fileSystemResource = new FileSystemResource(MercuryConstants.BaseFilePath
                        + "/" + fileConfig.getFileName());
                MediaType type = MediaType.parseMediaType("multipart/form-data");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(type);
                MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
                form.add("file", fileSystemResource);
                form.add("fileId", fileId);
                HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                        "http://" + node + ":8091/config/file/syncAdd", files, String.class);
                String result = responseEntity.getBody();
                if ("fail".equals(result)) {
                    logger.error("文件同步失败");
                    return ImmutablePair.of(false, "文件同步失败");
                }
                fileConfig.setUploadSource(fileConfig.getUploadSource() + ":" + node);
            }
        }
        update(fileConfig);
        return ImmutablePair.of(true, null);
    }
}
