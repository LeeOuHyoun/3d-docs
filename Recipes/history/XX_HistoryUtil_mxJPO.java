import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_HistoryUtil_mxJPO {

    private static final String[] HISTORY_KEYS = { "time", "user", "action", "state", "description" };

    public XX_HistoryUtil_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String last(StringList values) {
        return values == null || values.isEmpty() ? "" : (String) values.get(values.size() - 1);
    }

    private static DomainObject objectOf(Context context, Map programMap) throws Exception {
        return DomainObject.newInstance(context, (String) programMap.get("objectId"));
    }

    public Map getLatestHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map history = UINavigatorUtil.getHistoryData(context, (String) programMap.get("objectId"));
        Map latest = new HashMap();
        for (String key : HISTORY_KEYS) {
            latest.put(key, last((StringList) history.get(key)));
        }
        return latest;
    }

    public String getLatestEventHistory(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return last(objectOf(context, programMap).getInfoList(context, "history." + programMap.get("event")));
    }

    public String getPreviousOwner(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String entry = last(objectOf(context, programMap).getInfoList(context, "history.changeowner"));
        int index = entry.lastIndexOf("was:");
        return index < 0 ? "" : entry.substring(index + "was:".length()).trim();
    }

    public Map getHistoryByIndex(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject obj = objectOf(context, programMap);
        int count = Integer.parseInt(obj.getInfo(context, "history.count").trim());
        Map result = new HashMap();
        result.put("count", String.valueOf(count));
        if (count > 0) {
            String before = "history[" + (count - 1) + "]";
            String at = "history[" + count + "]";
            result.put(before, obj.getInfo(context, before));
            result.put(at, obj.getInfo(context, at));
        }
        return result;
    }
}
