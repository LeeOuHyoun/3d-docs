import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilArgs_mxJPO {

    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";

    public XX_UtilArgs_mxJPO(Context context, String[] args) throws Exception {
    }

    private static Map splitTableRowId(String rowId) {
        StringList parts = FrameworkUtil.split(rowId, "|");
        Map row = new HashMap();
        if (parts.size() == 4) {
            row.put("relId", parts.get(0));
            row.put("objectId", parts.get(1));
            row.put("parentId", parts.get(2));
            row.put("rowPosition", parts.get(3));
        } else if (parts.size() == 3) {
            row.put("objectId", parts.get(0));
            row.put("parentId", parts.get(1));
            row.put("rowPosition", parts.get(2));
        } else if (parts.size() == 2) {
            row.put("relId", parts.get(0));
            row.put("objectId", parts.get(1));
        } else {
            row.put("objectId", rowId);
        }
        return row;
    }

    public Map getArgs(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String mode = (String) programMap.get("mode");
        if (UIUtil.isNullOrEmpty(mode)) {
            mode = "view";
        }
        Map result = new HashMap();
        result.put("objectId", objectId);
        result.put("hasObjectId", Boolean.valueOf(UIUtil.isNotNullAndNotEmpty(objectId)));
        result.put("mode", mode);
        return result;
    }

    public StringList getChildIds(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String relStructure = PropertyUtil.getSchemaProperty(context, SYM_REL_MY_PART_STRUCTURE);
        return part.getInfoList(context, "from[" + relStructure + "].to.id");
    }

    public Integer countChildren(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map params = new HashMap();
        params.put("objectId", programMap.get("objectId"));
        StringList childIds = (StringList) JPO.invoke(context, "XX_UtilArgs", null, "getChildIds",
                JPO.packArgs(params), StringList.class);
        return Integer.valueOf(childIds.size());
    }

    public StringList getSelectedObjectIds(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String[] rowIds = (String[]) programMap.get("emxTableRowId");
        return StringList.create(FrameworkUtil.getSplitTableRowIds(rowIds));
    }

    public MapList splitTableRowIds(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String[] rowIds = (String[]) programMap.get("emxTableRowId");
        MapList rows = new MapList();
        if (rowIds == null) {
            return rows;
        }
        for (String rowId : rowIds) {
            rows.add(splitTableRowId(rowId));
        }
        return rows;
    }

    public Boolean isObjectId(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String value = (String) programMap.get("value");
        if (UIUtil.isNullOrEmpty(value)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(FrameworkUtil.isObjectId(context, value));
    }

    public String toPhysicalId(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkUtil.getPIDfromOID(context, (String) programMap.get("objectId"));
    }

    public String toObjectId(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkUtil.getOIDfromPID(context, (String) programMap.get("physicalId"));
    }
}
