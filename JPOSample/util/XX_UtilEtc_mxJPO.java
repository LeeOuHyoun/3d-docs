import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.CacheUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilEtc_mxJPO {

    private static final Logger LOG = LoggerFactory.getLogger(XX_UtilEtc_mxJPO.class);
    private static final String SYM_TYPE_MY_PART = "type_MyPart";
    private static final String SYM_POLICY_MY_PART = "policy_MyPartPolicy";
    private static final String SYM_ATTR_MY_STATUS = "attribute_MyStatus";
    private static final String CACHE_KEY_POLICIES = "XX_MyApp.PolicyList";

    public XX_UtilEtc_mxJPO(Context context, String[] args) throws Exception {
    }

    public String createNumberedPart(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String attrStatus = PropertyUtil.getSchemaProperty(context, SYM_ATTR_MY_STATUS);
        ContextUtil.startTransaction(context, true);
        try {
            String newId = FrameworkUtil.autoName(context, SYM_TYPE_MY_PART, SYM_POLICY_MY_PART);
            DomainObject.newInstance(context, newId).setAttributeValue(context, attrStatus, (String) programMap.get("status"));
            ContextUtil.commitTransaction(context);
            return newId;
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw e;
        }
    }

    public StringList getCachedPolicies(Context context, String[] args) throws Exception {
        StringList policies = (StringList) CacheUtil.getCacheObject(context, CACHE_KEY_POLICIES);
        if (policies == null) {
            policies = FrameworkUtil.split(MqlUtil.mqlCommand(context, "list policy $1", "My*"), "\n");
            CacheUtil.setCacheObject(context, CACHE_KEY_POLICIES, policies);
        }
        return policies;
    }

    public void clearCachedPolicies(Context context, String[] args) throws Exception {
        CacheUtil.removeCacheObject(context, CACHE_KEY_POLICIES);
    }

    public Map getServerInfo(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        result.put("cloud", String.valueOf(UINavigatorUtil.isCloud(context)));
        result.put("programCentral",
                String.valueOf(FrameworkUtil.isSuiteRegistered(context, "appVersionProgramCentral", false, null, null)));
        return result;
    }

    public void startReport(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map params = new HashMap();
        params.put("objectId", (String) programMap.get("objectId"));
        new BackgroundProcess().submitJob(context, "XX_UtilEtc", "writeReport", JPO.packArgs(params), (String) null);
    }

    public int writeReport(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        try {
            String name = DomainObject.newInstance(context, objectId).getInfo(context, DomainConstants.SELECT_NAME);
            LOG.info("report for {}", name);
            return 0;
        } catch (Exception e) {
            LOG.error("report failed: " + objectId, e);
            throw e;
        }
    }
}
