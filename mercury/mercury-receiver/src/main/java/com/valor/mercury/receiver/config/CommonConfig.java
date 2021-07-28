package com.valor.mercury.receiver.config;

import com.mfc.config.ConfigTools3;

import java.util.List;

/**
 *常用的从配置文件里读取的配置
 */
public class CommonConfig {
    public static boolean isControllerLoggable(){ return ConfigTools3.getAsBoolean("loggable.controller", false); }

    public static int getForwardThreadSize() { return ConfigTools3.getConfigAsInt("forward.thread.size",3);}

    public static int getMessageQueueSize() { return ConfigTools3.getConfigAsInt("message.queue.size",500000); }
}
