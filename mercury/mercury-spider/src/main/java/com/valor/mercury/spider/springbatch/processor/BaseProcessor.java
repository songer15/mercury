package com.valor.mercury.spider.springbatch.processor;



import com.valor.mercury.spider.springbatch.BaseBatchComponent;
import org.springframework.batch.item.ItemProcessor;
import java.util.HashMap;

public abstract class BaseProcessor extends BaseBatchComponent implements ItemProcessor<HashMap<String,Object>, HashMap<String,Object>> {

}
