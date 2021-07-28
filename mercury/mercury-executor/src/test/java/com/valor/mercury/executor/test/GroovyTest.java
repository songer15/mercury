package com.valor.mercury.executor.test;

import com.valor.mercury.common.constant.MercuryConstants;
import com.valor.mercury.executor.script.groovy.GroovyScriptTask;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.io.FileInputStream;

public class GroovyTest {



    @Test
    public void downloadFromMercury() {
        try {
            GroovyScriptTask job = new GroovyScriptTask();
            job.bashCommand = "java -jar mercury-script-task-1.0-SNAPSHOT.jar 1 2 3";
            job.isExecutable = true;
            job.commandType = MercuryConstants.EXECUTOR_COMMAND_START;
            job.exec();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void file() {
        try {
            System.out.println(DigestUtils.md5Hex(new FileInputStream("mercury-script-task-1.0-SNAPSHOT.jar")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
