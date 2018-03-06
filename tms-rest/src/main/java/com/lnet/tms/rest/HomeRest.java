package com.lnet.tms.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by LH on 2016/12/28.
 */
@RestController
@RequestMapping("/")
public class HomeRest {
    @RequestMapping(method = RequestMethod.GET)
    public String index() {
        return "tms rest deploy success...";
    }
}
