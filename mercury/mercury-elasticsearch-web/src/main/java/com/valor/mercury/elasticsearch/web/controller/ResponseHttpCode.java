package com.valor.mercury.elasticsearch.web.controller;

public class ResponseHttpCode {
    public static final int SERVER_ERROR = 555; // Server error
    public static final String SERVER_ERROR_MSG = "Server Internal Error.";

    public static final int OK = 0;
    public static final String OK_MSG = "Operation is successful.";

    public static final int RET_ELASTICSEARCH_DATA_VIEW = 1001;

    public static final int ERR_ELASTICSEARCH_DATA_VIEW_EMPTY_INDEX = 1;//
    public static final String ERR_ELASTICSEARCH_DATA_VIEW_EMPTY_INDEX_MSG = "Empty Index Selected.";

    public static final int ERR_ELASTICSEARCH_DATA_VIEW_INDEX_NOT_EXISTS = 2;//
    public static final String ERR_ELASTICSEARCH_DATA_VIEW_INDEX_NOT_EXISTS_MSG = "Not Exists Index.";


    public static final int RET_ELASTICSEARCH_BASIC_QUERY = 1002;

    public static final int ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX = 1;//
    public static final String ERR_ELASTICSEARCH_BASIC_QUERY_EMPTY_INDEX_MSG = "Empty Index Selected.";

    public static final int ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS = 2;//
    public static final String ERR_ELASTICSEARCH_BASIC_QUERY_INDEX_NOT_EXISTS_MSG = "Not Exists Index.";

    public static final int ERR_ELASTICSEARCH_BASIC_QUERY_DATE_NOT_EXISTS = 3;//
    public static final String ERR_ELASTICSEARCH_BASIC_QUERY_DATE_NOT_EXISTS_MSG = "Must Selected Date Field.";

    public static final int ERR_ELASTICSEARCH_BASIC_QUERY_DAYS_TOO_LONG = 4;//
    public static final String ERR_ELASTICSEARCH_BASIC_QUERY_DAYS_TOO_LONG_MSG = "Date cycle too long.";

}
