package com.kite9.server.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import org.codehaus.stax2.XMLStreamWriter2;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.MediaTypeHelper;
import com.kite9.server.adl.holder.meta.MetaHelper;
import com.kite9.server.domain.RestEntity;

/**
 * Handles conversion of the Hateoas {@link ResourceSupport} objects to ADL, and therefore HTML, SVG etc..
 * All of the Kite9 domain objects extend {@link ResourceSupport}, and lists of them 
 * implement {@link org.springframework.hateoas.Resources}
 * 
 * @author robmoffat
 *
 */
@Component
public class HateoasADLHttpMessageWriter extends AbstractADLDomMessageWriter<RepresentationModel<?>> {

	protected final static Logger LOG = LoggerFactory.getLogger(HateoasADLHttpMessageWriter.class);
	
	public static final HttpHeaders EMPTY_HEADERS = new HttpHeaders();
	public static final Charset DEFAULT = Charset.forName("UTF-8");

	private final ObjectMapper objectMapper;
	private XmlFactory xmlFactory;
	private WstxOutputFactory wstxOutputFactory;
	private ADLFactory adlFactory;
	private DocumentBuilder db;


	public HateoasADLHttpMessageWriter(
			ObjectMapper objectMapper,
			FormatSupplier formatSupplier,
			ADLFactory adlFactory,
			Cache c) throws Exception {
		super(formatSupplier, c);
		this.adlFactory = adlFactory;
		this.objectMapper = objectMapper;
		this.formatSupplier = formatSupplier;
		this.xmlFactory  = new XmlFactory();
		this.wstxOutputFactory = new WstxOutputFactory();
		this.wstxOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		this.db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return RepresentationModel.class.isAssignableFrom(clazz);
	}

	@Override
	protected boolean canRead(MediaType mediaType) {
		return false;	// this is for display formats only.
	}

	@Override
	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
		return false;
	}

	@Override
	public RepresentationModel<?> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException("Can't read with this converter");
	}

	@Override
	protected RepresentationModel<?> readInternal(Class<? extends RepresentationModel<?>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException("Can't read with this converter");
	}


	@Override
	public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
		return super.canWrite(clazz, mediaType);
	}


	@Override
	protected void writeInternal(RepresentationModel<?> t, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		try {
			MediaType contentType = outputMessage.getHeaders().getContentType();
			Format f = formatSupplier.getFormatFor(MediaTypeHelper.getKite9MediaType(contentType));
			if (f instanceof DiagramWriteFormat) {
				writeADL(t, outputMessage, (DiagramWriteFormat) f);
			} else {
				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Can't format directory as "+f.getExtension());
			}
		} catch (Exception e) {
			throw new HttpMessageNotWritableException("Couldn't create REST Response", e);
		}
	}

	protected void writeADL(RepresentationModel<?> t, HttpOutputMessage outputMessage, DiagramWriteFormat f) throws Exception {
		K9URI request = URIRewriter.getCompleteCurrentRequestURI();
		K9URI u = request.resolve(getSelfRef(t));
		
		Document out = generateRestXML(t);
		if (LOG.isDebugEnabled()) {
			LOG.debug("IN: " + new XMLHelper().toXML(out, true));
		}

		ADLDom adlDom = adlFactory.dom(u, out, EMPTY_HEADERS);

		MetaHelper.setUser(adlDom);
		adlDom.setTitle(getTitle(t));
		
		writeADLDom(adlDom, outputMessage);
	}

	private String getSelfRef(RepresentationModel<?> t) {
		return t.getLink(IanaLinkRelations.SELF).orElseThrow(
			() -> new Kite9ProcessingException("Couldn't get url for "+t))
				.getHref();
	}

	protected Document generateRestXML(RepresentationModel<?> t) throws XMLStreamException, IOException, JsonGenerationException, JsonMappingException {
		Document out = db.newDocument();
		DOMResult domResult = new DOMResult(out);
		ToXmlGenerator generator = createXMLGenerator(domResult);
		objectMapper.writeValue(generator, t);
		removeExcessNamespaces(out.getDocumentElement(), false);
		handleTemplateNaming(out);
		return out;
	}

	private void handleTemplateNaming(Document out) {
		out.getDocumentElement().setAttributeNS(Kite9Namespaces.XSL_TEMPLATE_NAMESPACE, "xslt:template", "/github/kite9-org/kite9/templates/admin/admin-template.xsl?v=v0.13");
	}

	/**
	 * Fixing a bug in woodstox that means nearly every element gets a namespace declaration.
	 */
	private void removeExcessNamespaces(Element e, boolean remove) {
		boolean found = e.hasAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE)
				&& e.getAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE).equals(Kite9Namespaces.ADL_NAMESPACE);

		if (found && remove) {
			e.removeAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
		}

		NodeList ch = e.getChildNodes();
		for (int i = 0; i < ch.getLength(); i++) {
			if (ch.item(i) instanceof Element) {
				removeExcessNamespaces((Element) ch.item(i), found || remove);
			}
		}
	}

	/**
	 * Way to convert JSON output to XML
	 */
	protected ToXmlGenerator createXMLGenerator(DOMResult domResult) throws XMLStreamException, IOException {
		XMLStreamWriter streamWriter = wstxOutputFactory.createXMLStreamWriter(domResult);
		streamWriter.setDefaultNamespace(Kite9Namespaces.ADL_NAMESPACE);
		streamWriter.setPrefix("", Kite9Namespaces.ADL_NAMESPACE);

		ToXmlGenerator generator = xmlFactory.createGenerator(streamWriter);

		// disable pretty-printing with DOM Write
		@SuppressWarnings("serial")
		DefaultXmlPrettyPrinter pp = new DefaultXmlPrettyPrinter() {
			@Override
			public void writePrologLinefeed(XMLStreamWriter2 sw) throws XMLStreamException {
			}
		};
		pp.spacesInObjectEntries(false);
		pp.indentArraysWith(null);
		pp.indentObjectsWith(null);
		generator.setPrettyPrinter(pp);

		// set top-level element name
		generator.setNextName(new QName(Kite9Namespaces.ADL_NAMESPACE, "diagram", ""));
		return generator;
	}

	private String getTitle(RepresentationModel<?> rs) {
		if (rs instanceof RestEntity) {
			return ((RestEntity) rs).getType()+ ": " + ((RestEntity) rs).getTitle();
		}

		return "";
	}
}
