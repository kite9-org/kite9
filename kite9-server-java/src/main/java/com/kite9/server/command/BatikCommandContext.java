package com.kite9.server.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.batik.anim.dom.AbstractElement;
import org.apache.batik.dom.AbstractAttr;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.range.IntegerRange;
import org.kite9.diagram.dom.css.Kite9CSSParser;
import org.kite9.diagram.dom.managers.IntegerRangeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonType;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.command.Command;
import com.kite9.pipeline.command.CommandContext;


public class BatikCommandContext implements CommandContext {

    private static final Logger LOG = LoggerFactory.getLogger(BatikCommandContext.class);

    @Override
    public void log(String message) {
        LOG.info(message);
    }

    public boolean elementIdExists(Document d, String id) {
         return getElementById(d, id) != null;
    }

    @Override
    public String uniqueId(Document d) {
        int nextId = 1;
        while (elementIdExists(d, ""+(nextId))) {
            nextId ++;
        }

        return ""+nextId;
    }

    @Override
    public void setOwnerElement(Attr child, Element parent) {
        ((AbstractAttr)child).setOwnerElement((AbstractElement) parent);
    }

    public Command.Mismatch twoElementsAreIdentical(Element existing, Element with) {
        List<Comparison> out = new ArrayList<>();
        DiffBuilder
                .compare(Input.fromNode(existing).build())
                .withTest(Input.fromNode(with).build())
                .ignoreWhitespace()
                .ignoreComments()
                .withDifferenceListeners((a, b) ->
                {
                    if (a.getType() == ComparisonType.ATTR_VALUE) {
                    	boolean isStyleAttribute = a.getControlDetails().getXPath().endsWith("@style");
				        String s1 = (String) a.getTestDetails().getValue();
				        String s2 = (String) a.getControlDetails().getValue();
        
        				if (!isStyleAttribute) {
	                        // fix for when batik reformats the xml and adds newlines.
	                        if (s1.replaceAll("\\s", "").equals(s2.replaceAll("\\s", ""))) {
	                            return;
	                        }
        				} else {
        					// in this case, we are comparing styles.  
                        	// sometimes, there can be a slight difference, e.g. "color: red;" vs "color:red"
                        	// this is a hack to fix that
                        	if (s1.replaceAll("[\\s;]", "").equals(s2.replaceAll("[\\s;]", ""))) {
                                return;
                            }
        				}
                    }

                    out.add(a);


                })
                .build();


        return out.size() == 0 ? null : () -> out.stream()
                .map(m -> m.toString())
                .reduce("", (a, b) -> a+ "\n" + b);
    }


    public Element decodeElement(String base64xml, ADLDom adl) {
        String xml = new String(Base64.getDecoder().decode(base64xml), StandardCharsets.UTF_8);
        Document nDoc = adl.parseDocument(xml, null);
        Element n = nDoc.getDocumentElement();
        System.out.println("Converting: "+xml+" with id "+n.getAttribute("id")); //+" into " + new XMLHelper().toXML(n));
        return n;
    }

    @Override
    public String getStyleValue(Element e, String name) {
        Map<String, String> map = parseStyle(e);
        return map.get(name);
    }

    /**
     * This is a super-dumb, naive parser that understands nothing of css format.
     * Turns out this is a a better option than trying to create {@link LexicalUnit}s that
     * might not make sense.
     *
     * Yes, it doesn't understand semi-colons inside urls, quoted strings etc.
     */
    private Map<String, String> parseStyle(Element e) {
        String style = e.getAttribute("style");
        Map<String, String> out = new LinkedHashMap<>();

        Arrays.stream(style.split(";"))
                .filter(s -> s.contains(":"))
                .forEach(s -> {
                    int colon = s.indexOf(":");
                    String key = s.substring(0, colon).trim();
                    String value = s.substring(colon + 1).trim();
                    out.put(key, value);
                });

        return out;

    }

    private void replaceStyle(Element e, Map<String, String> map) {
        String newStyle = map.entrySet().stream()
                .map(s -> s.getKey()+": "+s.getValue()+"; ")
                .reduce("", String::concat);

        if (newStyle.trim().length() == 0) {
            e.removeAttribute("style");
        } else {
            e.setAttribute("style", newStyle);
        }

    }

    @Override
    public IntegerRange getStyleRangeValue(Element el, String name) {
        String val = parseStyle(el).get(name);
        if (val == null) {
            return null;
        } else {
            LexicalUnit lu = null;
            try {
                lu = new Kite9CSSParser().parsePropertyValue(val);
                return (IntegerRange) new IntegerRangeManager("none").createValue(lu, null);
            } catch (IOException e) {
                throw new Kite9XMLProcessingException("Can't parse Lexical Unit " + name,e);
            }
        }
    }

    @Override
    public void setStyleValue(Element e, String name, String value) {
        Map<String, String> map = parseStyle(e);
        if (value != null) {
            map.put(name, value);
        } else {
            map.remove(name);
        }
        replaceStyle(e, map);
    }

    @Override
    public void setAttributeValue(Element insert, String xpath, String value) {
        try {
            XPath xp = XPathFactory.newInstance().newXPath();
            Attr a = (Attr) xp.evaluate(xpath, insert, XPathConstants.NODE);
            a.setValue(value);
        } catch (XPathExpressionException e) {
            throw new Kite9XMLProcessingException("Couldn't set attribute value "+xpath,e);
        }
    }

    @Override
    public Element getElementById(Document doc, String fragmentId) {
        try {
            XPath xp = XPathFactory.newInstance().newXPath();
            Element el = (Element) xp.evaluate("//*[@id='"+fragmentId+"']", doc, XPathConstants.NODE);
            return el;
        } catch (XPathExpressionException e) {
            return null;
        }
    }
}
