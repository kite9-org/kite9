
package com.kite9.server.adl.holder.meta;

import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.meta.MetaRead;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.XMLBase;

/**
 * Handles placing ADL content and metadata within the SVG document.  Some helper functions for the ADLFactory.
 */
public class Payload {

    public static final String ADL_MARKUP_ID = "adl:markup";

    public static void insertADLInformationIntoSVG(ADLDom adl, Document svgDocument) {
        insertEncodedADL(adl, svgDocument);
        insertMetadata(adl, svgDocument);
    }

    private static void insertMetadata(MetaRead adl, Document svgDocument) {
        boolean hasMetadata = !adl.getMetaData().isEmpty();
        NodeList nl = svgDocument.getElementsByTagNameNS(SVG_NAMESPACE_URI, "metadata");
        Element firstMeta = null;
        if (nl.getLength() > 0) {
            firstMeta = (Element) nl.item(0);
        } else if (hasMetadata) {
            Element documentElement = svgDocument.getDocumentElement();
            firstMeta = svgDocument.createElementNS(SVG_NAMESPACE_URI, "metadata");
            Node firstChild = documentElement.getChildNodes().item(0);
            documentElement.insertBefore(firstMeta, firstChild);
        }

        if (hasMetadata) {
            // remove any old metadata
            NodeList children = firstMeta.getChildNodes();
            while (children.getLength() > 0) {
                firstMeta.removeChild(children.item(0));
            }

            // add new items
            firstMeta.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:md", Kite9Namespaces.META_NAMESPACE);

            addMetadata(adl, svgDocument, firstMeta);
        }
    }

    protected static void addMetadata(MetaRead adl, Document doc, Element inside) {
    	List<String> keys = new ArrayList<>(adl.getMetaData().keySet());
    	Collections.sort(keys);
        for (String k : keys) {
        	Object v = adl.getMetaData().get(k);
            if (v != null) {
                Element entry = createContent(doc, k, v);
                inside.appendChild(entry);
            }
        }
    }

    public static Document createMetaDocument(MetaRead m) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:metadata");
            doc.appendChild(rootElement);
            addMetadata(m, doc, rootElement);
            return doc;
        } catch (Exception e) {
            throw new Kite9XMLProcessingException("Couldn't create meta document: ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Element createContent(Document svgDocument, String key, Object value) {
        Element entry = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:"+key);
        if (value instanceof List) {
            for (Object o : (List<Object>) value) {
                // currently only users are nested
                Element nested = createContent(svgDocument, "user", o);
                entry.appendChild(nested);
            }
        } else if (value instanceof UserMeta) {
            Element id = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:id");
            id.setTextContent(((UserMeta) value).getId());
            entry.appendChild(id);
            Element name = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:name");
            name.setTextContent(((UserMeta) value).getDisplayName());
            entry.appendChild(name);
            Element icon = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:icon");
            icon.setTextContent(((UserMeta) value).getIcon());
            entry.appendChild(icon);
            Element page = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:page");
            page.setTextContent(((UserMeta) value).getPage());
            entry.appendChild(page);
            Element login = svgDocument.createElementNS(Kite9Namespaces.META_NAMESPACE, "md:login");
            login.setTextContent(((UserMeta) value).getLogin());
            entry.appendChild(login);
        } else if (value instanceof Object) {
            entry.setTextContent(value.toString());
        }

        return entry;
    }

    protected static void insertEncodedADL(ADLDom adl, Document svgDocument) {
        String base64Encoded = createBase64ADLString(adl);
        Element adlScriptTag = svgDocument.getElementById(ADL_MARKUP_ID);
        if (adlScriptTag == null) {
            adlScriptTag = createScriptTag(svgDocument);
        }
        adlScriptTag.setTextContent(base64Encoded);
    }

    public static String createBase64ADLString(ADLDom adl) {
        byte[] bytes = adl.getAsString().getBytes();
        String base64Encoded = new String(Base64.getEncoder().encode(bytes));
        return base64Encoded;
    }

    private static Element createScriptTag(Document svgDocument) {
        NodeList nl = svgDocument.getElementsByTagNameNS(SVG_NAMESPACE_URI, "defs");
        Element firstDef = null;
        if (nl.getLength() == 0) {
            Element documentElement = svgDocument.getDocumentElement();
            firstDef = svgDocument.createElementNS(SVG_NAMESPACE_URI, "defs");
            Node firstChild = documentElement.getChildNodes().item(0);
            documentElement.insertBefore(firstDef, firstChild);
        } else {
            firstDef = (Element) nl.item(0);
        }

        // add the script tag
        Element scriptTag = svgDocument.createElementNS(SVG_NAMESPACE_URI, "script");
        scriptTag.setAttribute("type", Kite9MediaTypes.ADL_XML_VALUE +";base64");
        scriptTag.setAttribute("id", ADL_MARKUP_ID);

        firstDef.appendChild(scriptTag);
        return scriptTag;
    }


}