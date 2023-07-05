package com.kite9.server.adl.format;

import com.kite9.pipeline.adl.format.media.K9MediaType;

public class MediaTypeHelper {

    public static K9MediaType getKite9MediaType(org.springframework.http.MediaType springMediaType) {
        return K9MediaType.Companion.parseMediaType(springMediaType.toString());
    }

    public static boolean includes(K9MediaType m, org.springframework.http.MediaType mediaType) {
        return org.springframework.http.MediaType.parseMediaType(m.toString()).includes(mediaType);
    }
}
