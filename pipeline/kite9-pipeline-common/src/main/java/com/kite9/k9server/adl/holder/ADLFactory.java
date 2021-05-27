package com.kite9.k9server.adl.holder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.kite9.k9server.adl.holder.pipeline.ADLBase;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.springframework.http.HttpHeaders;

import com.kite9.k9server.web.HateoasADLHttpMessageWriter;

public interface ADLFactory {
	
	public ADLBase uri(URI uri, Map<String, List<String>> requestHeaders);
		
	public ADLBase adl(URI uri, String xml, Map<String, List<String>> requestHeaders);

	/**
	 * This is currently just used for HateoasADLHttpMessageWriter, and really shouldn't be,
	 * since we aren't giving it the content.
	 */
	public ADLDom emptyAdlDom(URI uri, Map<String, List<String>> requestHeaders);

	/**
	 * Used for HateoasADLHttpMessageWriter, probably shouldn't be.
	 */
	public String loadText(URI uri2, Map<String, List<String>> requestHeaders);
}
