package com.project.arbaz.aaspass.service;

import com.project.arbaz.aaspass.enums.Roles;
import com.project.arbaz.aaspass.entity.Users;
import com.project.arbaz.aaspass.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository appUserRepository;
    private final OidcUserService delegate = new OidcUserService();

    public CustomOidcUserService(UserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public @Nullable OidcUser loadUser(@NonNull OidcUserRequest request) throws OAuth2AuthenticationException {
        // Loading the user using the oidc default method
        OidcUser oidcUser = delegate.loadUser(request); /// making api calls and getting all the things
//        System.out.println("OidcUser: " + oidcUser);

        String provider = request.getClientRegistration().getRegistrationId(); // ( google )
        String providerSubject = oidcUser.getSubject();


        Users appUser = appUserRepository.findByProviderAndProviderSubject(provider , providerSubject)
                .orElseGet(() -> createNewUser(provider , providerSubject));

        appUser.setEmail(oidcUser.getEmail());
        appUser.setName(oidcUser.getFullName());
        appUserRepository.save(appUser);


        // It should return a simpleGranted authority
        Set<GrantedAuthority> role = Set.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole()));
        return new DefaultOidcUser(
                role ,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }

    private Users createNewUser(String provider, String providerSubject) {
        Users user = new Users();
        user.setProvider(provider);
        user.setProviderSubject(providerSubject);
        user.setRole(String.valueOf(Roles.USER));
        return user;
    }
}

