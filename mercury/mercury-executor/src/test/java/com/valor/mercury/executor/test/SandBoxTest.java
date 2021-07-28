package com.valor.mercury.executor.test;

import org.kohsuke.groovy.sandbox.GroovyInterceptor;

import javax.sql.DataSource;

public class SandBoxTest {

    class NoSystemExitSandbox extends GroovyInterceptor {
        @Override
        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
            if (receiver==System.class && method=="exit")
                throw new SecurityException("No call on System.exit() please");
            return super.onStaticCall(invoker, receiver, method, args);
        }
    }

    class NoRunTimeSandbox extends GroovyInterceptor {
        @Override
        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
            if (receiver==Runtime.class)
                throw new SecurityException("No call on RunTime please");
            return super.onStaticCall(invoker, receiver, method, args);
        }
    }

    class NoMysqlDatasourceSandbox extends GroovyInterceptor {
        @Override
        public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object... args) throws Throwable {
            if (receiver== DataSource.class)
                throw new SecurityException("No call on Datasource please");
            return super.onStaticCall(invoker, receiver, method, args);
        }
    }

//    @Test
//    public void sandbox() {
//        final GroovyShell sh = new GroovyShell(new CompilerConfiguration()
//                .addCompilationCustomizers(new SandboxTransformer()));
//        new NoSystemExitSandbox().register();
//        new NoRunTimeSandbox().register();
//        sh.evaluate("System.exit(0)");
//
//    }

}


