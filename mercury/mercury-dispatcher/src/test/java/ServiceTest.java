import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.mfc.config.ConfigTools3;
import com.valor.mercury.common.client.EsClient;
import com.valor.mercury.common.client.HDFSClient;
import com.valor.mercury.common.model.Router;
import com.valor.mercury.common.util.DeflateUtils;
import com.valor.mercury.common.util.GeoIpUtils;
import com.valor.mercury.dispatcher.service.*;
import com.valor.mercury.sender.service.MercurySender;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;


public class ServiceTest {

    public static List<Map<String, Object>> data = new ArrayList<>();
    public static Router router = new Router();

    static {
        ConfigTools3.load("cfg");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", i);
            map.put("content", "this is a test 2");
            map.put("Espanol", "Español");
            map.put("CityName", "\\n23");
            map.put("CityName3", "\n23");
            map.put("LocalCreateTime", System.currentTimeMillis());
            data.add(map);
        }

        router.shardNum = 1;
        router.replicaNum = 2;
        router.indexNameStrategy = Router.IndexNameStrategy.NULL;
        router.indexMapping = "";
        router.currentIndexName = "test_zk_dd2";

        router.database = "junit_test2";
        router.measurement = "test";
        router.tags = "id";

        router.basePath = "/junit/espanol";
        router.fileNameStrategy = Router.FileNameStrategy.DAY;
        router.path = "/junit/espanol/1.txt";

