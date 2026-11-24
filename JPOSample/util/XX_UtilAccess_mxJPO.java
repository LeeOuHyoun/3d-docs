import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilAccess_mxJPO {

    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";
    private static final String COMMENT = DomainAccess.COMMENT_MULTIPLE_OWNERSHIP;

    public XX_UtilAccess_mxJPO(Context context, String[] args) throws Exception {
    }

    public void shareWithCollabSpace(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainAccess.createObjectOwnership(context, (String) programMap.get("objectId"), (String) programMap.get("organization"),
                (String) programMap.get("project"), (String) programMap.get("access"), COMMENT);
    }

    public void shareWithPerson(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String personId = PersonUtil.getPersonObjectID(context, (String) programMap.get("userName"));
        DomainAccess.createObjectOwnership(context, (String) programMap.get("objectId"), personId,
                (String) programMap.get("access"), COMMENT);
    }

    public void inheritAccess(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainAccess.createObjectOwnership(context, (String) programMap.get("objectId"), (String) programMap.get("fromId"), COMMENT);
    }

    public void unshareWithCollabSpace(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainAccess.deleteObjectOwnership(context, (String) programMap.get("objectId"), (String) programMap.get("organization"),
                (String) programMap.get("project"), COMMENT);
    }

    public void unshareWithPerson(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String personProject = programMap.get("userName") + "_PRJ";
        DomainAccess.deleteObjectOwnership(context, (String) programMap.get("objectId"), "", personProject, COMMENT);
    }

    public void stopInheritingAccess(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainAccess.deleteObjectOwnership(context, (String) programMap.get("objectId"), (String) programMap.get("fromId"), COMMENT);
    }

    public Boolean isSharedWithPerson(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String personProject = programMap.get("userName") + "_PRJ";
        return Boolean.valueOf(DomainAccess.hasObjectOwnership(context, (String) programMap.get("objectId"), "", personProject, COMMENT));
    }

    public MapList getSharingList(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return DomainAccess.getAccessSummaryList(context, (String) programMap.get("objectId"));
    }

    public void clearSharing(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainAccess.clearMultipleOwnership(context, (String) programMap.get("objectId"));
    }

    public Map getAccessNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String ownerAccess = DomainAccess.getOwnerAccessName(context, objectId);
        Map result = new HashMap();
        result.put("names", DomainAccess.getLogicalNames(context, objectId));
        result.put("reader", DomainAccess.getReaderAccessName(context, objectId));
        result.put("owner", ownerAccess);
        result.put("ownerMasks", DomainAccess.getPhysicalAccessMasks(context, objectId, ownerAccess));
        return result;
    }

    public Boolean canModify(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        return Boolean.valueOf(FrameworkUtil.hasAccess(context, part, "modify"));
    }

    public MapList getChildPartsWithAccess(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        return part.getRelatedObjects(context, PropertyUtil.getSchemaProperty(context, SYM_REL_MY_PART_STRUCTURE), "*",
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME, "current.access[modify]"),
                null, false, true, (short) 1, null, null, 0);
    }

    public ArrayList findUsersWithoutRead(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        ArrayList persons = new ArrayList(FrameworkUtil.split((String) programMap.get("userNames"), ","));
        ArrayList accesses = new ArrayList();
        accesses.add("read");
        return AccessUtil.hasAccess(context, (String) programMap.get("objectId"), persons, accesses);
    }

    public void moveToCollabSpace(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        part.setPrimaryOwnership(context, (String) programMap.get("project"), (String) programMap.get("organization"));
    }
}
