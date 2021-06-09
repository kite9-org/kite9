
package com.kite9.server.adl.cache;

import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.web.URIRewriter;
import org.kite9.diagram.dom.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches all files loaded from the public area of the current site.
 *
 * @author robmoffat
 *
 */
public class PublicCache implements Cache {

    protected final Logger LOG = LoggerFactory.getLogger(PublicCache.class);


    private String urlPrefix;
    private Map<String, Map<String, SoftReference<Object>>> multiCache = new HashMap<>();

    public PublicCache() {
        super();
    }

    @Override
    public synchronized Object get(String key, String type) {
        Map<String, SoftReference<Object>> theCache = multiCache.computeIfAbsent(type, k -> new HashMap<>());
        if (isValid(key)) {
            SoftReference<Object> ref = theCache.get(key);
            if (ref != null) {
                Object out = ref.get();
                if (out != null) {
                    LOG.debug("PublicCache hit on {} type {}",key, type);
                    return out;
                }
            }
        }

        theCache.remove(key);
        return null;
    }

    @Override
    public synchronized void set(String key, String type, Object value) {
        Map<String, SoftReference<Object>> theCache = multiCache.computeIfAbsent(type, k -> new HashMap<>());
        if (isValid(key)) {
            SoftReference<Object> ref = theCache.get(key);
            Object existing = ref == null ? null : ref.get();
            if (existing == null) {
                LOG.debug("PublicCache storing {} of type {}",key, type);
                theCache.put(key, new SoftReference<Object>(value));
            }
        }
    }

    @Override
    public boolean isValid(String key) {
        if (urlPrefix == null) {
            K9URI host = URIRewriter.getCompleteCurrentRequestURI();
            host = host.resolve("/public");
            urlPrefix = host.toString();
            LOG.warn("Caching all content under {}", urlPrefix);
        }
        return (key != null) && key.startsWith(urlPrefix);
    }

}