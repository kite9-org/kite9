package org.kite9.diagram.batik.text;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.batik.bridge.FontFace;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.gvt.font.AWTFontFamily;
import org.apache.batik.gvt.font.AWTGVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Kite9LogImpl;
import org.kite9.diagram.logging.Logable;

/**
 * Doesn't resolve system fonts.  
 * 
 * Also handles the problem of fonts with the same name colliding in the Glyph 
 * renderer.
 */
public final class Kite9FontFamilyResolver implements FontFamilyResolver, Logable {
	
	private Kite9Log log = new Kite9LogImpl(this);

    public Kite9FontFamilyResolver() {
    }

    /**
     * The default font. This will be used when no font families can
     * be resolved for a particular text chunk/run.
     */
    private static final AWTFontFamily DEFAULT_FONT_FAMILY = new AWTFontFamily("SansSerif");

    private String getFaceKey(FontFace ff) {
    	return ff.getFamilyName()+" "+ff.getFontWeight()+" "+ff.getFontStyle();
    }
    
    /**
     * This keeps track of all the resolved font families. This is to hopefully
     * reduce the number of font family objects used.
     */
    private final Map<String, AWTFontFamily> resolvedFontFamilies = new HashMap<>();
    
    /**
     * This is used by FontFace to convert itself into a FontFamily.
     */
    public AWTFontFamily resolve(String familyName, FontFace fontFace) {
    	AWTFontFamily resolved = resolvedFontFamilies.get(getFaceKey(fontFace));
		log.send("Resolving (2-arg) "+familyName+" as "+resolved);
    	return resolved;
    }

    /**
     * This is used by FontFace when we are loading the font for the first time.
     */
    public GVTFontFamily loadFont(InputStream in, FontFace ff) throws Exception {
		log.send("loadFont "+ff.getFamilyName());

		try {
        	String faceKey = getFaceKey(ff);
			AWTFontFamily resolved = resolvedFontFamilies.get(faceKey);
			
        	if (resolved != null) {
        		in.close();
        		return resolved;
        	}
			git 
			// original version 
			Font font = Font.createFont(Font.TRUETYPE_FONT, in);

			// derive unique name by adding random weight
			// this only affects equals() method.
	    	Map<TextAttribute, Object> fontDetails = new HashMap<>();
	    	fontDetails.put(TextAttribute.WEIGHT, new Random().nextFloat());
	    	font = font.deriveFont(fontDetails);

			AWTFontFamily out = new AWTFontFamily(ff, font);
			resolvedFontFamilies.put(faceKey, out);
			log.send("Loaded "+faceKey+ " as "+out);
			return out;
		} catch (Exception e) {
			log.send("Couldn't load font for "+ff.getFamilyName()+e.toString());
		} 
    	
    	return null;
    }

    /**
     * This is called when a font family couldn't be resolved from a font-face, 
     * and therefore we need to resolve from the system or a previously-loaded
     * font-face.  We're not going to do that with Kite9.
     */
    public GVTFontFamily resolve(String familyName) {
		log.send("Resolving (1 arg) "+familyName+" as null");
		return null;
    }

    /**
     * This is a last-ditch attempt to find some font that has been loaded that supports
     * a given character.  
     */
    public GVTFontFamily getFamilyThatCanDisplay(char c) {
		log.send("Get Family That Can Display "+c);

//    	Font fonts = 
    		
    	//        for (int i = 0; i < awtFontFamilies.size(); i++) {
//            AWTFontFamily fontFamily = (AWTFontFamily)awtFontFamilies.get(i);
//            AWTGVTFont font = (AWTGVTFont)awtFonts.get(i);
//            if (font.canDisplay(c) && fontFamily.getFamilyName().indexOf("Song") == -1) {
//                // the awt font for "MS Song" doesn't display chinese glyphs correctly
//                return fontFamily;
//            }
//        }

        return null;
    }

    /** {@inheritDoc} */
    public GVTFontFamily getDefault() {
    	log.send("Getting default font");
		return DEFAULT_FONT_FAMILY;
    }

	@Override
	public String getPrefix() {
		return "FFR ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
