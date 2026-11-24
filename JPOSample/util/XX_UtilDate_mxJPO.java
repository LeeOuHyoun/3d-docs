import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilDate_mxJPO {

    private static final String SYM_ATTR_MY_START_DATE = "attribute_MyStartDate";
    private static final String SYM_ATTR_MY_DUE_DATE = "attribute_MyDueDate";

    public XX_UtilDate_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String schema(Context context, String symbolicName) throws Exception {
        return PropertyUtil.getSchemaProperty(context, symbolicName);
    }

    private static Locale toLocale(Context context, Object localeObj) {
        if (localeObj instanceof Locale) {
            return (Locale) localeObj;
        }
        return context.getLocale();
    }

    public Map readModifiedDate(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String modified = part.getInfo(context, DomainConstants.SELECT_MODIFIED);
        Map result = new HashMap();
        result.put("modified", modified);
        result.put("modifiedDate", eMatrixDateFormat.getJavaDate(modified));
        return result;
    }

    public String setDueDateNow(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        SimpleDateFormat dbFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
        String dbValue = dbFormat.format(new Date());
        part.setAttributeValue(context, schema(context, SYM_ATTR_MY_DUE_DATE), dbValue);
        return dbValue;
    }

    public Map getDisplayModified(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        double tz = Double.parseDouble((String) programMap.get("timeZone"));
        Locale locale = toLocale(context, programMap.get("localeObj"));
        int format = eMatrixDateFormat.getEMatrixDisplayDateFormat();
        String modified = part.getInfo(context, DomainConstants.SELECT_MODIFIED);
        Map result = new HashMap();
        result.put("date", eMatrixDateFormat.getFormattedDisplayDateTime(context, modified, false, format, tz, locale));
        result.put("dateTime", eMatrixDateFormat.getFormattedDisplayDateTime(context, modified, true, format, tz, locale));
        return result;
    }

    public String saveDueDateInput(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        double tz = Double.parseDouble((String) programMap.get("timeZone"));
        Locale locale = toLocale(context, programMap.get("localeObj"));
        String dbValue = eMatrixDateFormat.getFormattedInputDate(context, (String) programMap.get("dueDate"), tz, locale);
        part.setAttributeValue(context, schema(context, SYM_ATTR_MY_DUE_DATE), dbValue);
        return dbValue;
    }

    public Map countWorkingDays(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String startValue = part.getAttributeValue(context, schema(context, SYM_ATTR_MY_START_DATE));
        String dueValue = part.getAttributeValue(context, schema(context, SYM_ATTR_MY_DUE_DATE));
        Map result = new HashMap();
        result.put("startDate", startValue);
        result.put("dueDate", dueValue);
        if (UIUtil.isNullOrEmpty(startValue) || UIUtil.isNullOrEmpty(dueValue)) {
            return result;
        }
        Date start = eMatrixDateFormat.getJavaDate(startValue);
        Date due = eMatrixDateFormat.getJavaDate(dueValue);
        result.put("workingDays", Long.valueOf(DateUtil.computeDuration(start, due)));
        return result;
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
