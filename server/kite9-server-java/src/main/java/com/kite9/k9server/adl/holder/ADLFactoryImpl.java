package com.kite9.k9server.adl.holder;

import com.kite9.k9server.adl.holder.meta.BasicMeta;
import com.kite9.k9server.adl.format.media.DiagramReadFormat;
import com.kite9.k9server.adl.format.media.DiagramWriteFormat;
import com.kite9.k9server.adl.format.media.Kite9MediaTypes;
import com.kite9.k9server.adl.holder.pipeline.*;
import com.kite9.k9server.uri.URI;
import org.apache.batik.css.engine.CSSEngine;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9ProcessingException;
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
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADLFactoryImpl implements ADLFactory {
	
	private Cache cache;

	public ADLFactoryImpl(Cache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public ADLDom emptyAdlDom(URI uri, Map<String, ? extends List<String>> requestHeaders) {
		Document out = createNewDocument(createNewTranscoder(uri));
		return new ADLDomImpl(uri, requestHeaders, out, new HashMap<>());
	}

	@Override
	public ADLBase uri(URI uri, Map<String, List<String>> requestHeaders) {
		ADLBase out = new ADLBaseImpl(uri, requestHeaders, new HashMap<>()) {

			private String adlString;
			
			@Override
			public String getXMLString() {
				if (adlString == null) {
					adlString = loadText(getUri(), requestHeaders);
				}
				
				return adlString;
			}
		};
		return out;
	}

	@Override
	public ADLBase adl(URI uri, String xml, Map<String, List<String>> requestHeaders) {
		ADLBase out = new ADLBaseImpl(uri, requestHeaders, new HashMap<>()) {

			@Override
			public String getXMLString() {
				return xml;
			}
			
		};
		return out;
	}
	
	
	public static Document createNewDocument(Kite9SVGTranscoder t) {
		ADLExtensibleDOMImplementation dom = t.getDomImplementation();
		return dom.createDocument(XMLHelper.KITE9_NAMESPACE, null, null);
	}

	private Kite9SVGTranscoder createNewTranscoder(URI u) {
		Kite9SVGTranscoder out = new Kite9SVGTranscoder(cache);
		if (u != null) {
			RequestParameters.configure(u, out);
		}
		return out;
	}
	
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	protected Document parseXMLDocument(String content, URI uri2) {
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
	
	protected ADLDocument parseADLDocument(String content, URI uri2) {
		try {
			Kite9DocumentLoader l = createNewTranscoder(uri2).getDocLoader();
			InputStream is = new ByteArrayInputStream(content.getBytes());
			return (ADLDocument) l.loadDocument(uri2 == null ? null : uri2.toString(), is);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't load XML into DOM, URI: "+uri2, e, content, null);
		}
	}
	
	

	protected boolean matchingHost(URI uri2, Map<String, List<String>> requestHeaders) {
		String host = requestHeaders.getFirst("host");
		return (host != null) && (host.startsWith(uri2.getHost()));
	}
	
	
	
	public String loadText(URI uri2, Map<String, List<String>> requestHeaders) {
		try {
			WebClient webClient = WebClient.create(uri2.toString());
			ClientResponse cr = webClient
				.get()
				.accept(Kite9MediaTypes.ADL_SVG, Kite9MediaTypes.SVG, MediaType.TEXT_PLAIN, MediaType.TEXT_XML, MediaType.ALL)
				.headers(h -> {
					if (matchingHost(uri2, requestHeaders)) {
						h.set("cookie", requestHeaders.getFirst("cookie"));
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
	
	abstract class XMLBaseImpl extends BasicMeta implements XMLBase {
		
		protected Map<String, List<String>> requestHeaders;
		
		public XMLBaseImpl(URI uri, Map<String, List<String>> requestHeaders, Map<String, Object> meta) {
			super(meta);
			setUri(uri);
			this.requestHeaders = requestHeaders;
		}
		
		@Override
		public Map<String, List<String>> getRequestHeaders() {
			return requestHeaders;
		}

	}
	
	abstract class ADLBaseImpl extends XMLBaseImpl implements ADLBase {
		
		public ADLBaseImpl(URI uri, Map<String, List<String>> requestHeaders, Map<String, Object> meta) {
			super(uri, requestHeaders, meta);
		}
		
		@Override
		public ADLDom parse() {
			return new ADLDomImpl(getUri(), requestHeaders, parseADLDocument(getXMLString(), getUri()), new HashMap<>(metadata));
		}
		
	}
	
	abstract class XMLDomImpl extends XMLBaseImpl implements XMLDom {
		
		private Document doc;
		
		public XMLDomImpl(URI uri, Map<String, ? extends List<String>> requestHeaders, Document doc, Map<String, Object> meta) {
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
		public String getXMLString() {
			return toXMLString(getDocument(), true);
		}
		
	}
	
	
	class ADLDomImpl extends XMLDomImpl implements ADLDom {

		public ADLDomImpl(URI uri, Map<String, ? extends List<String>> requestHeaders, Document doc, Map<String, Object> meta) {
			super(uri, requestHeaders, doc, meta);
		}
		
		@Override
		public <X extends DiagramWriteFormat> ADLOutput<X> process(URI forLocation, X df) {
			try {
				Kite9SVGTranscoder transcoder = createNewTranscoder(forLocation);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				df.handleWrite(this, baos, transcoder);
				
				return new ADLOutputImpl<X>(forLocation, requestHeaders, baos.toByteArray(), df, this) {
					
					String xml = null;
					
					@Override
					public String getXMLString() {
						if (xml == null) {
							xml = ADLDomImpl.this.getXMLString();
						}
						return xml;
					}
					
					
				};
			} catch (Exception e) {
				throw new Kite9XMLProcessingException("Couldn't produce output format "+df.getExtension(), e, getDocument());
			}
		}

		@Override
		public void ensureCssEngine(Document doc) {
			Kite9SVGTranscoder transcoder = createNewTranscoder(getUri());
			CSSEngine engine = transcoder.getDomImplementation().createCSSEngine(doc, transcoder.createBridgeContext());
			doc.setCSSEngine(engine);
		}


		@Override
		public Document parseDocument(String content, URI uri) {
			return ADLFactoryImpl.this.parseADLDocument(content, uri);
		}

		@Override
		public Document parseDocument(URI uri) {
			String text = ADLFactoryImpl.this.loadText(uri, requestHeaders);
			return parseDocument(text, uri);
		}

	}
	
	abstract class ADLOutputImpl<X extends DiagramWriteFormat> extends XMLBaseImpl implements ADLOutput<X> {

		private byte[] bytes;
		private X df;
		private ADLDom orig;

		public ADLOutputImpl(URI uri, Map<String, List<String>> requestHeaders, byte[] bytes, X df, ADLDom orig) {
			super(uri, requestHeaders, new HashMap<>(orig.getMetaData()));
			this.bytes = bytes;
			this.df = df;
			this.orig = orig;
			this.metadata = new HashMap<>(orig.getMetaData());
		}

		@Override
		public byte[] getAsBytes() throws Kite9ProcessingException {
			return bytes;
		}

		@Override
		public X getFormat() {
			return df;
		}

		@Override
		public ADLDom originatingADLDom() {
			return orig;
		}
		
		@Override
		public String getAsString() {
			if ((df instanceof DiagramReadFormat) && (!((DiagramReadFormat)df).isBinaryFormat())) {
				return new String(bytes, Charsets.UTF_8);
			} else {
				throw new Kite9ProcessingException("Can't get string from format "+df.getExtension());
			}
		}

		
	}
}
