package com.valor.mercury.manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valor.manager.sdk.security.SecurityProvider;
import com.valor.manager.sdk.service.UpmsService;
import com.valor.manager.sdk.service.UpmsUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Gavin
 * 2020/9/7 10:24
 */
@EnableWebSecurity
@Configuration
@Order(1)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/assets/**", "/module/**").permitAll()
                .antMatchers("/executor/getTask", "/executor/taskReport", "/config/file/download",
                        "/config/file/syncAdd", "/config/file/syncDelete").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").permitAll()
                .successHandler(new AuthenticationSuccessHandler() {
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write((new ObjectMapper()).writeValueAsString(new HashMap<String, Object>() {
                            {
                                this.put("code", HttpStatus.OK.value());
                                this.put("msg", "Login Success");
                            }
                        }));
                    }
                }).failureHandler(new AuthenticationFailureHandler() {
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, final AuthenticationException authenticationException) throws IOException, ServletException {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write((new ObjectMapper()).writeValueAsString(new HashMap<String, Object>() {
                    {
                        this.put("code", HttpStatus.FORBIDDEN.value());
                        this.put("msg", "Login Failed:" + authenticationException.getMessage());
                    }
                }));
            }
        })
                .and().headers().frameOptions().disable()
                .and().logout().permitAll()
                .and().csrf().disable();
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.authenticationProvider());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new SecurityProvider();
    }

    @Bean
    public UpmsUserDetailsService userDetailsService() {
        return new UpmsUserDetailsService();
    }

    @Bean
    public UpmsService upmsService() {
        return new UpmsService();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.eraseCredentials(false);
    }

    @Bean
    public HttpFirewall allowUrlEncodedPeriodHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }
}
