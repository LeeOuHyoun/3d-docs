import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.mxType;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilSchema_mxJPO {

    private static final String SYM_TYPE_MY_PART = "type_MyPart";
    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";
    private static final String SYM_STATE_RELEASE = "state_Release";

    public XX_UtilSchema_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    private static Map getPolicyAndCurrent(Context context, String objectId) throws Exception {
        DomainObject part = DomainObject.newInstance(context, objectId);
        return part.getInfo(context, StringList.create(DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT));
    }

    public String toActualName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return schema(context, (String) programMap.get("symbolicName"));
    }

    public String toSymbolicName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String adminType = (String) programMap.get("adminType");
        String name = (String) programMap.get("name");
        return FrameworkUtil.getAliasForAdmin(context, adminType, name, true);
    }

    public Boolean isReleased(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map info = getPolicyAndCurrent(context, (String) programMap.get("objectId"));
        String policy = (String) info.get(DomainConstants.SELECT_POLICY);
        String current = (String) info.get(DomainConstants.SELECT_CURRENT);
        String release = PropertyUtil.getSchemaProperty(context, "policy", policy, SYM_STATE_RELEASE);
        return Boolean.valueOf(current.equals(release));
    }

    public String getCurrentStateSymbolicName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map info = getPolicyAndCurrent(context, (String) programMap.get("objectId"));
        String policy = (String) info.get(DomainConstants.SELECT_POLICY);
        String current = (String) info.get(DomainConstants.SELECT_CURRENT);
        return FrameworkUtil.reverseLookupStateName(context, policy, current);
    }

    public String getTypeProperty(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String type = (String) programMap.get("type");
        String property = (String) programMap.get("property");
        return PropertyUtil.getAdminProperty(context, "type", type, property);
    }

    public Boolean isMyPartType(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String type = (String) programMap.get("type");
        return Boolean.valueOf(mxType.isOfParentType(context, type, schema(context, SYM_TYPE_MY_PART)));
    }

    public StringList getPolicyNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList policies = mxType.getPolicies(context, (String) programMap.get("type"), false);
        StringList names = new StringList();
        for (Object o : policies) {
            Map policy = (Map) o;
            names.add((String) policy.get("name"));
        }
        return names;
    }

    public Map getDefaultPolicy(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map policy = mxType.getDefaultPolicy(context, (String) programMap.get("type"), false);
        Map result = new HashMap();
        if (policy != null) {
            result.put("policy", policy.get("name"));
            result.put("firstRevision", policy.get("revision"));
        }
        return result;
    }

    public MapList getTypeAttributes(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return mxType.getAttributes(context, (String) programMap.get("type"));
    }

    public Boolean hasStatusAttribute(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String type = (String) programMap.get("type");
        return Boolean.valueOf(FrameworkUtil.isAttributeOnType(context, type, schema(context, SYM_ATTR_MY_STATUS)));
    }

    public StringList getRanges(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkUtil.getRanges(context, schema(context, (String) programMap.get("symbolicAttribute")));
    }

    public String getStatus(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String statusSelect = DomainObject.getAttributeSelect(schema(context, SYM_ATTR_MY_STATUS));
        Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, statusSelect));
        return (String) info.get(statusSelect);
    }

    public Map getUnitInfo(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String attribute = schema(context, (String) programMap.get("symbolicAttribute"));
        Map result = new HashMap();
        result.put("hasUnit", Boolean.valueOf(UOMUtil.isAssociatedWithDimension(context, attribute)));
        result.put("dbUnit", UOMUtil.getDBunit(context, attribute));
        result.put("units", UOMUtil.getDimensionUnits(context, attribute));
        result.put("inputUnit", UOMUtil.getInputunit(context, (String) programMap.get("objectId"), attribute));
        return result;
    }

    public String toDbUnitValue(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String attribute = schema(context, (String) programMap.get("symbolicAttribute"));
        String value = (String) programMap.get("value");
        String unit = (String) programMap.get("unit");
        return UOMUtil.convertToDefaultUnit(context, attribute, value, unit);
    }
}
