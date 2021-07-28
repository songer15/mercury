package com.valor.mercury.common.model;

public class  Constants{

    public enum Task{

        TASK_STATUS_LINING("LINING"),
        TASK_STATUS_RUNNING("RUNNING");

        private String value;

        Task(String value) { this.value = value; }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

     public enum CustomCode {

        SUCCESS(200), 
        NESTED_ERROR(300),
        INVALID_CONFIG_ERROR(400),
        JSON_DESERIALIZE(401),
        JSON_SERIALIZE(402),
        UNDEFINED_EROOR(500),
        AUTH_ERROR(501),
        TASK_REPORT_ERROR(502),
        TASK_GENERATE_ERROR(503),
        QUEUE_FULL_ERROR(600),
        QUEUE_POLL_ERROR(600),
        ES_INDEX_CREATION_ERROR(701),
        ES_INDEX_BULKREQUEST_ERROR(702),
        ES_INDEX_LOADMAPPING_ERROR(703),
        ES_INDEX_IO_ERROR(704),
        ES_INDEX_PUTMAPPING_ERROR(705);

        private int value;

        CustomCode(int value) { this.value = value; }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }



}
