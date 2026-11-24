import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilMail_mxJPO {

    private static final String BUNDLE = "emxMyAppStringResource";
    private static final String KEY_SUBJECT = "emxMyApp.Mail.ReviewSubject";
    private static final String KEY_BODY = "emxMyApp.Mail.ReviewBody";

    public XX_UtilMail_mxJPO(Context context, String[] args) throws Exception {
    }

    public void sendReviewRequest(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        StringList toList = FrameworkUtil.split((String) programMap.get("toUsers"), ",");
        String name = DomainObject.newInstance(context, objectId).getInfo(context, DomainConstants.SELECT_NAME);
        MailUtil.setAgentName(context, context.getUser());
        MailUtil.sendMessage(context, toList, null, null, "Review request: " + name,
                (String) programMap.get("message"), new StringList(objectId));
    }

    public void sendReviewRequestByKey(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        StringList toList = FrameworkUtil.split((String) programMap.get("toUsers"), ",");
        String name = DomainObject.newInstance(context, objectId).getInfo(context, DomainConstants.SELECT_NAME);
        String[] keys = { "name" };
        String[] values = { name };
        MailUtil.setAgentName(context, context.getUser());
        MailUtil.sendNotification(context, toList, null, null, KEY_SUBJECT, keys, values, KEY_BODY, keys, values,
                new StringList(objectId), null, BUNDLE);
    }

    public String getObjectLink(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String baseURL = MailUtil.getBaseURL(context);
        if (UIUtil.isNullOrEmpty(baseURL)) {
            return "";
        }
        return MailUtil.getObjectLinkURI(context, baseURL, (String) programMap.get("objectId"));
    }

    public Map getMailSettings(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        result.put("baseURL", MailUtil.getBaseURL(context));
        result.put("agentName", MailUtil.getAgentName(context));
        return result;
    }
}
