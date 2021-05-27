package com.kite9.k9server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.kite9.k9server.adl.holder.meta.MetaReadWrite;
import com.kite9.k9server.adl.holder.meta.Role;
import com.kite9.k9server.adl.holder.pipeline.ADLBase;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.sources.ModifiableDiagramAPI;

public class TransientDiagramAPI implements ModifiableDiagramAPI {

	private final MediaType underlying;
	private ADLBase dom;

	public TransientDiagramAPI(MediaType underlying, ADLBase base) {
		this.underlying = underlying;
		this.dom = base;
	}

	public MediaType getMediaType() {
		return underlying;
	}

	@Override
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders headers) {
		return dom;
	}

	@Override
	public Type getType(Authentication a) {
		return Type.MODIFIABLE;
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
		adl.setTitle(createTitle(adl.getUri()));
	}

	public static String createTitle(URI u) {
		String fileNamePart = u.getPath().contains("/") ? 
			u.getPath().substring(u.getPath().lastIndexOf("/")+1) :
			u.getPath();
	
		String withoutExtension = fileNamePart.contains(".") ? 
			fileNamePart.substring(0, fileNamePart.indexOf(".")) :
			fileNamePart;
				
		
		return withoutExtension;
	}
	
	@Override
	public Role getAuthenticatedRole(Authentication a) {
		return Role.EDITOR;
	}

	@Override
	public String getUserId(Authentication a) {
		return null;
	}

	@Override
	public URI getSourceLocation() {
		return dom.getUri();
	}

	@Override
	public void commitRevision(String message, Authentication by, ADLDom data) {
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		return new ByteArrayInputStream(dom.getXMLString().getBytes());
	}

}
