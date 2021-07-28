package com.valor.mercury.spider.springbatch.reader;

import com.valor.mercury.spider.springbatch.BaseBatchComponent;
import org.springframework.batch.item.ItemReader;

import java.util.HashMap;

public abstract class BaseReader extends BaseBatchComponent implements ItemReader<HashMap<String, Object>> {

}
