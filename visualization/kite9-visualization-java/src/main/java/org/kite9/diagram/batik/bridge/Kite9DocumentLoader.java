package org.kite9.diagram.batik.bridge;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.Kite9DocumentFactory;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;

/**
 * This contains functionality to load in elements referenced in templates.
 * 
 * @author robmoffat
 *
 */
public class Kite9DocumentLoader extends DocumentLoader implements Logable {
	
	private final Cache cache;
	private final ErrorHandler eh;
	private final Kite9Log log = Kite9Log.Companion.instance(this);

	public Kite9DocumentLoader(UserAgent userAgent, Kite9DocumentFactory dbf, Cache cache, ErrorHandler eh) {
		super(userAgent);
		this.documentFactory = dbf;
		this.cache = cache;
		this.eh = eh;
	}

	@Override
	public String getPrefix() {
		return "K9DL";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public Document checkCache(String uri) {
		Document d = uri == null ? null : super.checkCache(uri);
		if (d != null) {
			return d;
		} else {
			d = cache.getDocument(uri);
		}
		
		return d;
	}

	@Override
	public Document loadDocument(String uri) throws IOException {
		Document out = super.loadDocument(uri);
		cache.set(uri, Cache.DOCUMENT, out);
		return out;
	}

	@Override
	public Document loadDocument(String uri, InputStream is) throws IOException {
		Document out = super.loadDocument(uri, is);
		return out;
	}

	public Document loadXMLDocument(String uri, InputStream is) throws IOException {
		Document doc = cache.getDocument(uri);

		if (doc != null) {
			return doc;
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(eh);
			doc = db.parse(is);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't load document from "+uri, e);
		}

		synchronized (cacheMap) {
			cacheMap.put(uri, doc);
		}

		return doc;
	}
	
	
}