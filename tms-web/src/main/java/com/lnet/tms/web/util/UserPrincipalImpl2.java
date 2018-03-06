/*
package com.lnet.tms.web.util;

import com.lnet.framework.core.Response;
import com.lnet.framework.security.UserPrincipal;
import com.lnet.ums.contract.api.SiteService;
import com.lnet.ums.contract.api.UserService;
import com.lnet.model.ums.user.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class UserPrincipalImpl2 implements UserPrincipal {
    private static final String SITE_NAME = "TMS2_SITE_CODE";
    private static final String BRANCH_NAME = "TMS2_BRANCH_CODE";
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserService userService;
    @Autowired
    private SiteService siteService;

    private User getUser() {
        return (User) SecurityUtils.getSubject().getPrincipal();
    }

    @Override
    public String getUserId() {
        return getUser().getUserId();
    }

    @Override
    public String getDisplayName() {
        return getUser().getFullName();
    }

    @Override
    public String getEmail() {
        return getUser().getEmail();
    }

    @Override
    public String getMobile() {
        return null;
    }

    @Override
    public boolean isActive() {
        return getUser().isActive();
    }

    @Override
    public Map<String, String> getBindings() {
        Response<Map<String, String>> response = userService.getBindings(getUserId());
        return response.getBody();
    }

    @Override
    public String getName() {
        return getUser().getUsername();
    }

    */
/*public List<Organization> getAllBranchList() {
        String branchCode = getBindings().get(SystemUtil.ORGANIZATION);
        return userService.getAllBranches(branchCode).getBody();
    }

    public List<Site> getMatchSites() {
        List<Site> sites = userService.getAllSites(getUserId()).getBody();

        //如果有站点，则只允许看到绑定的站点
        if (sites != null && 0 < sites.size())
            return sites;

        //没有，则允许看到所有的站点
        sites = siteService.getByBranchCode(getCurrentBranchCode()).getBody();
        return sites;
    }

    public List<Site> getMatchSitesForLogin(String branchCode) {
        List<Site> sites = userService.getAllSites(getUserId()).getBody();

        //如果有站点，则只允许看到绑定的站点
        if (sites != null && 0 < sites.size())
            return sites;

        //没有，则允许看到所有的站点
        sites = siteService.getByBranchCode(branchCode).getBody();
        return sites;
    }*//*


    public String getCurrentBranchCode() {
        return CookieUtils.get(request, BRANCH_NAME).orElse("");
    }

    public String getCurrentSiteCode() {
        return CookieUtils.get(request, SITE_NAME).orElse("");
    }
}
*/
