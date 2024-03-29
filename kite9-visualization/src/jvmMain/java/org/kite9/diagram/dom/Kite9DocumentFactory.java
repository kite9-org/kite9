package org.kite9.diagram.dom;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.util.MimeTypeConstants;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.Properties;

/**
 * This class contains methods for creating SVGDocument instances
 * from an URI using SAX2.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class Kite9DocumentFactory
    extends    SAXDocumentFactory
    implements SVGDocumentFactory {

    public static final Object LOCK = new Object();

    /**
     * Key used for public identifiers
     */
    public static final String KEY_PUBLIC_IDS = "publicIds";

    /**
     * Key used for public identifiers
     */
    public static final String KEY_SKIPPABLE_PUBLIC_IDS = "skippablePublicIds";

    /**
     * Key used for the skippable DTD substitution
     */
    public static final String KEY_SKIP_DTD = "skipDTD";

    /**
     * Key used for system identifiers
     */
    public static final String KEY_SYSTEM_ID = "systemId.";

    /**
     * The dtd public IDs resource bundle class name.
     */
    protected static final String DTDIDS =
        "org.apache.batik.dom.svg.resources.dtdids";

    /**
     * Constant for HTTP content type header charset field.
     */
    protected static final String HTTP_CHARSET = "charset";

    /**
     * The accepted DTD public IDs.
     */
    protected static String dtdids;

    /**
     * The DTD public IDs we know we can skip.
     */
    protected static String skippable_dtdids;

    /**
     * The DTD content to use when skipping
     */
    protected static String skip_dtd;

    /**
     * The ResourceBunder for the public and system ids
     */
    protected static Properties dtdProps;

    /**
     * Creates a new SVGDocumentFactory object.
     * @param parser The SAX2 parser classname.
     */
    public Kite9DocumentFactory(DOMImplementation domImpl, String parser, ErrorHandler errorHandler) {
        super(domImpl, parser, true);
        setErrorHandler(errorHandler);
    }

    public SVGDocument createSVGDocument(String uri) throws IOException {
        return (SVGDocument)createDocument(uri);
    }

    /**
     * Creates a SVG Document instance.
     * @param uri The document URI.
     * @param inp The document input stream.
     * @exception IOException if an error occured while reading the document.
     */
    public SVGDocument createSVGDocument(String uri, InputStream inp)
        throws IOException {
        return (SVGDocument)createDocument(uri, inp);
    }

    /**
     * Creates a SVG Document instance.
     * @param uri The document URI.
     * @param r The document reader.
     * @exception IOException if an error occured while reading the document.
     */
    public SVGDocument createSVGDocument(String uri, Reader r)
        throws IOException {
        return (SVGDocument)createDocument(uri, r);
    }

    /**
     * Creates a SVG Document instance.
     * 
     * This method supports gzipped sources.
     * @param uri The document URI.
     * @exception IOException if an error occured while reading the document.
     */
    public Document createDocument(String uri) throws IOException {
        ParsedURL purl = new ParsedURL(uri);

        InputStream is = purl.openStream
           (MimeTypeConstants.MIME_TYPES_SVG_LIST.iterator());
        uri = purl.getPostConnectionURL();

        InputSource isrc = new InputSource(is);

        // now looking for a charset encoding in the content type such
        // as "image/svg+xml; charset=iso8859-1" this is not official
        // for image/svg+xml yet! only for text/xml and maybe
        // for application/xml
        String contentType = purl.getContentType();
        int cindex = -1;
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            cindex = contentType.indexOf(HTTP_CHARSET);
        }

        String charset = null;
        if (cindex != -1) {
            int i                 = cindex + HTTP_CHARSET.length();
            int eqIdx = contentType.indexOf('=', i);
            if (eqIdx != -1) {
                eqIdx++; // no one is interested in the equals sign...

                // The patch had ',' as the terminator but I suspect
                // that is the delimiter between possible charsets,
                // but if another 'attribute' were in the accept header
                // charset would be terminated by a ';'.  So I look
                // for both and take to closer of the two.
                int idx     = contentType.indexOf(',', eqIdx);
                int semiIdx = contentType.indexOf(';', eqIdx);
                if ((semiIdx != -1) && ((semiIdx < idx) || (idx == -1)))
                    idx = semiIdx;
                if (idx != -1)
                    charset = contentType.substring(eqIdx, idx);
                else
                    charset = contentType.substring(eqIdx);
                charset = charset.trim();
                isrc.setEncoding(charset);
            }
        }

        isrc.setSystemId(uri);

        SVGOMDocument doc = (SVGOMDocument) super.createDocument
            (SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", uri, isrc);
        doc.setParsedURL(new ParsedURL(uri));
        doc.setDocumentInputEncoding(charset);
        doc.setXmlStandalone(isStandalone);
        doc.setXmlVersion(xmlVersion);

        return doc;
    }

    /**
     * Creates a SVG Document instance.
     * @param uri The document URI.
     * @param inp The document input stream.
     * @exception IOException if an error occured while reading the document.
     */
    public Document createDocument(String uri, InputStream inp)
        throws IOException {
        Document doc;
        InputSource is = new InputSource(inp);
        is.setSystemId(uri);

        try {
            doc = createDocument
                (SVGConstants.SVG_NAMESPACE_URI, "svg", uri, is);
            if (uri != null) {
                ((SVGOMDocument)doc).setParsedURL(new ParsedURL(uri));
            }

            AbstractDocument d = (AbstractDocument) doc;
            d.setDocumentURI(uri);
            d.setXmlStandalone(isStandalone);
            d.setXmlVersion(xmlVersion);
        } catch (MalformedURLException e) {
            throw new IOException(e.getMessage());
        } catch (IOException e) {
        	throw e;
        }
        return doc;
    }

    /**
     * Creates a SVG Document instance.
     * @param uri The document URI.
     * @param r The document reader.
     * @exception IOException if an error occured while reading the document.
     */
    public Document createDocument(String uri, Reader r)
        throws IOException {
        Document doc;
        InputSource is = new InputSource(r);
        is.setSystemId(uri);

        try {
            doc = createDocument(null, null, uri, is);
            if (uri != null) {
                ((SVGOMDocument)doc).setParsedURL(new ParsedURL(uri));
            }

            AbstractDocument d = (AbstractDocument) doc;
            d.setDocumentURI(uri);
            d.setXmlStandalone(isStandalone);
            d.setXmlVersion(xmlVersion);
        } catch (MalformedURLException e) {
            throw new IOException(e.getMessage());
        }
        return doc;
    }

    public DOMImplementation getDOMImplementation(String ver) {
        return implementation;
    }

    /**
     * <b>SAX</b>: Implements {@link
     * org.xml.sax.ContentHandler#startDocument()}.
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        // Do not assume namespace declarations when no DTD has been specified.
        // namespaces.put("", SVGDOMImplementation.SVG_NAMESPACE_URI);
        // namespaces.put("xlink", XLinkSupport.XLINK_NAMESPACE_URI);
    }

    /**
     * <b>SAX2</b>: Implements {@link
     * org.xml.sax.EntityResolver#resolveEntity(String,String)}.
     */
    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
        try {
            synchronized (LOCK) {
                // Bootstrap if needed - move to a static block???
                if (dtdProps == null) {
                    dtdProps = new Properties();
                    try {
                        Class<Kite9DocumentFactory> cls = Kite9DocumentFactory.class;
                        InputStream is = cls.getResourceAsStream
                            ("resources/dtdids.properties");
                        dtdProps.load(is);
                    } catch (IOException ioe) {
                        throw new SAXException(ioe);
                    }
                }

                if (dtdids == null)
                    dtdids = dtdProps.getProperty(KEY_PUBLIC_IDS);

                if (skippable_dtdids == null)
                    skippable_dtdids =
                        dtdProps.getProperty(KEY_SKIPPABLE_PUBLIC_IDS);

                if (skip_dtd == null)
                    skip_dtd = dtdProps.getProperty(KEY_SKIP_DTD);
            }

            if (publicId == null)
                return null; // Let SAX Parser find it.

            if (!isValidating &&
                (skippable_dtdids.indexOf(publicId) != -1)) {
                // We are not validating and this is a DTD we can
                // safely skip so do it...  Here we provide just enough
                // of the DTD to keep stuff running (set svg and
                // xlink namespaces).
                return new InputSource(new StringReader(skip_dtd));
            }

            if (dtdids.indexOf(publicId) != -1) {
                String localSystemId =
                    dtdProps.getProperty(KEY_SYSTEM_ID +
                                         publicId.replace(' ', '_'));

                if (localSystemId != null && !"".equals(localSystemId)) {
                    return new InputSource
                        (getClass().getResource(localSystemId).toString());
                }
            }
        } catch (MissingResourceException e) {
            throw new SAXException(e);
        }
        // Let the SAX parser find the entity.
        return null;
    }

    protected Document createDocument(String ns, String root, String uri,
                                      InputSource is)
            throws IOException {
        Document ret = createDocument(is);
        Element docElem = ret.getDocumentElement();

        if (root != null) {
            String lname = root;
            String nsURI = ns;
            if (ns == null) {
                int idx = lname.indexOf(':');
                String nsp = (idx == -1 || idx == lname.length() - 1)
                        ? ""
                        : lname.substring(0, idx);
                nsURI = namespaces.get(nsp);
                if (idx != -1 && idx != lname.length() - 1) {
                    lname = lname.substring(idx + 1);
                }
            }


            String docElemNS = docElem.getNamespaceURI();
            if ((docElemNS != nsURI) &&
                    ((docElemNS == null) || (!docElemNS.equals(nsURI))))
                throw new IOException
                        ("Root element namespace does not match that requested:\n" +
                                "Requested: " + nsURI + "\n" +
                                "Found: " + docElemNS);

            if (docElemNS != null) {
                if (!docElem.getLocalName().equals(lname))
                    throw new IOException
                            ("Root element does not match that requested:\n" +
                                    "Requested: " + lname + "\n" +
                                    "Found: " + docElem.getLocalName());
            } else {
                if (!docElem.getNodeName().equals(lname))
                    throw new IOException
                            ("Root element does not match that requested:\n" +
                                    "Requested: " + lname + "\n" +
                                    "Found: " + docElem.getNodeName());
            }

        }

        return ret;
    }
}
