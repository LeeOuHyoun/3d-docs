import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilHistory_mxJPO {

    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";
    private static final String[] HISTORY_KEYS = { "action", "time", "user", "state", "description" };

    public XX_UtilHistory_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String withoutLabel(String value) {
        String text = value == null ? "" : value.trim();
        String[] labels = { "time:", "user:", "state:" };
        for (String label : labels) {
            if (text.startsWith(label)) {
                return text.substring(label.length()).trim();
            }
        }
        return text;
    }

    private static String valueAt(StringList values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return withoutLabel((String) values.get(index));
    }

    public MapList getHistoryRows(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map history = UINavigatorUtil.getHistoryData(context, (String) programMap.get("objectId"));
        StringList actions = (StringList) history.get("action");
        MapList rows = new MapList();
        int count = actions == null ? 0 : actions.size();
        for (int i = 0; i < count; i++) {
            Map row = new HashMap();
            for (String key : HISTORY_KEYS) {
                row.put(key, valueAt((StringList) history.get(key), i));
            }
            rows.add(row);
        }
        return rows;
    }

    public Map getLatestHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map history = UINavigatorUtil.getHistoryData(context, (String) programMap.get("objectId"));
        Map latest = new HashMap();
        for (String key : HISTORY_KEYS) {
            StringList values = (StringList) history.get(key);
            latest.put(key, valueAt(values, values == null ? -1 : values.size() - 1));
        }
        return latest;
    }

    public String getPreviousOwner(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject object = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        StringList ownerChanges = object.getInfoList(context, "history.changeowner");
        String last = ownerChanges.isEmpty() ? "" : (String) ownerChanges.get(ownerChanges.size() - 1);
        int index = last.lastIndexOf("was:");
        return index < 0 ? "" : last.substring(index + "was:".length()).trim();
    }

    public Map getHistoryByIndex(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject object = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        int count = Integer.parseInt(object.getInfo(context, "history.count").trim());
        Map result = new HashMap();
        result.put("count", String.valueOf(count));
        if (count > 0) {
            String before = "history[" + (count - 1) + "]";
            String at = "history[" + count + "]";
            result.put(before, object.getInfo(context, before));
            result.put(at, object.getInfo(context, at));
        }
        return result;
    }

    public void addReviewHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        MqlUtil.mqlCommand(context, "modify bus $1 add history $2 comment $3",
                (String) programMap.get("objectId"), "Review", (String) programMap.get("comment"));
    }

    public void updateStatusWithoutHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject object = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String attrStatus = PropertyUtil.getSchemaProperty(context, SYM_ATTR_MY_STATUS);
        MqlUtil.mqlCommand(context, "history off", false, false);
        try {
            object.setAttributeValue(context, attrStatus, (String) programMap.get("status"));
        } finally {
            MqlUtil.mqlCommand(context, "history on", false, false);
        }
    }
}
