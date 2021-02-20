package org.kite9.diagram.dom.css;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.css.parser.CSSSACMediaList;
import org.apache.batik.css.parser.LexicalUnits;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.LexicalUnit;

/**
 * Allows us to correctly parse new kite9 at-rules the @script, @params and @defs ones.
 * 
 * Also allows regular attributes to begin with "-", which is common in browser-specific extensions.
 * 
 * @author robmoffat
 *
 */
public class Kite9CSSParser extends Parser {
	
	public Kite9CSSParser() {
	}

	private ScriptHandler sh;
	private String atKeyword;
	
	@Override
	public void setDocumentHandler(DocumentHandler handler) {
		super.setDocumentHandler(handler);
		if (handler instanceof ScriptHandler) {
			sh = (ScriptHandler) handler;
		}
	}

	@Override
	protected int nextIgnoreSpaces() {
		if (current == LexicalUnits.AT_KEYWORD) {
			atKeyword = scanner.getStringValue();
		}
			
		return super.nextIgnoreSpaces();
	}

	@Override
	protected void parseAtRule() {
		if (atKeyword.equals("script")) {
			parseAtScriptRule();
		} else if (atKeyword.equals("params")){
			parseAtParamsRule();
		} else if (atKeyword.equals("defs")) {
			parseAtDefsRule();
		} else {
			super.parseAtRule();
		}
	}
		
	/**
     * Parses a params rule.
     */
    protected void parseAtParamsRule() {
        if (current != LexicalUnits.LEFT_CURLY_BRACE) {
            reportError("left.curly.brace");
        } else {
            nextIgnoreSpaces();

            try {
                parseParamDeclaration();
            } catch (CSSParseException e) {
                reportError(e);
            }
        }
    }
	
    /**
     * Parses the given reader.
     */
    protected void parseParamDeclaration()
        throws CSSException {
        for (;;) {
        	
            switch (current) {
            case LexicalUnits.EOF:
                throw createCSSParseException("eof");
            case LexicalUnits.RIGHT_CURLY_BRACE:
                nextIgnoreSpaces();
                return;
            case LexicalUnits.SEMI_COLON:
                nextIgnoreSpaces();
                continue;
            default:
                throw createCSSParseException("identifier");
            case LexicalUnits.IDENTIFIER:
            }

            String name = scanner.getStringValue();

            if (nextIgnoreSpaces() != LexicalUnits.COLON) {
                throw createCSSParseException("colon");
            }
            nextIgnoreSpaces();

            List<String> found = parseParamValues();
            
            if (found.size() == 1) {
            	sh.setParam(name, found.get(0)); 
            } else if (found.size() > 1) {
            	sh.setParam(name, found);
            }
        }
    }
	

	private List<String> parseParamValues() {
		List<String> out = new ArrayList<>();
    	
        for (;;) {
            switch (current) {
            default:
                reportError("string");
                return out;
            case LexicalUnits.SEMI_COLON:
            	return out;
            case LexicalUnits.STRING:
            case LexicalUnits.IDENTIFIER:
            	out.add(scanner.getStringValue());
                nextIgnoreSpaces();
                break;
            case LexicalUnits.URI: 
            	out.add(new ParsedURL(new ParsedURL(documentURI), scanner.getStringValue()).toString());
                nextIgnoreSpaces();
            }
        }
	}

	protected void parseAtScriptRule() {
        String uri = null;
        switch (current) {
        default:
            reportError("string.or.uri");
            return;
        case LexicalUnits.STRING:
        case LexicalUnits.URI:
            uri = scanner.getStringValue();
            nextIgnoreSpaces();
        }

        CSSSACMediaList ml;
        if (current != LexicalUnits.IDENTIFIER) {
            ml = new CSSSACMediaList();
            ml.append("all");
        } else {
            ml = parseMediaList();
        }

        sh.importScript(new ParsedURL(new ParsedURL(documentURI), uri).toString(), ml);

        if (current != LexicalUnits.SEMI_COLON) {
            reportError("semicolon");
        } else {
            next();
        }
	}
	
	protected void parseAtDefsRule() {
        String uri = null;
        switch (current) {
        default:
            reportError("string.or.uri");
            return;
        case LexicalUnits.STRING:
        case LexicalUnits.URI:
            uri = scanner.getStringValue();
            nextIgnoreSpaces();
        }

        CSSSACMediaList ml;
        if (current != LexicalUnits.IDENTIFIER) {
            ml = new CSSSACMediaList();
            ml.append("all");
        } else {
            ml = parseMediaList();
        }

        sh.importDefs(new ParsedURL(new ParsedURL(documentURI), uri).toString(), ml);

        if (current != LexicalUnits.SEMI_COLON) {
            reportError("semicolon");
        } else {
            next();
        }
	}


	/**
	 * This allows proper error reporting within style declarations.
	 */
	@Override
	public void parseStyleDeclaration(String source) throws CSSException, IOException {
		this.documentURI = source;
		super.parseStyleDeclaration(source);
	}

	
	/**
     * Overrides the style declaration parser to allow a dash-prefix on styles.
     */
    protected void parseStyleDeclaration(boolean inSheet)
        throws CSSException {
    	
        for (;;) {
        	String name = "";
            switch (current) {
            case LexicalUnits.EOF:
                if (inSheet) {
                    throw createCSSParseException("eof");
                }
                return;
            case LexicalUnits.RIGHT_CURLY_BRACE:
                if (!inSheet) {
                    throw createCSSParseException("eof.expected");
                }
                nextIgnoreSpaces();
                return;
            case LexicalUnits.SEMI_COLON:
                nextIgnoreSpaces();
                continue;
            default:
                throw createCSSParseException("identifier");
            case LexicalUnits.MINUS:
            	current = scanner.next();
            	
            	if (current != LexicalUnits.IDENTIFIER) {
                    throw createCSSParseException("identifier");
            	}
            	
            	name = "-";
            	// drop through
            case LexicalUnits.IDENTIFIER:
            }

            name = name + scanner.getStringValue();

            if (nextIgnoreSpaces() != LexicalUnits.COLON) {
                throw createCSSParseException("colon");
            }
            nextIgnoreSpaces();

            LexicalUnit exp = null;

            try {
                exp = parseExpression(false);
            } catch (CSSParseException e) {
                reportError(e);
            }

            if (exp != null) {
                boolean important = false;
                if (current == LexicalUnits.IMPORTANT_SYMBOL) {
                    important = true;
                    nextIgnoreSpaces();
                }
                documentHandler.property(name, exp, important);
            }
        }
    }
	
}
