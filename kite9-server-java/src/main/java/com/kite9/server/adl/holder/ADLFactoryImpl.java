package com.kite9.server.adl.holder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jetbrains.annotations.NotNull;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.batik.format.Kite9TranscoderImpl;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.adl.holder.pipeline.AbstractXMLBase;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.uri.URIWrapper;
import com.kite9.server.web.URIRewriter;

public class ADLFactoryImpl implements ADLFactory {
	
	private final Cache cache;
	private final XMLHelper xmlHelper;
	private final String defaultTransform;

	public ADLFactoryImpl(Cache cache, XMLHelper xmlhelper, String defaultTransform) {
		super();
		this.cache = cache;
		this.xmlHelper = xmlhelper;
		this.defaultTransform = defaultTransform;
	}

	@NotNull
	@Override
	public ADLDom dom(@NotNull K9URI uri, @NotNull Document doc, @NotNull Map<String, ? extends List<String>> requestHeaders) {
		return new ADLDomImpl(uri, requestHeaders, doc, new HashMap<>());
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

			@Override
			public K9URI getUri() {
				return super.getUri();
			}
			
			
			
		};
		return out;
	}
	
	
	public static Document createNewDocument(Kite9SVGTranscoder t) {
		ADLExtensibleDOMImplementation dom = t.getDomImplementation();
		return dom.createDocument(Kite9Namespaces.ADL_NAMESPACE, null, null);
	}

	private Kite9Transcoder createNewTranscoder(K9URI u) {
		Kite9Transcoder out = new Kite9TranscoderImpl(cache, xmlHelper);
		out.addTranscodingHint(Kite9SVGTranscoder.KEY_DEFAULT_TEMPLATE, defaultTransform);
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
			InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			return l.loadXMLDocument(uri2 == null ? null : uri2.toString(), is);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't load XML into DOM, URI: "+uri2, e, content, null, null);
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
							Kite9MediaTypes.ADL_XML_VALUE,
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
				throw new Kite9XMLProcessingException("Error " + cr.statusCode().toString() + " loading content from " + uri2 + "\n" + body, null, null);
			}

			return cr.bodyToMono(String.class).block();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't request XML from: " + uri2, e, null, null);
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
			return xmlHelper.toXML(doc, false);
		}

		@Override
		public Document getDocument() {
			return doc;
		}
	}

}
