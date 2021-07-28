package com.valor.mercury.script.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.valor.mercury.script.MetricAnalyse
import com.valor.mercury.script.util.ESClient
import com.valor.mercury.script.util.HDFSClient
import com.valor.mercury.script.util.HDFSReader
import com.valor.mercury.script.util.IncrementValueHandler
import org.apache.logging.log4j.util.Strings

class HDFS2ES implements MetricAnalyse {

    ObjectMapper objectMapper = new ObjectMapper()

    @Override
    void run(String[] args) throws Exception {
        HDFSClient hdfsClient = new HDFSClient()
        ESClient esClient = new ESClient()
        def path = args[1]
        def indexName = args[2]
        def startTime = Long.parseLong(args[3])
        def endTime = Long.parseLong(args[4])

        try {
            hdfsClient.readValue(path, new HDFSReader() {
                def list = new ArrayList()

                @Override
                void read(String value) {
                    try {
                        Map<String, Object> result = objectMapper.readValue(value, Map.class)

                        if (result.containsKey("actionTime") && result.get("actionTime") instanceof Long) {
                            Long actionTime = (Long) result.get("actionTime")
                            if (actionTime > startTime && actionTime < endTime)
                                list.add(result)
                            if (list.size() > 1000) {
                                esClient.write(indexName, null, list)
                                list.clear()
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage())
                        IncrementValueHandler.setErrorMsg(this.getClass().getName(), e.getMessage())
                    }
                }

                @Override
                void finish(String line) {
                    try {
                        if (Strings.isNotEmpty(line)) {
                            Map<String, Object> result = objectMapper.readValue(line, Map.class)
                            list.add(result)
                        }
                        esClient.write(indexName, idField, list)
                        list.clear()
                    }
                    catch (Exception e) {
                        System.out.println(e.getMessage())
                        IncrementValueHandler.setErrorMsg(this.getClass().getName(), e.getMessage())
                    }
                }
            })
        } catch (Exception e) {
            System.out.println(e.getMessage())
            IncrementValueHandler.setErrorMsg(this.getClass().getName(), e.getMessage())
            System.exit(1)
        }

        hdfsClient.close()
        esClient.close()
    }
}
