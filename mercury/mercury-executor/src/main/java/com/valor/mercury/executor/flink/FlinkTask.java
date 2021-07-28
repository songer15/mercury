package com.valor.mercury.executor.flink;

import com.nextbreakpoint.flinkclient.api.ApiException;
import com.nextbreakpoint.flinkclient.api.ApiResponse;
import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.nextbreakpoint.flinkclient.model.JarRunResponseBody;
import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.executor.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static com.valor.mercury.common.constant.MercuryConstants.*;

public class FlinkTask extends Task {
    public String jobId;
    public String entryClass;
    public String jarName;
    public String jarMD5;
    public File jarFile;
    public String programArguments;
    public int parallelism;
    public FlinkApi flinkApi;
    public String jarId;

    public void init() throws IOException, ApiException {
        taskType = TYPE_REALTIME_FLINK;
        checkArgs();
        downloadJarIfAbsent();
        isExecutable = true;
    }

    /**
     * 检查必要参数
     */
    public void checkArgs() {
        Assert.notNull(commandType, "action is null");
        if (isStopCommand()) {
            Assert.notNull(jobId, "jobId is null");
        } else if (isStartCommand()) {
            Assert.notNull(entryClass, "entryClass is null");
            Assert.notNull(jarName, "jarName is null");
            Assert.notNull(jarMD5, "jarMD5 is null");
        } else {
            isInvalidCommand();
        }
        isArgsValid = true;
    }


    public void downloadJarIfAbsent() throws IOException,ApiException {
        if (!isArgsValid)
            throw new IllegalArgumentException(String.format("invalid args, the task is {%s}", this.toString()));
        if (isStartCommand() && (jarId == null || !isMD5equivalent() )) {
            jarFile = PostUtil.downloadFromMercury(clientName, clientPsd, jarMD5, jarName);
            uploadJar();
        }
    }

    public boolean isMD5equivalent() {
        try {
            String md5 = DigestUtils.md5Hex(new FileInputStream(jarName));
            if (jarMD5.equals(md5))
                return true;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    private void uploadJar() throws ApiException {
        String fullId = flinkApi.uploadJar(jarFile).getFilename();
        if (fullId.contains("/"))
            jarId = fullId.substring(fullId.lastIndexOf("/") + 1);
    }

    public void runJob() throws IOException, ApiException {
        ApiResponse<JarRunResponseBody> response= flinkApi.runJarWithHttpInfo(jarId,
                true,
                null,
                null,
                programArguments,
                entryClass,
                parallelism);
        if (response != null) {
            if (response.getStatusCode() == 200) {
                jobId = response.getData().getJobid();
                executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_SUCCESS);
                executorReport.getMetrics().put("jobId", jobId );
                executorReport.getMetrics().put("endTime", System.currentTimeMillis());
            } else {
                executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_FAIL);
                executorReport.setErrorMessage("status code: " +response.getStatusCode());
            }
        }
    }

    public void cancelJob() throws ApiException {
        flinkApi.terminateJob(jobId, "cancel");
        executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_SUCCESS);
        executorReport.getMetrics().put("jobId", jobId);
        executorReport.getMetrics().put("endTime", System.currentTimeMillis());
    }

    public void exec() throws IOException, ApiException{
        Assert.isTrue(isExecutable, "job is not executable");
        executorReport.getMetrics().put("startTime", System.currentTimeMillis());
        if (isStartCommand())
            runJob();
        else if (isStopCommand())
            cancelJob();
    }

     boolean isStartCommand() { return MercuryConstants.EXECUTOR_COMMAND_START.equals(commandType); }

     boolean isStopCommand() { return MercuryConstants.EXECUTOR_COMMAND_STOP.equals(commandType); }

     void isInvalidCommand() { throw new IllegalArgumentException(String.format("invalid commandType: {%s}", commandType)); }


    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }


    public String getJarMD5() {
        return jarMD5;
    }

    public void setJarMD5(String jarMD5) {
        this.jarMD5 = jarMD5;
    }



    public File getJarFile() {
        return jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    public String getProgramArguments() {
        return programArguments;
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments = programArguments;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }


    public boolean isArgsValid() {
        return isArgsValid;
    }

    public void setArgsValid(boolean argsValid) {
        isArgsValid = argsValid;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    public void setExecutable(boolean executable) {
        isExecutable = executable;
    }

    public String getJarId() {
        return jarId;
    }

    public void setJarId(String jarId) {
        this.jarId = jarId;
    }
}


