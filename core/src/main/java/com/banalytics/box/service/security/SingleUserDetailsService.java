package com.banalytics.box.service.security;

import com.banalytics.box.module.LocalUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import static com.banalytics.box.config.LoginSecurityConfig.PASSWORD_ENCODER;

@Slf4j
@Component
public class SingleUserDetailsService implements UserDetailsService, InitializingBean, AuthenticationManager, LocalUserService {
    public static final String DEFAULT_USER_NAME = "default";
    private static final String DEFAULT_PASSWORD = "default";
    private static final String DEFAULT_ROLE = "USER";
    private static final String PASSWORD_FILE_NAME = "user.password";

    @Value("${config.instance.root}/config")
    private File configFolder;

    private File passwordFile;

    private String currentEncodedPassword;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.currentEncodedPassword = PASSWORD_ENCODER.encode(DEFAULT_PASSWORD);
        this.passwordFile = new File(configFolder, PASSWORD_FILE_NAME);
        if (this.passwordFile.exists()) {
            try (FileReader fr = new FileReader(this.passwordFile)) {
                this.currentEncodedPassword = IOUtils.toString(fr);
            }
        } else {
            try {
                if (!this.passwordFile.getParentFile().mkdirs() && !this.passwordFile.createNewFile()) {
                    throw new Exception("Can't create password storage");
                }
            } catch (Exception e) {
                throw new Exception(passwordFile.getAbsolutePath(), e);
            }
            flushCurrentPasswordToFile();
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (PASSWORD_ENCODER.matches((String) authentication.getCredentials(), this.currentEncodedPassword)) {
            return new UsernamePasswordAuthenticationToken(DEFAULT_USER_NAME, this.currentEncodedPassword, List.of(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return DEFAULT_ROLE;
                }
            }));
        } else {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (PASSWORD_ENCODER.matches(oldPassword, this.currentEncodedPassword)) {
            this.currentEncodedPassword = PASSWORD_ENCODER.encode(newPassword);
            flushCurrentPasswordToFile();
        } else {
            throw new RuntimeException("Invalid password.");
        }
    }

    public void resetPassword(String newPassword) {
        this.currentEncodedPassword = PASSWORD_ENCODER.encode(newPassword);
        flushCurrentPasswordToFile();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return User
                .withUsername(DEFAULT_USER_NAME)
                .password(this.currentEncodedPassword)
                .roles(DEFAULT_ROLE)
                .build();
    }

    private void flushCurrentPasswordToFile() {
        try (FileWriter fr = new FileWriter(this.passwordFile)) {
            IOUtils.write(this.currentEncodedPassword, fr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyPassword(String password) {
        return PASSWORD_ENCODER.matches(password, this.currentEncodedPassword);
    }
}
