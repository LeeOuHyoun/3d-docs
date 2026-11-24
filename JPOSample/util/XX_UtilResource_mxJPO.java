import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilResource_mxJPO {

    private static final String FRAMEWORK_RESOURCE = "emxFrameworkStringResource";
    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";

    public XX_UtilResource_mxJPO(Context context, String[] args) throws Exception {
    }

    public String readSetting(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String key = (String) programMap.get("key");
        try {
            return EnoviaResourceBundle.getProperty(context, key);
        } catch (FrameworkException e) {
            return (String) programMap.get("defaultValue");
        }
    }

    public StringList readSettingList(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkProperties.getTokenizedProperty(context, (String) programMap.get("key"), ",");
    }

    public String readMessage(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return EnoviaResourceBundle.getProperty(context, FRAMEWORK_RESOURCE, context.getLocale(),
                (String) programMap.get("key"));
    }

    public String readMessageInLanguage(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Locale locale = MessageUtil.getLocale((String) programMap.get("language"));
        return EnoviaResourceBundle.getProperty(context, FRAMEWORK_RESOURCE, locale, (String) programMap.get("key"));
    }

    public String getDuplicateNameMessage(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Locale locale = context.getLocale();
        String typeLabel = EnoviaResourceBundle.getAdminI18NString(context, "Type",
                part.getInfo(context, DomainConstants.SELECT_TYPE), locale.getLanguage());
        String text = EnoviaResourceBundle.getProperty(context, FRAMEWORK_RESOURCE, locale,
                "emxFramework.DuplicateObjectName.Message");
        return MessageUtil.substituteMessageArgs(context, text, new Object[] {typeLabel}, locale);
    }

    public Map getDisplayNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        Map info = part.getInfo(context,
                StringList.create(DomainConstants.SELECT_TYPE, DomainConstants.SELECT_POLICY, DomainConstants.SELECT_CURRENT));
        String policy = (String) info.get(DomainConstants.SELECT_POLICY);
        String language = context.getLocale().getLanguage();
        Map result = new HashMap();
        result.put("type", EnoviaResourceBundle.getAdminI18NString(context, "Type",
                (String) info.get(DomainConstants.SELECT_TYPE), language));
        result.put("policy", EnoviaResourceBundle.getAdminI18NString(context, "Policy", policy, language));
        result.put("current", EnoviaResourceBundle.getStateI18NString(context, policy,
                (String) info.get(DomainConstants.SELECT_CURRENT), language));
        return result;
    }

    public String getStatusLabel(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        DomainObject part = DomainObject.newInstance(context, (String) programMap.get("objectId"));
        String attrStatus = PropertyUtil.getSchemaProperty(context, SYM_ATTR_MY_STATUS);
        return EnoviaResourceBundle.getRangeI18NString(context, attrStatus, part.getAttributeValue(context, attrStatus),
                context.getLocale().getLanguage());
    }

    public Map getStatusChoices(Context context, String[] args) throws Exception {
        String attrStatus = PropertyUtil.getSchemaProperty(context, SYM_ATTR_MY_STATUS);
        StringList values = FrameworkUtil.getRanges(context, attrStatus);
        Map result = new HashMap();
        result.put("values", values);
        result.put("labels", EnoviaResourceBundle.getAttrRangeI18NStringList(context, attrStatus, values,
                context.getLocale().getLanguage()));
        return result;
    }

    public Map getUserLanguage(Context context, String[] args) throws Exception {
        Locale locale = context.getLocale();
        Map result = new HashMap();
        result.put("locale", locale.toString());
        result.put("language", locale.getLanguage());
        return result;
    }
}
