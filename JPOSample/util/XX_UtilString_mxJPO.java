import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilString_mxJPO {

    private static final String SYM_REL_MY_PART_DOCUMENT = "relationship_MyPartDocument";
    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";

    public XX_UtilString_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    private static MapList expandParts(Context context, String objectId, short level) throws Exception {
        DomainObject part = DomainObject.newInstance(context, objectId);
        return part.getRelatedObjects(context, schema(context, SYM_REL_MY_PART_STRUCTURE), "*",
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME),
                StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, level, null, null, 0);
    }

    public Boolean isEmptyValue(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String value = (String) programMap.get("value");
        return Boolean.valueOf(UIUtil.isNullOrEmpty(value));
    }

    public StringList splitValues(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkUtil.split((String) programMap.get("values"), ",");
    }

    public String getDocumentNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        StringList names = part.getInfoList(context, "from[" + schema(context, SYM_REL_MY_PART_DOCUMENT) + "].to.name");
        return FrameworkUtil.join(names, ", ");
    }

    public String toSingleLine(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String text = (String) programMap.get("text");
        if (UIUtil.isNullOrEmpty(text)) {
            return "";
        }
        String withoutCrLf = FrameworkUtil.findAndReplace(text, "\r\n", " ");
        return FrameworkUtil.findAndReplace(withoutCrLf, "\n", " ");
    }

    public Map getShortDescription(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String description = part.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
        Map result = new HashMap();
        result.put("byWord", FrameworkUtil.stripString(description, 50, FrameworkUtil.StripStringType.WORD_STRIP));
        result.put("byChar", FrameworkUtil.stripString(description, 50, FrameworkUtil.StripStringType.CHAR_STRIP));
        return result;
    }

    public String cutMailSubject(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String subject = (String) programMap.get("subject");
        if (UIUtil.isNullOrEmpty(subject)) {
            return "";
        }
        return FrameworkUtil.getUtf8TruncatedString(subject, 250);
    }

    public Map getPartBasics(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        StringList selects = StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION,
                DomainConstants.SELECT_CURRENT);
        return part.getInfo(context, selects);
    }

    public MapList getNamesOfIds(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList idList = FrameworkUtil.split((String) programMap.get("objectIds"), ",").filterEmpty();
        return DomainObject.getInfo(context, idList.toStringArray(),
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
    }

    public StringList removeEmptyValues(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList values = FrameworkUtil.split((String) programMap.get("values"), ",");
        return values.filterEmpty();
    }

    public StringList removeDuplicates(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList unique = new StringList();
        unique.addAllExclusive(FrameworkUtil.split((String) programMap.get("values"), ","));
        return unique;
    }

    public StringList sortValues(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList values = FrameworkUtil.split((String) programMap.get("values"), ",");
        values.sort();
        return values;
    }

    public MapList getChildrenByName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList children = expandParts(context, (String) programMap.get("objectId"), (short) 1);
        children.sort(DomainConstants.SELECT_NAME, "ascending", "string");
        return children;
    }

    public MapList getChildrenByTypeAndName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList children = expandParts(context, (String) programMap.get("objectId"), (short) 1);
        children.addSortKey(DomainConstants.SELECT_TYPE, "ascending", "string");
        children.addSortKey(DomainConstants.SELECT_NAME, "descending", "string");
        children.sort();
        return children;
    }

    public MapList getSortedStructure(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList structure = expandParts(context, (String) programMap.get("objectId"), (short) 0);
        structure.sortStructure(DomainConstants.SELECT_NAME, "ascending", "string");
        return structure;
    }
}
