package com.vms.metric.analyse;

import com.mfc.config.ConfigTools3;
import com.vms.metric.analyse.model.WorkItem;
import com.vms.metric.analyse.service.job.live.DAUAnalyse;
import com.vms.metric.analyse.service.job.live.DRQAnalyse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AnalyseApplicationTests {

    @Autowired
    private DAUAnalyse dauAnalyse;

    @Autowired
    private DRQAnalyse drqAnalyse;

    static {
        ConfigTools3.load("cfg");
    }


    @Test
    public void DAUtest() {
        WorkItem workItem = new WorkItem();
        workItem.setPre("2021-03-13");
        dauAnalyse.execute(workItem);
    }

    @Test
    public void DRQtest() {
        WorkItem workItem = new WorkItem();
        workItem.setPre("2021-01-02");
        drqAnalyse.execute(workItem);
    }


}
