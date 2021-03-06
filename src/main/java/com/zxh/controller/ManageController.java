package com.zxh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by admin on 2017/12/14.
 */

@Controller
public class ManageController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }


    @RequestMapping("{page}")
    public String showPage(@PathVariable String page) {
        return page;
    }

    /*
    @RequestMapping("/admin/{page}")
    public String showAdminPage(@PathVariable String page) {
        return "/admin/" + page;
    }
    */

}
