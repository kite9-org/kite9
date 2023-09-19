package com.kite9.server.persistence.queue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.persistence.cache.AbstractCachingModifiableAPI;
import com.kite9.server.sources.DiagramAPI;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;

/**
 * Writes operations to the ChangeQueue, as well as making them on the 
 * delegate.
 * 
 * @author robmoffat
 */
public class CommandQueueModifiableDiagramAPI extends AbstractCachingModifiableAPI implements ModifiableDiagramAPI {

	private final ChangeQueue cq;
	private final ModifiableDiagramAPI backingStore;
	private String localCache = null;
	private final ADLFactory factory;
	
	public CommandQueueModifiableDiagramAPI(ChangeQueue cq, ModifiableDiagramAPI backingStore, ADLFactory factory, long occupancyTimeMs) {
		super(occupancyTimeMs);
		this.cq = cq;
		this.backingStore = backingStore;
		this.factory = factory;
	}
	
	@Override
	public void commitRevision(String message, Authentication by, ADLDom dom) throws Exception {
		checkUserCanWrite(by);
		localCache = dom.getAsString();
		ADLOutput out = dom.process(getUnderlyingResourceURI(by), getWriteFormat());
		commitRevisionAsBytes(message, by, out.getAsBytes());
	}
	
	@Override
	public void commitRevisionAsBytesInner(String message, Authentication by, byte[] bytes) {
		cq.addItem(new ChangeQueue.Change(backingStore, message, bytes, by));
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
		backingStore.addMeta(adl);
		int qs = cq.getQueueSize();
		if (qs > 0) {
			adl.setCommitCount(qs); 
		}
	}

	@Override
	public void update() {
		localCache = null;
		goodEmails.clear();
	}

	@Override
	public ADLBase getCurrentRevisionContent(Authentication a, HttpHeaders headers) throws Exception {
		if (getAuthenticatedRole(a) != com.kite9.pipeline.adl.holder.meta.Role.NONE) {
			if (localCache == null) {
				ADLBase bs = backingStore.getCurrentRevisionContent(a, headers);
				localCache = bs.getAsString();
				return bs;
			}
			
			return factory.adl(getUnderlyingResourceURI(a), localCache, headers);
		} else {
			// try to access without authentication
			return backingStore.getCurrentRevisionContent(a, headers);
		}
	}
	
	@Override
	protected com.kite9.pipeline.adl.holder.meta.Role getAuthenticatedRoleInner(Authentication a) {
		return backingStore.getAuthenticatedRole(a);
	}

	@Override
	public String getUserId(Authentication a) {
		return backingStore.getUserId(a);
	}

	@Override
	public int getCommitCount() {
		return cq.getQueueSize();
	}

	@Override
	public K9MediaType getMediaType() {
		return backingStore.getMediaType();
	}

	@Override
	public ModificationType getModificationType(Authentication a) throws Exception {
		return backingStore.getModificationType(a);
	}

	@Override
	public K9URI getUnderlyingResourceURI(Authentication a) throws Exception {
		return backingStore.getUnderlyingResourceURI(a);
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		// potentially, the user could be getting back some stale content here, if the
		// edits are still being persisted.  
		return backingStore.getCurrentRevisionContentStream(authentication);
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		return backingStore.getEntityRepresentation(a);
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		return backingStore.getSourceType(a);
	}

	@Override
	public DiagramWriteFormat getWriteFormat() {
		return backingStore.getWriteFormat();
	}

	

}
