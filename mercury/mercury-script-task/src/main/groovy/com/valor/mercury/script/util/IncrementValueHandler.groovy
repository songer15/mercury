package com.valor.mercury.script.util

/**
 * @author Gavin* 2020/9/15 13:45
 */
class IncrementValueHandler {

    static setIncValue(String className, String incValue) {
        new File(className + ".txt").withWriter('utf-8') {
            writer -> writer.write incValue
        }
    }

    static String getIncValue(String className) {
        String incValue = ""
        new File(className + ".txt").eachLine {
            line -> incValue = incValue + line
        }
        return incValue
    }

    static setErrorMsg(String className, String value) {
        new File(className + ".error.txt").withWriter('utf-8') {
            writer -> writer.write value
        }
    }
}
