package com.project.arbaz.aaspass.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;
@EqualsAndHashCode(callSuper = true)
@Data
public class AppOidcUser extends DefaultOidcUser {
    private final Long userId;
    private final String email;
    private final String name;
    private final String role;
    private final String provider;
    private final String providerSubject;

    public AppOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            OidcUserInfo userInfo,
            Long userId,
            String email,
            String name,
            String role,
            String provider,
            String providerSubject
    ) {
        super(authorities, idToken, userInfo);
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.providerSubject = providerSubject;
    }


    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getFullName() {
        return name;
    }
}
