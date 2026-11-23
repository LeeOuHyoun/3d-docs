import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_GetObjectDetail_mxJPO {

    private static final StringList BASIC_SELECTS = new StringList(new String[] {
        DomainConstants.SELECT_ID,
        DomainConstants.SELECT_TYPE,
        DomainConstants.SELECT_NAME,
        DomainConstants.SELECT_REVISION,
        DomainConstants.SELECT_POLICY,
        DomainConstants.SELECT_CURRENT,
        DomainConstants.SELECT_OWNER,
        DomainConstants.SELECT_VAULT,
        DomainConstants.SELECT_DESCRIPTION,
        DomainConstants.SELECT_ORIGINATED,
        DomainConstants.SELECT_MODIFIED,
        DomainConstants.SELECT_ORIGINATOR,
        DomainConstants.SELECT_ORGANIZATION,
        DomainConstants.SELECT_PROJECT
    });

    public XX_GetObjectDetail_mxJPO(Context context, String[] args) throws Exception {
    }

    public Map getObjectDetail(Context context, String[] args) throws Exception {
        Map paramMap = (Map) JPO.unpackArgs(args);
        DomainObject dobj = DomainObject.newInstance(context, (String) paramMap.get("pid"));
        Map result = new HashMap();
        result.put("basic", dobj.getInfo(context, BASIC_SELECTS));
        result.put("attributes", dobj.getAttributeMap(context));
        return result;
    }
}
