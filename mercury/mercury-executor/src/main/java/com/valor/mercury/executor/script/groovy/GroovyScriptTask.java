package com.valor.mercury.executor.script.groovy;

import com.alibaba.fastjson.JSON;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.common.model.ExecutorCommand;
import com.valor.mercury.common.model.ExecutorReport;
import com.valor.mercury.common.util.PostUtil;
import com.valor.mercury.common.util.StringTools;
import com.valor.mercury.executor.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Date;

import static com.valor.mercury.common.constant.MercuryConstants.*;

public class GroovyScriptTask extends Task {
    private static Logger logger = LoggerFactory.getLogger(GroovyScriptTask.class);
    public GroovyScriptManager groovyScriptManager;
    public String scriptId;
    public String scriptText;
    public String entryClass;
    public String fileName;
    public String fileMd5;
    public String incValue;
    public String config;
    public String programArguments;
    public String filePath;
    public File file;
    public String bashCommand;
    public long runningPid = -1L;


    public void init() throws IOException{
        taskType = TYPE_OFFLINE_SCRIPT;
        checkArgs();
        downloadFileIfAbsent();
        prepareBashCommand();
        prepareIncValue();
        isExecutable = true;
    }

    /**
     * 检查必要参数
     */
    public void checkArgs() {
        Assert.notNull(commandType, "action is null");
        if (isStartCommand()) {
            Assert.notNull(entryClass, "entryClass is null");
            Assert.notNull(fileMd5, "fileMd5 is null");
            Assert.notNull(fileName, "fileName is null");
            //默认使用当前目录
            filePath = fileName;
            if (!StringUtils.isEmpty(config)) {
                programArguments = config;
            }
        }
        isArgsValid = true;
    }

    public void prepareBashCommand() {
        StringBuilder command = new StringBuilder();
        if (MercuryConstants.EXECUTOR_COMMAND_STOP.equals(commandType)) {
            command.append("kill -9").append(" ");
            Long pid  =  ScriptTaskExecutor.processMap.get(instanceId);
            if (pid != null && pid != -1L)
                command.append(pid);
            else
                throw new RuntimeException("pid does not exists");
        } else if (MercuryConstants.EXECUTOR_COMMAND_START.equals(commandType)) {
            command.append("java -jar ").append(filePath).append(" ");

            //添加主类名称，必需
//        jarCommand.append(entryClass).append(" ");

            //添加主类参数，可选
            if (programArguments != null)
                command.append(programArguments).append(" ");
        }
        bashCommand = command.toString();
    }

    public void prepareIncValue() throws IOException{
        File file = new File(entryClass + ".txt");
        FileUtils.write(file, incValue);
    }

    public void downloadFileIfAbsent() throws IOException {
        if (!isArgsValid)
            throw new IllegalArgumentException(String.format("invalid args, the task is {%s}", this.toString()));
        if (isStartCommand() && !isMD5equivalent()) {
            file = PostUtil.downloadFromMercury(clientName, clientPsd, fileMd5, fileName);
        }
    }

    public boolean isMD5equivalent() {
        try {
            String currentMD5 = DigestUtils.md5Hex(new FileInputStream(fileName));
            logger.info("current md5: {}", currentMD5);
            logger.info("target md5: {}", fileMd5);
            if (fileMd5.equals(currentMD5))
                return true;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public void exec() throws Exception {
        Assert.isTrue(isExecutable, "job is not executable");
        executorReport.setInstanceStatus(OFFLINE_TASK_INSTANCE_STATUS_RUNNING);
        Process p = Runtime.getRuntime().exec(bashCommand);
        runningPid = getProcessID(p);
        if (runningPid != -1L)
            ScriptTaskExecutor.processMap.put(instanceId, runningPid);
        int code =  p.waitFor();
        if (code == 0) {
            executorReport.getMetrics().put("endTime", System.currentTimeMillis());
            executorReport.getMetrics().put("incValue", FileUtils.readFileToString(new File(entryClass + ".txt")));
            executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_SUCCESS);
        }
        else {
            File errorFile = new File(entryClass + ".error.txt");
            if (errorFile.exists()) {
                //按照约定，程序本身产生的错误会记录到 .error.txt文件，由script-executor读取并返回给manager
                executorReport.setErrorMessage(FileUtils.readFileToString(errorFile));
            } else {
                //如果程序本身没有产生错误，script-executor会把标准错误流返回给manager
                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while((line = error.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                error.close();
                executorReport.setErrorMessage(sb.toString());
            }
            executorReport.setInstanceStatus(EXECUTOR_INSTANCE_STATUS_FAIL);
        }
    }

    public static long getProcessID(Process p) {
        long result = -1;
        try
        {
            //for windows
            if (p.getClass().getName().equals("java.lang.Win32Process") ||
                    p.getClass().getName().equals("java.lang.ProcessImpl"))
            {
                Field f = p.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(p);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE hand = new WinNT.HANDLE();
                hand.setPointer(Pointer.createConstant(handl));
                result = kernel.GetProcessId(hand);
                f.setAccessible(false);
            }
            //for unix based operating systems
            else if (p.getClass().getName().equals("java.lang.UNIXProcess"))
            {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                result = f.getLong(p);
                f.setAccessible(false);
            }
        }
        catch(Exception ex)
        {
            result = -1;
        }
        return result;
    }

    private boolean isStartCommand() { return MercuryConstants.EXECUTOR_COMMAND_START.equals(commandType); }

    private boolean isStopCommand() { return MercuryConstants.EXECUTOR_COMMAND_STOP.equals(commandType); }

    private void isInvalidCommand() { throw new IllegalArgumentException(String.format("invalid commandType: {%s}", commandType)); }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getIncValue() {
        return incValue;
    }

    public void setIncValue(String incValue) {
        this.incValue = incValue;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getProgramArguments() {
        return programArguments;
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments = programArguments;
    }


}
