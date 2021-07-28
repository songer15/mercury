package com.valor.mercury.script.util;

/**
 * @author Gavin
 * 2020/9/21 10:25
 */
public interface HDFSReader {
    void read(String value);

    void finish(String line);
}
