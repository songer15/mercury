package com.valor.mercury.elasticsearch.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class LoginManageController {

    private static Logger logger = LoggerFactory.getLogger(LoginManageController.class);


    @RequestMapping(value = "/ping/v1", method = {RequestMethod.HEAD, RequestMethod.GET})
    @ResponseBody
    public String ping() {
        return "OK";
    }

    @RequestMapping({ReqRediectItem.MAIN_URL, "/"})
    public String mainPage(Model model) {
        putUserSession(model);
        return ReqRediectItem.Main_PAGE_URL;
    }

    @RequestMapping(ReqRediectItem.HOMEPAGE_URL)
    public String homePage(Model model) {

        putUserSession(model);
        return ReqRediectItem.HOME_PAGE_URL;
    }


    private void putUserSession(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            model.addAttribute("user", "anonymous");
        } else {
            User user = (User) principal;
            model.addAttribute("user", user);
        }
    }

}
