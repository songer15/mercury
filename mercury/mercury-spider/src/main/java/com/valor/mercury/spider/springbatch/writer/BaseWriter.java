package com.valor.mercury.spider.springbatch.writer;


import com.valor.mercury.spider.springbatch.BaseBatchComponent;
import org.springframework.batch.item.ItemWriter;

import java.util.HashMap;

public abstract class BaseWriter extends BaseBatchComponent implements ItemWriter<HashMap> {

}
