package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.sources.ModifiableDiagramAPI;

/**
 * This class allows diagrams to be edited client-side, even though they are static on disk.
 * 
 * @author rob@kite9.com
 *
 */
public class TransientDiagramAPI implements ModifiableDiagramAPI {

	private final K9MediaType underlying;
	private ADLBase dom;

	public TransientDiagramAPI(K9MediaType underlying, ADLBase base) {
		this.underlying = underlying;
		this.dom = base;
	}

	public K9MediaType getMediaType() {
		return underlying;
	}

	@Override
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders headers) {
		return dom;
	}

	@Override
	public ModificationType getModificationType(Authentication a) {
		return ModificationType.MODIFIABLE;
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
		adl.setTitle(createTitle(adl.getUri()));
	}

	public static String createTitle(K9URI u) {
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
	public void commitRevisionAsBytesInner(String message, Authentication by, byte[] bytes) {
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		return new ByteArrayInputStream(dom.getAsString().getBytes());
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		throw new UnsupportedOperationException("Only works for directories");
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		return SourceType.FILE;
	}

	@Override
	public K9URI getUnderlyingResourceURI(Authentication a) {
		return dom.getUri();
	}

	@Override
	public DiagramWriteFormat getWriteFormat() {
		return null;	// transient diagrams are not written
	}

}
