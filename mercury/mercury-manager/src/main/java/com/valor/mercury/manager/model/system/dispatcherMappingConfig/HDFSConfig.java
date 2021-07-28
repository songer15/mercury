package com.valor.mercury.manager.model.system.dispatcherMappingConfig;

/**
 * @author Gavin
 * 2020/8/18 14:46
 */
public class HDFSConfig {
    private String path;
    private int namedStrategy; //1:hour,2:day,3:week,4:month,5:quarter,6:year
    private String replacePath;

    public HDFSConfig() {
    }

    public HDFSConfig(String path,int namedStrategy,String replacePath) {
        this.path = path;
        this.namedStrategy=namedStrategy;
        this.replacePath=replacePath;
    }

    public String getReplacePath() {
        return replacePath;
    }

    public void setReplacePath(String replacePath) {
        this.replacePath = replacePath;
    }

    public int getNamedStrategy() {
        return namedStrategy;
    }

    public void setNamedStrategy(int namedStrategy) {
        this.namedStrategy = namedStrategy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
