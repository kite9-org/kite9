package com.kite9.server.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import com.kite9.server.domain.RestEntity;
import org.apache.xml.utils.DefaultErrorHandler;
import org.codehaus.stax2.XMLStreamWriter2;
import org.kite9.diagram.common.StreamHelp;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kite9.diagram.logging.Logable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.DOMImplementation;
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
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;

/**
 * Handles conversion of the Hateoas {@link ResourceSupport} objects to ADL, and therefore HTML, SVG etc..
 * All of the Kite9 domain objects extend {@link ResourceSupport}, and lists of them 
 * implement {@link org.springframework.hateoas.Resources}
 * 
 * TODO: Refactor so this doesn't use ADLFactory.
 * @author robmoffat
 *
 */
@Component
public class HateoasADLHttpMessageWriter {
//	extends AbstractGenericHttpMessageConverter<RepresentationModel<?>> implements Logable {
//
//	private final Kite9Log log = new Kite9LogImpl(this);
//
//	public static final HttpHeaders EMPTY_HEADERS = new HttpHeaders();
//	public static final Charset DEFAULT = Charset.forName("UTF-8");
//
//	private final ObjectMapper objectMapper;
//	final private FormatSupplier formatSupplier;
//	private XmlFactory xmlFactory;
//	private WstxOutputFactory wstxOutputFactory;
//	private String resource = "clas";
//	private TransformerFactory transFact;
//	private ADLFactory adlFactory;
//	private Cache cache;
//
//
//	public HateoasADLHttpMessageWriter(
//			ObjectMapper objectMapper,
//			FormatSupplier formatSupplier,
//			ResourceLoader resourceLoader,
//			ADLFactory adlFactory,
//			Cache c,
//			@Value("${kite9.rest.transform:/public/templates/admin/transform.xslt}") String resource) {
//		super();
//		this.adlFactory = adlFactory;
//		this.objectMapper = objectMapper;
//		this.formatSupplier = formatSupplier;
//		this.xmlFactory  = new XmlFactory();
//		this.wstxOutputFactory = new WstxOutputFactory();
//		this.wstxOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
//		this.resource = resource;
//		this.transFact = TransformerFactory.newInstance();
//		this.transFact.setErrorListener(new DefaultErrorHandler(true));
//		this.cache = c;
//		setSupportedMediaTypes(formatSupplier.getMediaTypes());
//	}
//
//	@Override
//	protected boolean supports(Class<?> clazz) {
//		return RepresentationModel.class.isAssignableFrom(clazz);
//	}
//
//	@Override
//	protected boolean canRead(MediaType mediaType) {
//		return false;	// this is for display formats only.
//	}
//
//	@Override
//	public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
//		return false;
//	}
//
//	@Override
//	public RepresentationModel<?> read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
//		throw new UnsupportedOperationException("Can't read with this converter");
//	}
//
//	@Override
//	protected RepresentationModel<?> readInternal(Class<? extends RepresentationModel<?>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
//		throw new UnsupportedOperationException("Can't read with this converter");
//	}
//
//
//	@Override
//	public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
//		return super.canWrite(clazz, mediaType);
//	}
//
//
//	@Override
//	protected void writeInternal(RepresentationModel<?> t, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
//		try {
//			MediaType contentType = outputMessage.getHeaders().getContentType();
//			Format f = formatSupplier.getFormatFor(contentType);
//			if (f instanceof DiagramWriteFormat) {
//				writeADL(t, outputMessage, (DiagramWriteFormat) f);
//			} else {
//				throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Can't format directory as "+f.getExtension());
//			}
//		} catch (Exception e) {
//			throw new HttpMessageNotWritableException("Couldn't create REST Response", e);
//		}
//	}
//
//
//
//	protected void writeADL(RepresentationModel<?> t, HttpOutputMessage outputMessage, DiagramWriteFormat f) throws Exception {
//		URI request = URIRewriter.getCompleteCurrentRequestURI();
//		URI u = new URI(getSelfRef(t));
//		u = request.resolve(u);
//		ADLDom restDom = adlFactory.emptyAdlDom(u, EMPTY_HEADERS);
//		ADLExtensibleDOMImplementation dom =  restDom.getDocument().getImplementation();
//		generateRestXML(t, dom, restDom.getDocument());
//		if (isLoggingEnabled()) {
//			log.send("IN: " + new XMLHelper().toXML(restDom.getDocument()));
//		}
//
//		ADLDom adlDom = adlFactory.emptyAdlDom(u, EMPTY_HEADERS);
//		transformXML(restDom, adlDom);
//		if (isLoggingEnabled()) {
//			log.send("OUT: " + new XMLHelper().toXML(adlDom.getDocument()));
//		}
//
//		adlDom.setUser();
//		adlDom.setTitle(getTitle(t));
//
//		ADLOutput<?> created = adlDom.process(u, f);
//		StreamHelp.streamCopy(new ByteArrayInputStream(created.getAsBytes()), outputMessage.getBody(), false);
//	}
//
//	private String getSelfRef(RepresentationModel<?> t) {
//		return t.getLink(IanaLinkRelations.SELF).orElseThrow(
//			() -> new Kite9ProcessingException("Couldn't get url for "+t))
//				.getHref();
//	}
//
//	protected void generateRestXML(RepresentationModel<?> t, DOMImplementation dom, ADLDocument out) throws XMLStreamException, IOException, JsonGenerationException, JsonMappingException {
//		DOMResult domResult = new DOMResult(out);
//		ToXmlGenerator generator = createXMLGenerator(domResult);
//		objectMapper.writeValue(generator, t);
//		removeExcessNamespaces(out.getDocumentElement(), false);
//	}
//
//	public static final String TRANSFORMER = "transformer";
//
//	public void transformXML(ADLDom restDom, ADLDom adlDom) throws Exception {
//		// load the transform document
//		URI u = URIRewriter.resolve(resource);
//		String fullUrl = u.toString();
//		Transformer trans = (Transformer) cache.get(fullUrl, TRANSFORMER);
//		if (trans == null) {
//			String xslt = adlFactory.loadText(u, HttpHeaders.EMPTY);
//			Source xsltSource = new StreamSource(new StringReader(xslt), fullUrl);
//			xsltSource.setSystemId(u.toString());
//	        trans = transFact.newTransformer(xsltSource);
//	        cache.set(fullUrl, TRANSFORMER, trans);
//		}
//
//		Source inSource = new DOMSource(restDom.getDocument());
//        DOMResult result = new DOMResult(adlDom.getDocument());
//        trans.transform(inSource, result);
//    }
//
//	/**
//	 * Fixing a bug in woodstox that means nearly every element gets a namespace declaration.
//	 */
//	private void removeExcessNamespaces(Element e, boolean remove) {
//		boolean found = e.hasAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE)
//				&& e.getAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE).equals(XMLHelper.KITE9_NAMESPACE);
//
//		if (found && remove) {
//			e.removeAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
//		}
//
//		NodeList ch = e.getChildNodes();
//		for (int i = 0; i < ch.getLength(); i++) {
//			if (ch.item(i) instanceof Element) {
//				removeExcessNamespaces((Element) ch.item(i), found || remove);
//			}
//		}
//	}
//
//	/**
//	 * Way to convert JSON output to XML
//	 */
//	protected ToXmlGenerator createXMLGenerator(DOMResult domResult) throws XMLStreamException, IOException {
//		XMLStreamWriter streamWriter = wstxOutputFactory.createXMLStreamWriter(domResult);
//		streamWriter.setDefaultNamespace(XMLHelper.KITE9_NAMESPACE);
//		streamWriter.setPrefix("", XMLHelper.KITE9_NAMESPACE);
//
//		ToXmlGenerator generator = xmlFactory.createGenerator(streamWriter);
//
//		// disable pretty-printing with DOM Write
//		DefaultXmlPrettyPrinter pp = new DefaultXmlPrettyPrinter() {
//			@Override
//			public void writePrologLinefeed(XMLStreamWriter2 sw) throws XMLStreamException {
//			}
//		};
//		pp.spacesInObjectEntries(false);
//		pp.indentArraysWith(null);
//		pp.indentObjectsWith(null);
//		generator.setPrettyPrinter(pp);
//
//		// set top-level element name
//		generator.setNextName(new QName(XMLHelper.KITE9_NAMESPACE, "entity", ""));
//		return generator;
//	}
//
//	private String getTitle(RepresentationModel<?> rs) {
//		if (rs instanceof RestEntity) {
//			return ((RestEntity) rs).getType()+ ": " + ((RestEntity) rs).getTitle();
//		}
//
//		return "";
//	}
//
//	@Override
//	public String getPrefix() {
//		return "HATE";
//	}
//
//	@Override
//	public boolean isLoggingEnabled() {
//		return false;
//	}
//
}
