package com.kite9.server.command;

import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import org.apache.batik.anim.dom.AbstractElement;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.dom.AbstractAttr;
import org.apache.commons.io.Charsets;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonType;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class BatikCommandContext implements CommandContext {

    private static final Logger LOG = LoggerFactory.getLogger(BatikCommandContext.class);

    Parser sacParser;

    @Override
    public void log(@NotNull String message) {
        LOG.info(message);
    }

    public boolean elementIdExists(Document d, String id) {
         return ((SVGOMDocument)d).getElementById(id) != null;
    }

    @NotNull
    @Override
    public String uniqueId(@NotNull Document d) {
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
                    // fix for when batik reformats the style and adds newlines.
                    if (a.getType() == ComparisonType.ATTR_VALUE) {
                        String s1 = (String) a.getTestDetails().getValue();
                        String s2 = (String) a.getControlDetails().getValue();
                        if (s1.replaceAll("\\s", "").equals(s2.replaceAll("\\s", ""))) {
                            return;
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
        String xml = new String(Base64.getDecoder().decode(base64xml), Charsets.UTF_8);
        Document nDoc = (Document) adl.parseDocument(xml, null);
        Element n = nDoc.getDocumentElement();
        return n;
    }

    @NotNull
    @Override
    public String getStyleValue(@NotNull Element e, @NotNull String name) {
        if (e instanceof CSSStylableElement) {
            StyleMap sd = ((CSSStylableElement)e).get
            return sd.getPropertyValue(name)
        } else {
            return "";
        }

    }
}
