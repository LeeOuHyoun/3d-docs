import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;

public class XX_UpdateAttribute_mxJPO {

    private static final String SYM_ATTR = "attribute_MyStatus";

    public XX_UpdateAttribute_mxJPO(Context context, String[] args) throws Exception {
    }

    public Boolean update(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        new DomainObject((String) paramMap.get("pid")).setAttributeValue(context,
                PropertyUtil.getSchemaProperty(context, SYM_ATTR), (String) paramMap.get("value"));
        return Boolean.TRUE;
    }
}
