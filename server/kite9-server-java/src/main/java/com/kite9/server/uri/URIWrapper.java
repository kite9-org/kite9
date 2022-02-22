package com.kite9.server.uri;

import java.net.URISyntaxException;

import org.jetbrains.annotations.NotNull;
import org.kite9.diagram.logging.Kite9ProcessingException;

import com.kite9.pipeline.uri.K9URI;

public class URIWrapper {

    public static K9URI wrap(java.net.URI javaNetURI) {
        if (javaNetURI==null) {
            return null;
        }

        return new K9URI() {

            @NotNull
            @Override
            public K9URI withoutQueryParameters() {
                try {
                    return wrap(new java.net.URI(javaNetURI.getScheme(), javaNetURI.getAuthority(), javaNetURI.getPath(), null, null));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Couldn't create uri: ", e);
                }
            }

            @NotNull
            @Override
            public K9URI changeScheme(@NotNull String scheme, @NotNull String path) {
                try {
                    return wrap(new java.net.URI(scheme, javaNetURI.getUserInfo(), javaNetURI.getHost(), javaNetURI.getPort(),
                            path, javaNetURI.getQuery(), javaNetURI.getFragment()));
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Couldn't create uri: ", e);
                }
            }

            @NotNull
            @Override
            public String getScheme() { return javaNetURI.getScheme(); }

            @NotNull
            @Override
            public String getPath() {
                return javaNetURI.getPath();
            }

            @NotNull
            @Override
            public K9URI resolve(@NotNull String location) {
                return wrap(javaNetURI.resolve(location));
            }

            @Override
            public String toString() {
                return javaNetURI.toString();
            }

            @Override
            public int hashCode() {
                return javaNetURI.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof K9URI) {
                    return this.toString().equals(obj.toString());
                } else {
                    return false;
                }
            }

            @NotNull
            @Override
            public String getHost() {
                return javaNetURI.getHost();
            }
        };
    }

    public static java.net.URI from(K9URI u) {
        try {
            return new java.net.URI(u.toString());
        } catch (URISyntaxException e) {
            throw new Kite9ProcessingException("Couldn't create URI from "+u);
        }
    }
}
