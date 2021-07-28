package com.valor.mercury.executor.test;

import com.mfc.config.ConfigTools3;
import com.nextbreakpoint.flinkclient.api.ApiException;
import com.nextbreakpoint.flinkclient.api.FlinkApi;
import com.nextbreakpoint.flinkclient.model.DashboardConfiguration;
import com.nextbreakpoint.flinkclient.model.JarListInfo;
import com.nextbreakpoint.flinkclient.model.JarRunResponseBody;
import com.nextbreakpoint.flinkclient.model.JarUploadResponseBody;
import groovy.lang.*;
import groovy.util.GroovyScriptEngine;
import io.github.luyiisme.script.ScriptEngine;
import io.github.luyiisme.script.management.ScriptManager;
import io.github.luyiisme.script.sandbox.interceptor.NoSystemExitInterceptor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LibTest {

    //@Test
    public void flinktest() throws ApiException {
//        System.out.println(System.getProperty("user.home"));
//        System.out.println(System.getProperty("java.io.tmpdir"));

        ConfigTools3.load("cfg");
//        System.out.println(ConfigTools3.getConfigAsString("jobmanager.addr"));
        FlinkApi api = new FlinkApi();
        api.getApiClient().setBasePath(ConfigTools3.getConfigAsString("jobmanager.addr"));
        api.getApiClient().getHttpClient().setConnectTimeout(20000, TimeUnit.MILLISECONDS);
        api.getApiClient().getHttpClient().setWriteTimeout(30000, TimeUnit.MILLISECONDS);
        api.getApiClient().getHttpClient().setReadTimeout(30000, TimeUnit.MILLISECONDS);
        api.getApiClient().setDebugging(true);
        //DashboardConfiguration config = api.showConfig();
        //JarUploadResponseBody result = api.uploadJar(new File("C:\\Users\\Administrator\\IdeaProjects\\devops-metric\\vms-metric\\metric-spider\\build\\libs\\metric-spider-0.3.0-SNAPSHOT.jar"));
        JarListInfo jars = api.listJars();

//        result.getFilename();
//        JarRunResponseBody response = api.runJar("bf4afb3b-d662-435e-b465-5ddb40d68379_flink-job.jar", true, null, "--INPUT A --OUTPUT B", null, null, null);
    }

    @Test
    public void filetest() {
        File file = new File(".");
        System.out.println(file.getAbsolutePath());
        System.out.println(System.getProperty("user.dir"));
        System.out.println(System.getProperty("user.home"));

    }

    //@Test
    public void groovytest() throws Exception {

        int num = 10000;
        /*
        GroovyClassLoader
         */
        long start = System.currentTimeMillis();
        GroovyClassLoader loader =  new GroovyClassLoader();
        Class aClass = loader.parseClass(new File("src/Groovy/com/baosight/groovy/CycleDemo.groovy"));
        try {
            GroovyObject instance = (GroovyObject) aClass.newInstance();
            instance.invokeMethod("cycle", num);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis()-start;

        /*
        GroovyShell
         */
        long start1 = System.currentTimeMillis();

        new GroovyShell().parse( new File( "src/Groovy/com/baosight/groovy/CycleDemo.groovy" ) )
                .invokeMethod("cycle",num);

        long end1 = System.currentTimeMillis()-start1;

        /*
        GroovyScriptEngine
         */
        long start2 = System.currentTimeMillis();

        Class script = new GroovyScriptEngine("src/Groovy/com/baosight/groovy/")
                .loadScriptByName("CycleDemo.groovy");
        try {
            Script instance =(Script) script.newInstance();
            instance.invokeMethod ("cycle",new Object[]{num});
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        long end2 = System.currentTimeMillis()-start2;

        System.out.println(" GroovyClassLoader时间："+ end );
        System.out.println(" GroovyShell时间："+ end1 );
        System.out.println(" GroovyScriptEngine时间："+ end2 );

    }


    //@Test
    public void groovytest2() throws Exception {
        ScriptEngine scriptEngine = new io.github.luyiisme.script.groovy.GroovyScriptEngine();
        scriptEngine.addGroovyInterceptor(new NoSystemExitInterceptor());

        ((ScriptManager)scriptEngine).registerScript("123", "println( context.name ); return 1;");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "jack");
        Integer v = scriptEngine.invoke("123", params);
        Assert.assertSame(1, v);
    }

    @Test
    public void groovytest3() {
        try {
            Binding binding = new Binding();
            //foo
            binding.setVariable("foo", 2);
            binding.setVariable("bar", 2);
            GroovyShell shell = new GroovyShell(binding);

            Object value = shell.evaluate(new File("src\\test\\java\\com\\valor\\mercury\\executor\\test\\HelloWorld.groovy"));
            //System.out.println(value);
        } catch (
                CompilationFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
