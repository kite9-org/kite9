package com.kite9.server.adl.format.media;

import java.util.Collections;
import java.util.List;

import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;

public class SEFFormat implements Format {

	@Override
	public String getExtension() {
		return "sef";
	}

	 @Override
    public String getFormatIdentifier() {
        return getMediaTypes().get(0).toString();
    }

	@Override
	public List<K9MediaType> getMediaTypes() {
		return Collections.singletonList(Kite9MediaTypes.INSTANCE.getSEF());
	}

}
