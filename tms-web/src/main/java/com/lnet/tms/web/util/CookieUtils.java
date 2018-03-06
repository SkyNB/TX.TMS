package com.lnet.tms.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

public class CookieUtils {

    public static Optional<String> get(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if ((null != cookies) && (0 < cookies.length)) {
            for (Cookie cookie : cookies) {
                if (name.equalsIgnoreCase(cookie.getName())) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<String> get(HttpServletRequest request, String name, String path) {
        Cookie[] cookies = request.getCookies();

        if((null != cookies) && (0 < cookies.length)) {
            for(Cookie cookie : cookies) {
                if(name.equalsIgnoreCase(cookie.getName()) && path.equalsIgnoreCase(cookie.getPath())) {
                    return Optional.ofNullable(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public static void set(HttpServletResponse response, String name, String value) {
        response.addCookie(new Cookie(name, value));
    }

    public static void set(HttpServletResponse response, String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        response.addCookie(cookie);
    }
}
