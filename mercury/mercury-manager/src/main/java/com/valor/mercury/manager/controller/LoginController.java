package com.valor.mercury.manager.controller;

import com.valor.manager.sdk.model.SysUser;
import com.valor.manager.sdk.security.UserAccess;
import com.valor.mercury.manager.model.system.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController implements ErrorController {

    private final AuthenticationProvider authenticationManager;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public LoginController(AuthenticationProvider authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult login(@RequestParam String username, @RequestParam String password) {
        logger.info("authenticate user:{}", username);
        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            return JsonResult.error("账号密码不能为空");
        }
        try {
            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return JsonResult.ok();
        } catch (UsernameNotFoundException ice) {
            return JsonResult.error("账号不存在");
        } catch (AuthenticationException uae) {
            return JsonResult.error("登录失败:" + uae.getMessage());
        }
    }

    /**
     * 主页
     */
    @RequestMapping({"/", "/index"})
    public String index(Model model) {
        SysUser user = UserAccess.getLoginUser();
        model.addAttribute("menus", UserAccess.getMenuTree());
        model.addAttribute("username", user.getUserName());
        return "index.html";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    /**
     * iframe页
     */
    @RequestMapping("/iframe")
    public String error(String url, Model model) {
        model.addAttribute("url", url);
        return "tpl/iframe.html";
    }

    /**
     * 错误页
     */
    @RequestMapping("/error")
    public String error(String code) {
        if ("403".equals(code)) {
            return "error/403.html";
        }
        return "error/404.html";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
