package org.kite9.diagram.dom.scripts;

import org.apache.batik.css.parser.CSSSACMediaList;
import org.apache.batik.css.parser.LexicalUnits;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.util.ParsedURL;

/**
 * Allows us to correctly parse at-rules, and in particular, the script one.
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
		} else {
			super.parseAtRule();
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

