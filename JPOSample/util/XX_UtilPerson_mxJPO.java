import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class XX_UtilPerson_mxJPO {

    private static final String SYM_ROLE_MY_TEAM_LEADER = "role_MyTeamLeader";
    private static final String SYM_ROLE_MY_REVIEWER = "role_MyReviewer";

    public XX_UtilPerson_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String userName(Context context, Map programMap) {
        String user = (String) programMap.get("userName");
        if (UIUtil.isNullOrEmpty(user)) {
            return context.getUser();
        }
        return user;
    }

    public Map getLoginUserInfo(Context context, String[] args) throws Exception {
        String user = context.getUser();
        Map result = new HashMap();
        result.put("userName", user);
        result.put("personId", PersonUtil.getPersonObjectID(context, user));
        result.put("fullName", PersonUtil.getFullName(context, user));
        result.put("email", PersonUtil.getEmail(context, user));
        return result;
    }

    public Map getPersonDetails(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Person person = Person.getPerson(context, userName(context, programMap));
        String firstName = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_FIRST_NAME);
        String companyName = "to[" + DomainConstants.RELATIONSHIP_EMPLOYEE + "].from.name";
        Map info = person.getInfo(context, StringList.create(DomainConstants.SELECT_NAME, firstName, companyName));
        Map result = new HashMap();
        result.put("name", info.get(DomainConstants.SELECT_NAME));
        result.put("firstName", info.get(firstName));
        result.put("company", info.get(companyName));
        return result;
    }

    public String getCompanyId(Context context, String[] args) throws Exception {
        return PersonUtil.getUserCompanyId(context);
    }

    public StringList getMemberOrganizationIds(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String[] ids = PersonUtil.getMemberOrganizations(context, userName(context, programMap));
        if (ids == null) {
            return new StringList();
        }
        return new StringList(ids);
    }

    public Map getCurrentSecurityContext(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        result.put("securityContext", PersonUtil.getActiveSecurityContext(context));
        result.put("organization", PersonUtil.getActiveOrganization(context));
        result.put("project", PersonUtil.getActiveProject(context));
        return result;
    }

    public Map getDefaultSecurityContext(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String user = userName(context, programMap);
        Map result = new HashMap();
        String securityContext = PersonUtil.getDefaultSecurityContext(context, user);
        result.put("securityContext", securityContext);
        if (UIUtil.isNotNullAndNotEmpty(securityContext)) {
            result.put("organization", PersonUtil.getDefaultOrganization(context, user));
            result.put("project", PersonUtil.getDefaultProject(context, user));
        }
        return result;
    }

    public StringList getSecurityContextNames(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return PersonUtil.getSecurityContextsNames(context, userName(context, programMap));
    }

    public StringList getProjectsOfOrganization(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return PersonUtil.getProjects(context, userName(context, programMap), (String) programMap.get("organization"));
    }

    public Boolean hasTeamLeaderRole(Context context, String[] args) throws Exception {
        String role = PropertyUtil.getSchemaProperty(context, SYM_ROLE_MY_TEAM_LEADER);
        return Boolean.valueOf(PersonUtil.hasAssignment(context, role));
    }

    public Boolean hasAnyReviewRole(Context context, String[] args) throws Exception {
        return Boolean.valueOf(PersonUtil.hasAnyAssignment(context, SYM_ROLE_MY_TEAM_LEADER + "," + SYM_ROLE_MY_REVIEWER));
    }

    public Boolean isTeamLeader(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        Person person = Person.getPerson(context, userName(context, programMap));
        return Boolean.valueOf(person.hasRole(context, PropertyUtil.getSchemaProperty(context, SYM_ROLE_MY_TEAM_LEADER)));
    }

    public StringList getUserAssignments(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return new StringList(PersonUtil.getAssignments(context, userName(context, programMap)));
    }

    public Boolean isAdminContext(Context context, String[] args) throws Exception {
        return Boolean.valueOf(PersonUtil.isVPLMAdmin(context, PersonUtil.getActiveSecurityContext(context)));
    }

    public StringList getTeamLeaders(Context context, String[] args) throws Exception {
        return PersonUtil.getPersonFromRole(context, PropertyUtil.getSchemaProperty(context, SYM_ROLE_MY_TEAM_LEADER));
    }

    public MapList getGroupMembers(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        return FrameworkUtil.getProjectGroupAssignees(context, (String) programMap.get("groupName"));
    }

    public Map getVaults(Context context, String[] args) throws Exception {
        Map result = new HashMap();
        result.put("loginVault", context.getVault().getName());
        result.put("searchVaults", VaultUtil.getSearchVaultPattern(context));
        return result;
    }
}
