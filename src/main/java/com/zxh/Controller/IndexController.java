package com.zxh.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by admin on 2017/12/14.
 */

@Controller

public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

}
