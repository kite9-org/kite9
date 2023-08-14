package com.kite9.server.adl.holder.meta;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.kite9.pipeline.adl.holder.meta.MetaWrite;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * This extracts the config details from the repository REST xml and adds them to the
     * metadata for the diagram being produced.
     */
    public static void setConfigMetadata(ADLDom adlDom) {
        NodeList configs = adlDom.getDocument().getElementsByTagNameNS(Kite9Namespaces.ADL_NAMESPACE, "config");
        List<String> templates = new ArrayList<>();
        for (int i = 0; i< configs.getLength(); i++) {
            NodeList items = configs.item(i).getChildNodes();
            for (int j =0; j< items.getLength(); j++) {
                Node c = items.item(j);
                if (c instanceof Element) {
                    String tagName = ((Element) c).getTagName();
                    String content = c.getTextContent();
                    if (tagName.equals("uploads")) {
                        adlDom.setUploadsPath(content);
                    } else if (tagName.equals("templatePath")) {
                        adlDom.setTemplatePath(content);
                    } else if (tagName.equals("templates")) {
                        templates.add(content);
                    }
                }
            }
        }
        adlDom.setTemplates(templates);
    }
}