        router.topicName = "junit";

    }

    @Before
    public void init () {

    }

    @Test
    public void consumerConfig() {
        ConfigManager configManager = new ConfigManager();
        configManager.fetchConfigs();
    }

    @Test
    public void esTest() {
        EsService service = new EsService();
        Assert.isTrue(service.route(data, router), "es test failed");
    }

    @Test
    public void closeIndices() throws IOException {
        EsClient esClient = new EsClient(ConfigTools3.getAsList("elasticsearch.url", ","));

        Map<String, Router.IndexNameStrategy> suffice = new HashMap<>();
        suffice.put(Router.IndexNameStrategy.DAY.getIndexNameSuffix(new Date()), Router.IndexNameStrategy.DAY);
        suffice.put(Router.IndexNameStrategy.WEEK.getIndexNameSuffix(new Date()), Router.IndexNameStrategy.WEEK);
        suffice.put(Router.IndexNameStrategy.MONTH.getIndexNameSuffix(new Date()), Router.IndexNameStrategy.MONTH );
        suffice.put(Router.IndexNameStrategy.QUARTER.getIndexNameSuffix(new Date()), Router.IndexNameStrategy.QUARTER );
        suffice.put(Router.IndexNameStrategy.YEAR.getIndexNameSuffix(new Date()), Router.IndexNameStrategy.YEAR);

        Set<String> rollingIndices = esClient.getIndicesByPattern("*-server");

        Set<String> shouldBeClose = new HashSet<>();
        Set<String> shouldBeOpen = new HashSet<>();
        Set<String> shouldNotBeClose = Sets.newHashSet("mfc_user_info-2018-server");

        for (String index : rollingIndices) {
            //默认需要关闭
            //index = "mfc_user_info-2018-server";
            String prefix = index.substring(0, index.indexOf("-20"));
//            String date = index.substring(index.indexOf("-20"), index.indexOf("-server"));
//            map.putIfAbsent(prefix, new HashSet<>());
//            map.get(prefix).add(date);
            boolean shouldClose = true;
            for (String suffix : suffice.keySet()) {
                Router.IndexNameStrategy strategy = suffice.get(suffix);
                //当该索引包含当前时间的后缀，且它是当前最大索引，保持open
                if (index.contains(suffix) && !rollingIndices.contains(prefix + strategy.getIndexNameSuffix(null, 1))) {
                    shouldClose = false;
                    break;
                }
            }
            if (shouldNotBeClose.contains(index)) {
                shouldClose = false;
            }
            if (shouldClose) {
                shouldBeClose.add(index);
            } else {
                shouldBeOpen.add(index);
            }
        }

        Set<String> toClose = new HashSet<>();
        Set<String> toOpen = new HashSet<>();
        for (String index : shouldBeClose) {
            if ("open".equals(esClient.getIndexStatus(index))) {
                System.out.println(index + "  should be  close");
                toClose.add(index);
            }
        }

        for (String index : shouldBeOpen) {
            if ("close".equals(esClient.getIndexStatus(index))) {
                System.out.println(index + "  should be  open");
                toOpen.add(index);
            }
        }

        System.out.println();
    }

    @Test
    public void DataES2HDFS() throws Exception{
        final String type = "uota_transfer";
        final String index = "uota_mrt_metric_app_action_message";
        EsClient esClient = new EsClient(Lists.newArrayList("http://metric01.mecury0data.xyz:9201"));

        Map<String, String[]> urls = new HashMap<>();
        urls.put("remote", new String[]{"http://metric01.mecury0data.xyz:2082"});

        MercurySender.init(urls, "es2hdfs", "es2hdfs", 5000, 100, 2, 5, 200000, true, false);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("LocalCreateTime");
        rangeQueryBuilder.from("1524288720000", true).to("1618912077939", true).timeZone("UTC");
        searchSourceBuilder.query(rangeQueryBuilder);
        searchSourceBuilder.sort("LocalCreateTime");
        searchSourceBuilder.size(5000);
        String scrollId = null;
        while (true) {
            Tuple<String, List<Map<String, Object>>> tuple = esClient.searchByScrollId(new String[]{index}, new String[]{"_doc"}, searchSourceBuilder,  scrollId);
            scrollId = tuple.v1();
            List<Map<String, Object>> data = tuple.v2();
            if (data.isEmpty())
                break;
            for (Map<String, Object>  e: data) {
                MercurySender.put("remote", type, e);
            }
            data.clear();
        }
        Thread.sleep(10000);
    }

    @Test
    public void DataHDFS2ES() throws Exception {
        final String type = "uota_transfer";
        final String hdfsPath = "hdfs://mycluster/hive_daily/tve_account_info_tve/dispatcher-executor01_-2021";
        HDFSClient hdfsClient  = new HDFSClient(
                "valor",
               "hdfs://mycluster",
               "hadoop/core-site.xml",
               "hadoop/hdfs-site.xml");
        Map<String, String[]> urls = new HashMap<>();
        urls.put("remote", new String[]{"http://metric01.mecury0data.xyz:2082"});

        MercurySender.init(urls, "es2hdfs", "es2hdfs", 5000, 100, 2, 5, 200000, true, false);
//        hdfsClient.read(hdfsPath);
        Thread.sleep(10000);
    }


    //获得
    @Test
    public void getVisualizationFromIndexpattern() throws Exception {
        EsClient esClient = new EsClient(Lists.newArrayList("http://metric01.mecury0data.xyz:9208"));

        SearchSourceBuilder indexSSB = new SearchSourceBuilder();
        indexSSB.query(QueryBuilders
                .boolQuery()
                .must(QueryBuilders
                        .queryStringQuery("*player_metric_info_btvbox*")
                        //支持通配符
                        .analyzeWildcard(true)
                        //默认搜索的字段
                        .defaultField("index-pattern.title")));
        indexSSB.size(5000);
        List<Map<String, Object>> index  = esClient.search(new String[]{".kibana"}, new String[]{"doc"}, indexSSB, true);

        Map<String, String> titles = new TreeMap<>();

        for (Map i : index) {
            String _id = (String)i.get("_id");
            if (_id.contains("index-pattern:")) {
                String index_pattern_id = _id.substring(_id.indexOf(":") + 1);
                SearchSourceBuilder visualizeSSB = new SearchSourceBuilder();
                visualizeSSB.query(QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders
                                .queryStringQuery("\"" + index_pattern_id + "\"")
                                .analyzeWildcard(true)
                                .defaultField("visualization.kibanaSavedObjectMeta.searchSourceJSON")
                                ));
                visualizeSSB.size(5000);
                List<Map<String, Object>> visualize  = esClient.search(new String[]{".kibana"}, new String[]{"doc"}, visualizeSSB, true);

                for (Map  v : visualize) {
                    if (v.containsKey("visualization")) {
                        String title =  (String)((Map)v.get("visualization")).get("title");
                        if (!titles.containsKey(title)) {
                        }
                        titles.put(title, "");
                    }
                }
            }
        }

        if (titles.isEmpty())
            System.out.println("没有对应的visualize");

        titles.forEach((k,v) -> {
            System.out.println(k);
        });

    }

    @Test
    public void createMappingTest() throws IOException {
        EsService esService = new EsService();
        XContentBuilder xBuilder = XContentFactory.jsonBuilder().prettyPrint();

        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
                "\"type\":\"date\"" );
        xBuilder.copyCurrentStructure(parser);
        xBuilder.value("1");


        router.indexMapping = "{    \"create_time\":{        \"type\":\"date\"    },    \"register_ts\":{        \"type\":\"date\"    },    \"update_time\":{        \"type\":\"date\"    },    \"expire_ts\":{        \"type\":\"date\"    }}";

        esService.createIndexIfAbsent(router, "hast");
    }



    @Test
    public void InfluxTest() throws InterruptedException {
        InfluxDBService service = new InfluxDBService();
        Assert.isTrue(service.route(data, router), "influx test failed");
        //异步发送，等待一段时间
        Thread.sleep(5000);
    }

    @Test
    public void hdfsTest() throws Exception {
        HDFSService service = new HDFSService();
        Assert.isTrue(service.route(data, router), "hdfs test failed");
    }

    @Test
    public void kafkaTest() {
        RouteService service = new KafkaService();
        Assert.isTrue(service.route(data, router), "kafka test failed");
    }

    @Test
    public void uncompress() throws Exception {
        String[] decode = new String[]{"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"};

        for (String s : decode) {
            System.out.println(DeflateUtils.unCompress(Base64.getDecoder().decode(s)));

        }
    }

    @Test
    public void ipParseTest() {
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("191.102.218.85"));
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("45.187.2.83"));
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("45.185.194.135"));
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("45.172.126.129"));
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("190.130.255.150"));
        org.junit.Assert.assertNotNull("", GeoIpUtils.getGeoIPData("2800:370:f4:fae0:6c15:b845:dc45:852b"));
    }



    @Test
    public void GeoIPTest() throws IOException, GeoIp2Exception {
        DatabaseReader city = new DatabaseReader.Builder(new File("data/GeoLite2-City.mmdb")).fileMode(Reader.FileMode.MEMORY_MAPPED).withCache(new CHMCache()).build();
        DatabaseReader asn = new DatabaseReader.Builder(new File("data/GeoLite2-ASN.mmdb")).fileMode(Reader.FileMode.MEMORY).withCache(new CHMCache()).build();

        InetAddress ipAddress = InetAddress.getByName("191.102.218.85");
        city.city(ipAddress);
        city.close();
        asn.asn(ipAddress);
        asn.close();


    }
}
