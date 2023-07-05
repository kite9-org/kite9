package org.kite9.diagram.dom.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.css.parser.Scanner;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SelectorList;

/**
 * Makes use of the HoudiniScanner, for reading kite9 css directives.
 * 
 * Also allows regular attributes to begin with "-", which is common in browser-specific extensions.
 * 
 * @author robmoffat
 *
 */
public class Kite9CSSParser extends Parser {
	
	public Kite9CSSParser() {
	}
	
	/**
	 * This allows proper error reporting within style declarations.
	 */
	@Override
	public void parseStyleDeclaration(String source) throws CSSException, IOException {
		this.documentURI = source;
		scanner = new HoudiniScanner(source);
		parseStyleDeclarationInternal();
	}

    protected Scanner createScanner(InputSource source) {
        documentURI = source.getURI();
        if (documentURI == null) {
            documentURI = "";
        }

        Reader r = source.getCharacterStream();
        if (r != null) {
            return new HoudiniScanner(r);
        }

        InputStream is = source.getByteStream();
        if (is != null) {
            return new HoudiniScanner(is, source.getEncoding());
        }

        String uri = source.getURI();
        if (uri == null) {
            throw new CSSException(formatMessage("empty.source", null));
        }

        try {
            ParsedURL purl = new ParsedURL(uri);
            is = purl.openStreamRaw(CSSConstants.CSS_MIME_TYPE);
            return new HoudiniScanner(is, source.getEncoding());
        } catch (IOException e) {
            throw new CSSException(e);
        }
    }

    /**
     * Implements {@link ExtendedParser#parseRule(String)}.
     */
    public void parseRule(String source) throws CSSException, IOException {
        scanner = new HoudiniScanner(source);
        parseRuleInternal();
    }

    /**
     * Implements {@link ExtendedParser#parseSelectors(String)}.
     */
    public SelectorList parseSelectors(String source)
            throws CSSException, IOException {
        scanner = new HoudiniScanner(source);
        return parseSelectorsInternal();
    }

    /**
     * Implements {@link ExtendedParser#parsePropertyValue(String)}.
     */
    public LexicalUnit parsePropertyValue(String source)
            throws CSSException, IOException {
        scanner = new HoudiniScanner(source);
        return parsePropertyValueInternal();
    }

    /**
     * Implements {@link ExtendedParser#parsePriority(String)}.
     */
    public boolean parsePriority(String source)
            throws CSSException, IOException {
        scanner = new HoudiniScanner(source);
        return parsePriorityInternal();
    }
    
    /**
     * Creates a parse exception.
     */
    protected CSSParseException createCSSParseException(String key,
                                                        Object[] params) {
    	int currentLine = scanner.getLine();
    	int currentColumn = scanner.getColumn();
    	String contents = ((HoudiniScanner) scanner).getReadContents();
    	
        return new ContextualCSSParseException(formatMessage(key, params),
                                     documentURI,
                                     contents,
                                     currentLine,
                                     currentColumn);
    }
}

