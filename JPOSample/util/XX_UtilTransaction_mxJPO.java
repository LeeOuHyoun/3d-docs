import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;

public class XX_UtilTransaction_mxJPO {

    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";

    public XX_UtilTransaction_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    public void setStatusTogether(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        DomainObject document = DomainObject.newInstance(context, (String) programMap.get("documentId"));
        String attrStatus = schema(context, SYM_ATTR_MY_STATUS);
        String status = (String) programMap.get("status");
        ContextUtil.startTransaction(context, true);
        try {
            part.setAttributeValue(context, attrStatus, status);
            document.setAttributeValue(context, attrStatus, status);
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }

    public Boolean isInTransaction(Context context, String[] args) throws Exception {
        return Boolean.valueOf(ContextUtil.isTransactionActive(context));
    }

    public void setCheckedByAsSystem(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String user = context.getUser();
        ContextUtil.pushContext(context);
        try {
            part.setAttributeValue(context, schema(context, SYM_ATTR_MY_STATUS), "Checked by " + user);
        } finally {
            ContextUtil.popContext(context);
        }
    }

    public Map getUserDuringPush(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map result = new HashMap();
        result.put("before", context.getUser());
        ContextUtil.pushContext(context, (String) programMap.get("userName"), null, null);
        try {
            result.put("during", context.getUser());
            result.put("loggedIn", PropertyUtil.getRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, true));
        } finally {
            ContextUtil.popContext(context);
        }
        result.put("after", context.getUser());
        return result;
    }
}
