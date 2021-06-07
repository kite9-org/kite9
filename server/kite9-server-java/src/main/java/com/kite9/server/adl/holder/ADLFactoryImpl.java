package com.kite9.server.adl.holder;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.*;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.uri.URIWrapper;
import com.kite9.server.web.URIRewriter;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.batik.format.Kite9TranscoderImpl;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADLFactoryImpl implements ADLFactory {
	
	private Cache cache;

	public ADLFactoryImpl(Cache cache) {
		super();
		this.cache = cache;
	}

	abstract class AbstractADLBase extends AbstractXMLBase implements ADLBase {


		public AbstractADLBase(K9URI uri,
							   Map<String, ? extends List<String>> requestHeaders,
							   Map<String, Object> meta) {
			super(uri, requestHeaders, meta);
		}

		@Override
		public ADLDom parse() {
			return new ADLDomImpl(getUri(), getRequestHeaders(), parseADLDocument(getAsString(), getUri()), new HashMap<>(getMetaData()));
		}

	}

	@Override
	public ADLBase uri(K9URI uri, Map<String, ? extends List<String>> requestHeaders) {
		ADLBase out = new AbstractADLBase(uri, requestHeaders, new HashMap<>()) {

			private String adlString;
			
			@Override
			public String getAsString() {
				if (adlString == null) {
					adlString = loadText(getUri(), requestHeaders);
				}
				
				return adlString;
			}
		};

		return out;
	}

	@Override
	public ADLBase adl(K9URI uri, String xml, Map<String, ? extends List<String>> requestHeaders) {
		ADLBase out = new AbstractADLBase(uri, requestHeaders, new HashMap<>()) {

			@Override
			public String getAsString() {
				return xml;
			}
			
		};
		return out;
	}
	
	
	public static Document createNewDocument(Kite9SVGTranscoder t) {
		ADLExtensibleDOMImplementation dom = t.getDomImplementation();
		return dom.createDocument(XMLHelper.KITE9_NAMESPACE, null, null);
	}

	private Kite9Transcoder createNewTranscoder(K9URI u) {
		Kite9Transcoder out = new Kite9TranscoderImpl(cache);
		if (u != null) {
			RequestParameters.configure(URIWrapper.from(u), out);
		}
		return out;
	}
	
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	protected Document parseXMLDocument(String content, K9URI uri2) {
		try {
			Document d = null;
			
			if (URIRewriter.localPublicContent(uri2)) {
				d = cache.getDocument(uri2.getPath());			
			}
			
			if (d == null) {
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(content));
				is.setSystemId(uri2.toString());
				d = builder.parse(is);
				cache.set(uri2.getPath(), Cache.DOCUMENT, d);
			}
			
			return d;
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't load: "+uri2, e);
		}
	}
	
	protected Document parseADLDocument(String content, K9URI uri2) {
		try {
			Kite9DocumentLoader l = ((Kite9TranscoderImpl) createNewTranscoder(uri2)).getDocLoader();
			InputStream is = new ByteArrayInputStream(content.getBytes());
			return l.loadDocument(uri2 == null ? null : uri2.toString(), is);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't load XML into DOM, URI: "+uri2, e, content, null);
		}
	}
	
	private String getFirst(Map<String, ? extends List<String>> map, String item) {
		List<String> elems = map.get(item);
		if ((elems == null) || (elems.size() == 0)) {
			return null;
		} else {
			return elems.get(0);
		}
	}

	protected boolean matchingHost(K9URI uri2, Map<String, ? extends List<String>> requestHeaders) {
		String host = getFirst(requestHeaders, "host");
		return (host != null) && (host.startsWith(uri2.getHost()));
	}
	
	
	
	public String loadText(K9URI uri2, Map<String, ? extends List<String>> requestHeaders) {
		try {
			WebClient webClient = WebClient.create(uri2.toString());
			ClientResponse cr = webClient
				.get()
				.header(HttpHeaders.ACCEPT,
						Kite9MediaTypes.ADL_SVG_VALUE,
						Kite9MediaTypes.SVG_VALUE,
						Kite9MediaTypes.TEXT_PLAIN_VALUE,
						Kite9MediaTypes.TEXT_XML_VALUE,
						Kite9MediaTypes.ALL_VALUE)
				.headers(h -> {
					if (matchingHost(uri2, requestHeaders)) {
						h.set("cookie", getFirst(requestHeaders, "cookie"));
					}
				})
				.exchange()
				.block();
			if (cr.statusCode().isError()) {
				String body = cr.bodyToMono(String.class).block();
				throw new Kite9XMLProcessingException("Error "+cr.statusCode().toString()+" loading content from "+uri2+"\n"+body, null, null);
			}
			
			return cr.bodyToMono(String.class).block();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't request XML from: "+uri2, e, null, null);
		}
	}
	
	public static String toXMLString(Node n, boolean omitDeclaration) {
		StringWriter output = new StringWriter();
		duplicate(n, omitDeclaration, new StreamResult(output));
		return output.toString();
	}

	public static void duplicate(Node n, boolean omitDeclaration, Result sr) {
		try {
		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    if (omitDeclaration) {
		    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    }
		    transformer.transform(new DOMSource(n), sr);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't serialize XML:", e, null, null);
		}
	}
	

	
	abstract class XMLDomImpl extends AbstractXMLBase implements XMLDom {
		
		private Document doc;
		
		public XMLDomImpl(K9URI uri, Map<String, ? extends List<String>> requestHeaders, Document doc, Map<String, Object> meta) {
			super(uri, requestHeaders, meta);
			this.doc = doc;
		}

		public Document getDocument() {
			return doc;
		}
	
		/**
		 * Since the dom is often in a state of change, we will compute the ADL 
		 * string from scratch when needed.
		 */
		@Override
		public String getAsString() {
			return toXMLString(getDocument(), true);
		}
		
	}

	class ADLDomImpl extends AbstractXMLBase implements ADLDom {

		private Document doc;

		public ADLDomImpl(K9URI uri, Map<String, ? extends List<String>> requestHeaders, Document d, Map<String, Object> meta) {
			super(uri, requestHeaders, meta);
			this.doc = d;
		}
		
		@Override
		public ADLOutput process(K9URI forLocation, DiagramWriteFormat df) {
			Kite9Transcoder transcoder = createNewTranscoder(forLocation);
			ADLOutput out = df.handleWrite(this, transcoder);
			return out;
		}

		@Override
		public Document parseDocument(String content, K9URI uri) {
			return ADLFactoryImpl.this.parseADLDocument(content, uri);
		}

		@Override
		public Document parseDocument(K9URI uri) {
			String text = ADLFactoryImpl.this.loadText(uri, getRequestHeaders());
			return parseDocument(text, uri);
		}

		@Override
		public String getAsString() {
			return toXMLString(doc, false);
		}

		@Override
		public Document getDocument() {
			return doc;
		}
	}

}
