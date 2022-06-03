package com.kite9.server.adl.format.media;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;
import org.w3c.dom.Document;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.adl.holder.ADLOutputImpl;
import com.kite9.server.adl.holder.meta.Payload;

public class EditableADLFormat extends ADLFormat {

	public EditableADLFormat(ADLFactory factory, XMLHelper xmlHelper) {
		super(factory, xmlHelper);
	}
	
	@Override
	public ADLOutput handleWrite(ADLDom toWrite, Kite9Transcoder t) {
		DOMResult intermediate = new DOMResult();
		xmlHelper.duplicate(toWrite.getDocument(), isOmitDeclaration(), intermediate);
		Document toModify = (Document) intermediate.getNode();
		Payload.insertADLMetadata(toWrite, toModify);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		xmlHelper.duplicate(toModify, isOmitDeclaration(), new StreamResult(baos));
				
		return new ADLOutputImpl(this, toWrite, baos.toByteArray(), toWrite.getDocument());
	}

	private final List<K9MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getEADL());

	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}
	
	@Override
	public String getExtension() {
		return "eadl";
	}
	
	

}
