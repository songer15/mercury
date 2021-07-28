import com.valor.mercury.common.model.Router;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

public class IndexTest {

    @Test
    public void testIndexName() {
        Router router = new Router();
        router.indexName = "index";
        router.indexNameStrategy = Router.IndexNameStrategy.QUARTER;
        Date date = new Date();
        date.setMonth(Calendar.JANUARY);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-1-server"), "");
        date.setMonth(Calendar.FEBRUARY);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-1-server"), "");
        date.setMonth(Calendar.MARCH);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-1-server"), "");
        date.setMonth(Calendar.APRIL);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-2-server"), "");
        date.setMonth(Calendar.JUNE);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-2-server"), "");
        date.setMonth(Calendar.DECEMBER);
        Assert.isTrue(router.indexNameStrategy.getIndexNameSuffix(date).endsWith("2021-4-server"), "");
    }
}
