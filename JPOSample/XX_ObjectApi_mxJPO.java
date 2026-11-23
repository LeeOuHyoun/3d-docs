import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_ObjectApi_mxJPO {

    private static final String SYM_TYPE_MY_PART = "type_MyPart";
    private static final String SYM_TYPE_MY_DOCUMENT = "type_MyDocument";
    private static final String SYM_REL_MY_PART_DOCUMENT = "relationship_MyPartDocument";
    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";
    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";
    private static final String SYM_ATTR_MY_QUANTITY = "attribute_MyQuantity";
    private static final String SYM_ATTR_TITLE = "attribute_Title";

    public XX_ObjectApi_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    public Map getPartInfo(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_REVISION,
                DomainConstants.SELECT_CURRENT, DomainObject.getAttributeSelect(schema(context, SYM_ATTR_MY_STATUS))));
        info.put("documentIds", part.getInfoList(context, "from[" + schema(context, SYM_REL_MY_PART_DOCUMENT) + "].to.id"));
        info.put("isMyPart", Boolean.valueOf(part.isKindOf(context, schema(context, SYM_TYPE_MY_PART))));
        return info;
    }

    public MapList getChildParts(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        MapList children = part.getRelatedObjects(context, schema(context, SYM_REL_MY_PART_STRUCTURE), "*",
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT),
                StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID,
                        DomainRelationship.getAttributeSelect(schema(context, SYM_ATTR_MY_QUANTITY))),
                false, true, (short) 1, null, null, 0);
        children.sort(DomainConstants.SELECT_NAME, "ascending", "String");
        return children;
    }

    public MapList getObjectsInfo(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String[] ids = FrameworkUtil.split((String) programMap.get("objectIds"), ",").toStringArray();
        return DomainObject.getInfo(context, ids,
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, DomainConstants.SELECT_OWNER));
    }

    public MapList findLatestParts(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String name = (String) programMap.get("name");
        return DomainObject.findObjects(context, schema(context, SYM_TYPE_MY_PART), UIUtil.isNullOrEmpty(name) ? "*" : name,
                "*", "*", null, "revision==last", true, StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME));
    }

    public Map createDocument(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        DomainObject document = DomainObject.newInstance(context);
        Map attributes = new HashMap();
        attributes.put(schema(context, SYM_ATTR_TITLE), (String) programMap.get("title"));
        ContextUtil.startTransaction(context, true);
        try {
            DomainRelationship rel = document.createAndConnect(context, schema(context, SYM_TYPE_MY_DOCUMENT),
                    schema(context, SYM_REL_MY_PART_DOCUMENT), part, true);
            document.setAttributeValues(context, attributes);
            ContextUtil.commitTransaction(context);
            Map result = new HashMap();
            result.put("id", document.getId(context));
            result.put("relId", rel.getName());
            return result;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }

    public void updatePart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Map attributes = new HashMap();
        attributes.put(schema(context, SYM_ATTR_MY_STATUS), (String) programMap.get("status"));
        part.setAttributeValues(context, attributes);
        part.setDescription(context, (String) programMap.get("description"));
    }

    public void updateQuantity(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainRelationship.setAttributeValue(context, (String) programMap.get("relId"),
                schema(context, SYM_ATTR_MY_QUANTITY), (String) programMap.get("quantity"));
    }

    public Integer releasePart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String policy = part.getInfo(context, DomainConstants.SELECT_POLICY);
        return Integer.valueOf(part.setState(context, PropertyUtil.getSchemaProperty(context, "policy", policy, "state_Release")));
    }

    public String revisePart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        return part.reviseObject(context, false).getObjectId(context);
    }

    public void deleteParts(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject.deleteObjects(context, FrameworkUtil.split((String) programMap.get("objectIds"), ",").toStringArray());
    }

    public void removeDocuments(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        StringList relIds = part.getInfoList(context, "from[" + schema(context, SYM_REL_MY_PART_DOCUMENT) + "].id");
        if (!relIds.isEmpty()) {
            DomainRelationship.disconnect(context, relIds.toStringArray());
        }
    }

    public String getModifiedLabel(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        if (UIUtil.isNullOrEmpty(objectId)) {
            return DomainConstants.EMPTY_STRING;
        }
        Map info = new DomainObject(objectId).getInfo(context,
                StringList.create(DomainConstants.SELECT_OWNER, DomainConstants.SELECT_MODIFIED));
        String owner = PersonUtil.getFullName(context, (String) info.get(DomainConstants.SELECT_OWNER));
        String modified = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(eMatrixDateFormat.getJavaDate((String) info.get(DomainConstants.SELECT_MODIFIED)));
        return XSSUtil.encodeForHTML(context, owner + " (" + modified + ")");
    }
}
