package com.kite9.server.adl.holder.meta;

import com.kite9.pipeline.adl.holder.meta.MetaWrite;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class MetaHelper {

    public static UserMeta createUser(OAuth2User oauthUser) {
        return new UserMeta() {

            @Override
            public String getId() {
                return oauthUser.getName();
            }

            @Override
            public String getIcon() {
                return oauthUser.getAttribute("avatar_url");
            }

            @Override
            public String getPage() {
                return oauthUser.getAttribute("html_url");
            }

            @Override
            public String getDisplayName() {
                String name =  oauthUser.getAttribute("name");
                return name == null ? getLogin() : name;
            }

            @Override
            public String getLogin() {
                return oauthUser.getAttribute("login");
            }

        };
    }

    public static void setUser(MetaWrite mw) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            Object p = authentication.getPrincipal();
            if (p instanceof OAuth2User) {
                mw.setUser(createUser((OAuth2User) p));
            }
        }
    }

    public static void setAuthorAndNotification(Authentication authentication, MetaWrite mw) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            Object p = authentication.getPrincipal();
            if (p instanceof OAuth2User) {
                UserMeta user = createUser((OAuth2User) p);
                mw.setAuthor(user);
                mw.setNotification("Edit by "+user.getDisplayName());
            }
        }
    }
}
