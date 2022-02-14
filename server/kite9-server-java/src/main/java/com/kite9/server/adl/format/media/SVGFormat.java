package com.kite9.server.adl.format.media;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.NotKite9DiagramException;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.uri.K9URI;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.io.Charsets;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.format.Kite9Transcoder;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.springframework.http.HttpHeaders;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns encapsulated SVG.  i.e. with no references to external
 * media like fonts, images etc.
 *
 * @author robmoffat
 *
 */
public class SVGFormat extends AbstractSVGFormat implements DiagramFileFormat {

    private final ADLFactory factory;

    public SVGFormat(ADLFactory factory, XMLHelper helper) {
        super(helper);
        this.factory = factory;
    }

    private final List<K9MediaType> mediaTypes = Collections.singletonList(Kite9MediaTypes.INSTANCE.getSVG());

    public List<K9MediaType> getMediaTypes() {
        return mediaTypes;
    }

    public String getExtension() {
        return "svg";
    }

    @Override
    public boolean isBinaryFormat() {
        return true;
    }


    @Override
    public ADLBase handleRead(InputStream someFormat, K9URI in, HttpHeaders headers) throws Exception {
        HttpHeaders newHeaders = HttpHeaders.writableHttpHeaders(headers);
        newHeaders.set(HttpHeaders.ACCEPT, getMediaTypes().stream().map(Object::toString).collect(Collectors.joining(",")));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // an instance of builder to parse the specified xml file
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(someFormat);
        return extractEncodedADLInSVG(in, headers, doc);
    }

    public ADLBase extractEncodedADLInSVG(K9URI in, HttpHeaders headers, Document doc) {
        try {
            Element e2 = findFirst(doc.getDocumentElement(), "defs");
            Element e3 = findFirst(e2, "script");
            String content = e3.getTextContent();
            byte[] xml = Base64.getDecoder().decode(content);
            return factory.adl(in, new String(xml, Charsets.UTF_8), headers);
        } catch (Exception e) {
            throw new NotKite9DiagramException("Couldn't decode ADL in "+in+".  Is this definitely a Kite9 Diagram?", e);
        }
    }

    private static Element findFirst(Element e, String name) {
        NodeList out = e.getElementsByTagNameNS(SVGConstants.SVG_NAMESPACE_URI, name);
        if (out.getLength() == 0) {
            throw new Kite9ProcessingException("Couldn't find "+name);
        } else {
            return (Element) out.item(0);
        }
    }

    @Override
    protected void setupTranscoder(Kite9Transcoder t, ADLDom toWrite) {
        t.addTranscodingHint(Kite9SVGTranscoder.KEY_ENCAPSULATING, true);
    }


}