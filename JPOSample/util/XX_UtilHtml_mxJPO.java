import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.BPSJsonObjectBuilder;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilHtml_mxJPO {

    private static final String SYM_REL_MY_PART_STRUCTURE = "relationship_MyPartStructure";
    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";

    public XX_UtilHtml_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    public String getNameHtml(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String name = part.getInfo(context, DomainConstants.SELECT_NAME);
        String description = part.getInfo(context, DomainConstants.SELECT_DESCRIPTION);
        return "<span title=\"" + XSSUtil.encodeForHTMLAttribute(context, description) + "\">"
                + XSSUtil.encodeForHTML(context, name) + "</span>";
    }

    public String getAlertLink(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String name = part.getInfo(context, DomainConstants.SELECT_NAME);
        return "<a href=\"javascript:void(0)\" onclick=\"alert('" + XSSUtil.encodeForJavaScript(context, name) + "')\">"
                + XSSUtil.encodeForHTML(context, name) + "</a>";
    }

    public String getTreeLink(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        String name = DomainObject.newInstance(context, objectId).getInfo(context, DomainConstants.SELECT_NAME);
        String url = "../common/emxTree.jsp?objectId=" + XSSUtil.encodeForURL(context, objectId)
                + "&name=" + XSSUtil.encodeForURL(context, name);
        return "<a href=\"" + url + "\">" + XSSUtil.encodeForHTML(context, name) + "</a>";
    }

    public String decodeUrlValue(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return XSSUtil.decodeFromURL((String) programMap.get("value"));
    }

    public String getPartXml(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        DomainObject part = DomainObject.newInstance(context, objectId);
        String name = part.getInfo(context, DomainConstants.SELECT_NAME);
        String status = part.getAttributeValue(context, schema(context, SYM_ATTR_MY_STATUS));
        return "<part id=\"" + XSSUtil.encodeForXML(context, objectId) + "\"><name>" + XSSUtil.encodeForXML(context, name)
                + "</name><status>" + XSSUtil.encodeForXML(context, status) + "</status></part>";
    }

    public String getPartJson(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        DomainObject part = DomainObject.newInstance(context, objectId);
        JsonObjectBuilder json = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
        json.add("id", objectId);
        json.add("name", part.getInfo(context, DomainConstants.SELECT_NAME));
        json.add("status", part.getAttributeValue(context, schema(context, SYM_ATTR_MY_STATUS)));
        return json.build().toString();
    }

    public String getChildrenJson(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        MapList children = part.getRelatedObjects(context, schema(context, SYM_REL_MY_PART_STRUCTURE), "*",
                StringList.create(DomainConstants.SELECT_ID, DomainConstants.SELECT_NAME),
                StringList.create(DomainConstants.SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, 0);
        JsonArrayBuilder rows = Json.createArrayBuilder();
        for (Object o : children) {
            Map row = (Map) o;
            JsonObjectBuilder item = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
            item.add("id", (String) row.get(DomainConstants.SELECT_ID));
            item.add("name", (String) row.get(DomainConstants.SELECT_NAME));
            rows.add(item);
        }
        JsonObjectBuilder result = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
        result.add("children", rows);
        return result.build().toString();
    }

    public Map readJsonValues(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        JsonObject json = FrameworkUtil.toJsonObject((String) programMap.get("json"));
        Map result = new HashMap();
        result.put("name", json.getString("name"));
        result.put("status", json.getString("status"));
        return result;
    }
}
