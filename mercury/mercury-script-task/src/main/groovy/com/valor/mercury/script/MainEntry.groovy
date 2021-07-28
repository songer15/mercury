package com.valor.mercury.script
/**
 * @author Gavin* 2020/12/22 16:44
 */
class MainEntry {
    static void main(String[] args) {
        try {
            if (args == null || args.size() == 0)
                System.exit(1)
            Class clazz = Class.forName(args[0])
            MetricAnalyse metricAnalyse = (MetricAnalyse) clazz.newInstance()
            metricAnalyse.run(args)
        } catch (Exception e) {
            println(e)
            System.exit(1)
        }
    }
}
