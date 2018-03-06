package com.lnet.tms.web.util;

import com.lnet.framework.security.UserPrincipal;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/5.
 */
@Component
public class UserPrincipalImpl implements UserPrincipal {
    @Override
    public String getUserId() {
        return "809320136273506304";
    }

    @Override
    public String getDisplayName() {
        return "深圳管理员";
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getMobile() {
        return null;
    }

    @Override
    public String getCurrentBranchCode() {
        return "SZ";
    }

    @Override
    public String getCurrentSiteCode() {
        return "SZ_IT";
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Map<String, String> getBindings() {
        Map<String,String> map = new HashMap<>();
        map.put("SYSTEM","");
        return null;
    }

    @Override
    public String getName() {
        return "SZ001";
    }
}
