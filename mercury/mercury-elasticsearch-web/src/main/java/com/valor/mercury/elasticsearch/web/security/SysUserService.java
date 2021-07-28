package com.valor.mercury.elasticsearch.web.security;

import com.valor.mercury.elasticsearch.web.configs.ElasticsearchWebConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SysUserService implements UserDetailsService {
    private static Logger logger = LoggerFactory.getLogger(SysUserService.class);

    private Map<String, String> users = new ConcurrentHashMap<>();

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ElasticsearchWebConfiguration configuration;
    private long lastModifyTime = -1;

    @Autowired
    private void init() {
        loadUsers();
    }

    @Scheduled(fixedDelay = 60_000)
    private void loadUsers() {
        try {
            File file = new File(configuration.getUserConfigFiles());
            if (!file.exists())
                return;
            if (file.lastModified() <= lastModifyTime)
                return;
            List<String> lines = FileUtils.readLines(file);
            Map<String, String> map = new ConcurrentHashMap<>();
            for (String line : lines) {
                String[] user = line.split(",");
                map.put(user[0], passwordEncoder.encode(user[1]));
            }
            map.put("valor", passwordEncoder.encode("valor@9981"));
            users = map;
            logger.info("Load [{}] users from [{}].", users.size(), file.getAbsoluteFile());
            this.lastModifyTime = file.lastModified();

        } catch (Exception e) {
            logger.error("Load users error.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String password = users.get(username);
        if (password == null)
            throw new UsernameNotFoundException(username);
        return new User(username, password, AuthorityUtils.commaSeparatedStringToAuthorityList("users"));

    }
}
