import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilMql_mxJPO {

    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";
    private static final String RPE_SKIP_STATUS_CHECK = "XX_SKIP_STATUS_CHECK";

    public XX_UtilMql_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    public String getCurrentState(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        return MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectId, DomainConstants.SELECT_CURRENT);
    }

    public Map getNameAndState(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String output = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4", objectId,
                DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT, "|");
        StringList values = FrameworkUtil.split(output, "|");
        Map result = new HashMap();
        result.put("name", values.get(0));
        result.put("current", values.get(1));
        return result;
    }

    public Map getSelectValues(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList selects = FrameworkUtil.split((String) programMap.get("selects"), ",");
        List<String> values = new ArrayList<String>();
        values.add((String) programMap.get("objectId"));
        String command = "print bus $1 select";
        for (String select : selects) {
            values.add(select);
            command = command + " $" + values.size();
        }
        values.add("|");
        command = command + " dump $" + values.size();
        StringList output = FrameworkUtil.split(MqlUtil.mqlCommand(context, command, values), "|");
        Map result = new HashMap();
        for (int i = 0; i < selects.size(); i++) {
            result.put(selects.get(i), output.get(i));
        }
        return result;
    }

    public String getTransactionStatus(Context context, String[] args) throws Exception {
        return MqlUtil.mqlCommand(context, "print transaction", false, false);
    }

    public String getOwnerAsSystem(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        return MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, objectId, DomainConstants.SELECT_OWNER);
    }

    public void setStatusWithoutTrigger(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String status = (String) programMap.get("status");
        MqlUtil.mqlCommand(context, false, true, "modify bus $1 $2 $3", false, objectId,
                schema(context, SYM_ATTR_MY_STATUS), status);
    }

    public void setStatusWithFlag(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        PropertyUtil.setRPEValue(context, RPE_SKIP_STATUS_CHECK, "true", false);
        try {
            part.setAttributeValue(context, schema(context, SYM_ATTR_MY_STATUS), (String) programMap.get("status"));
        } finally {
            PropertyUtil.unsetRPEValue(context, RPE_SKIP_STATUS_CHECK, false);
        }
    }

    public int checkStatus(Context context, String[] args) throws Exception {
        String skip = PropertyUtil.getRPEValue(context, RPE_SKIP_STATUS_CHECK, false);
        if ("true".equalsIgnoreCase(skip)) {
            return 0;
        }
        MqlUtil.mqlCommand(context, "notice $1", "Use setStatusWithFlag to change My Status.");
        return 1;
    }
}
