package org.kite9.diagram.dom.scripts;

import org.apache.batik.css.parser.CSSSACMediaList;
import org.apache.batik.css.parser.LexicalUnits;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.util.ParsedURL;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.LexicalUnit;

/**
 * Allows us to correctly parse at-rules, and in particular, the @script and @params ones.
 * 
 * @author robmoffat
 *
 */
public class AtRuleParser extends Parser {
	
	public AtRuleParser(ScriptHandler sh) {
		this.sh = sh;
	}

	private ScriptHandler sh;
	private String atKeyword;
	
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
		} else {
			super.parseAtRule();
		}
	}
		
	 /**
     * Parses a font-face rule.
     */
    protected void parseAtParamsRule() {
        try {
            documentHandler.startFontFace();

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
        } finally {
            documentHandler.endFontFace();
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

            String value = null;
            switch (current) {
            default:
                reportError("string");
                return;
            case LexicalUnits.STRING:
            case LexicalUnits.IDENTIFIER:
                value = scanner.getStringValue();
                nextIgnoreSpaces();
                break;
            case LexicalUnits.URI: 
            	value = new ParsedURL(new ParsedURL(documentURI), scanner.getStringValue()).toString();
                nextIgnoreSpaces();
            }
            
            sh.setParam(name, value);
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

	
}

