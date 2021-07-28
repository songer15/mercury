package com.valor.mercury.manager.model.system;

/**
 * @author Gavin
 * 2020/8/25 17:27
 */
public class ScriptTask {
    private String name;
    private String description;
    private String executor;
    private String incValue;
    private Long fileId;
    private String config;
    private String entryClass;

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getIncValue() {
        return incValue;
    }

    public void setIncValue(String incValue) {
        this.incValue = incValue;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "ScriptTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", executor='" + executor + '\'' +
                ", incValue='" + incValue + '\'' +
                ", fileId=" + fileId +
                ", config='" + config + '\'' +
                '}';
    }
}
