import com.valor.mercury.spider.model.SpiderJob;
import com.valor.mercury.spider.springbatch.processor.TimeIncItemProcessor;

public class DateFormatTest {

    @org.junit.Test
    public void dateParse() throws Exception {

        SpiderJob spiderJob = new SpiderJob();
        spiderJob.processor = "";

        for (int i = 0; i < 20;i++) {
            TimeIncItemProcessor timeIncItemProcessor = new TimeIncItemProcessor();
//            timeIncItemProcessor.init();
        }

    }
}
