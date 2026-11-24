import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

public class XX_DomainObjectApi_mxJPO {

    private static final String SYM_TYPE_MY_PART = "type_MyPart";
    private static final String SYM_TYPE_MY_DOCUMENT = "type_MyDocument";
    private static final String SYM_REL_MY_PART_DOCUMENT = "relationship_MyPartDocument";
    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";

    public XX_DomainObjectApi_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    private static String[] splitIds(Map programMap, String key) throws Exception {
        return FrameworkUtil.split((String) programMap.get(key), ",").toStringArray();
    }

    public Map getPartSummary(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Map summary = new HashMap();
        summary.put("isPart", Boolean.valueOf(part.isKindOf(context, schema(context, SYM_TYPE_MY_PART))));
        summary.put("isLastRevision", Boolean.valueOf(part.isLastRevision(context)));
        summary.put("hasDocument", Boolean.valueOf(part.hasRelatedObjects(context, schema(context, SYM_REL_MY_PART_DOCUMENT), true)));
        summary.put("isUsed", Boolean.valueOf(part.hasRelatedObjects(context, schema(context, SYM_REL_MY_PART_STRUCTURE), false)));
        summary.put("attributes", part.getAttributeMap(context, true));
        return summary;
    }

    public MapList getNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        StringList selects = StringList.create(DomainConstants.SELECT_NAME, DomainConstants.SELECT_CURRENT);
        MapList rows = new MapList();
        DomainObject obj = DomainObject.newInstance(context);
        for (String id : splitIds(programMap, "objectIds")) {
            if (UIUtil.isNotNullAndNotEmpty(id)) {
                obj.setId(id);
                rows.add(obj.getInfo(context, selects));
            }
        }
        return rows;
    }

    public String getIdByName(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        BusinessObject bo = new BusinessObject(schema(context, SYM_TYPE_MY_PART), (String) programMap.get("name"),
                (String) programMap.get("revision"), context.getVault().getName());
        return new DomainObject(bo).getId(context);
    }

    public String getLatestRevisionId(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        DomainObject part = DomainObject.newInstance(context, objectId);
        if (part.isLastRevision(context)) {
            return objectId;
        }
        return part.getLastRevision(context).getObjectId(context);
    }

    public MapList getRevisionHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String documentIds = "from[" + schema(context, SYM_REL_MY_PART_DOCUMENT) + "].to.id";
        return part.getRevisionsInfo(context,
                StringList.create(DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_MODIFIED),
                StringList.create(documentIds));
    }

    public Map getFirstDocument(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        return part.getRelatedObject(context, schema(context, SYM_REL_MY_PART_DOCUMENT), true,
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
                StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID));
    }

    public Map connectDocuments(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        return part.addRelatedObjects(context, new RelationshipType(schema(context, SYM_REL_MY_PART_DOCUMENT)), true,
                splitIds(programMap, "documentIds"));
    }

    public String replaceDocument(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String documentId = (String) programMap.get("documentId");
        DomainRelationship rel = part.setRelatedObject(context, schema(context, SYM_REL_MY_PART_DOCUMENT), true,
                UIUtil.isNullOrEmpty(documentId) ? null : documentId);
        return rel == null ? DomainConstants.EMPTY_STRING : rel.getName();
    }

    public String createNumberedDocument(Context context, String[] args) throws Exception {
        String name = DomainObject.getAutoGeneratedName(context, SYM_TYPE_MY_DOCUMENT, "");
        DomainObject document = DomainObject.newInstance(context);
        document.createObject(context, schema(context, SYM_TYPE_MY_DOCUMENT), name, null, null, null);
        return document.getId(context);
    }

    public String copyPart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        BusinessObject copy = part.cloneObject(context, (String) programMap.get("name"), null, null, false, false);
        return copy.getObjectId(context);
    }

    public void renamePart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String name = (String) programMap.get("name");
        if (UIUtil.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name is empty");
        }
        DomainObject.newInstance(context, (String) programMap.get("objectId")).setName(context, name);
    }

    public StringList getUnsignedSignatures(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Map info = part.getInfo(context, StringList.create(DomainConstants.SELECT_CURRENT, DomainConstants.SELECT_POLICY));
        String release = PropertyUtil.getSchemaProperty(context, "policy", (String) info.get(DomainConstants.SELECT_POLICY),
                "state_Release");
        StringList unsigned = new StringList();
        for (Object o : part.getSignaturesDetails(context, (String) info.get(DomainConstants.SELECT_CURRENT), release)) {
            Map signature = (Map) o;
            if (!"TRUE".equalsIgnoreCase((String) signature.get("approved"))) {
                unsigned.add((String) signature.get("name"));
            }
        }
        return unsigned;
    }
}
