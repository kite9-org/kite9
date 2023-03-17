package org.kite9.diagram.batik.text;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.batik.bridge.FontFace;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.gvt.font.AWTFontFamily;
import org.apache.batik.gvt.font.AWTGVTFont;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;

/**
 * The is a utility class that is used for resolving UnresolvedFontFamilies.
 *
 * @author <a href="mailto:bella.robinson@cmis.csiro.au">Bella Robinson</a>
 * @version $Id: DefaultFontFamilyResolver.java 1804130 2017-08-04 14:41:11Z ssteiner $
 */
public final class FixedDefaultFontFamilyResolver implements FontFamilyResolver {

    public static final Kite9FontFamilyResolver SINGLETON = new Kite9FontFamilyResolver();

    private FixedDefaultFontFamilyResolver() {
    }

    /**
     * The default font. This will be used when no font families can
     * be resolved for a particular text chunk/run.
     */
    private static final AWTFontFamily DEFAULT_FONT_FAMILY =
            new AWTFontFamily("SansSerif");

    /**
     * List of all available fonts on the current system, plus a few common
     * alternatives.
     */
    protected static final Map fonts = new HashMap();

    protected static final List awtFontFamilies = new ArrayList();

    protected static final List awtFonts = new ArrayList();

    /**
     * This sets up the list of available fonts.
     */
    static {
//        fonts.put("sans-serif",      "SansSerif");
//        fonts.put("serif",           "Serif");
//        fonts.put("times",           "Serif");
//        fonts.put("times new roman", "Serif");
//        fonts.put("cursive",         "Dialog");
//        fonts.put("fantasy",         "Symbol");
//        fonts.put("monospace",       "Monospaced");
//        fonts.put("monospaced",      "Monospaced");
//        fonts.put("courier",         "Monospaced");

        //
        // Load all fonts. Work around
        //

//        GraphicsEnvironment env;
//        env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        String[] fontNames = env.getAvailableFontFamilyNames();
//
//        int nFonts = fontNames != null ? fontNames.length : 0;
//        for(int i=0; i<nFonts; i++){
//            fonts.put(fontNames[i].toLowerCase(), fontNames[i]);
//
//            // also add the font name with the spaces removed
//            StringTokenizer st = new StringTokenizer(fontNames[i]);
//            String fontNameWithoutSpaces = "";
//            while (st.hasMoreTokens()) {
//                fontNameWithoutSpaces += st.nextToken();
//            }
//            fonts.put(fontNameWithoutSpaces.toLowerCase(), fontNames[i]);
//
//            // also add the font name with spaces replaced by dashes
//            String fontNameWithDashes = fontNames[i].replace(' ', '-');
//            if (!fontNameWithDashes.equals(fontNames[i])) {
//                fonts.put(fontNameWithDashes.toLowerCase(), fontNames[i]);
//            }
//        }
//
//        //Also register all font names, not just font families.
//        //Example: Font Family: "Univers", but Font Name: "Univers 45 Light"
//        //Without this, matching "Univers 45 Light" is not possible.
//        Font[] allFonts = env.getAllFonts();
//        for (Font f : allFonts) {
//            fonts.put(f.getFontName().toLowerCase(), f.getFontName());
//        }
//
//        // first add the default font
//        awtFontFamilies.add(DEFAULT_FONT_FAMILY);
//        awtFonts.add(new AWTGVTFont(DEFAULT_FONT_FAMILY.getFamilyName(), 0, 12));
//
//        Collection fontValues = fonts.values();
//        for (Object fontValue : fontValues) {
//            String fontFamily = (String) fontValue;
//            AWTFontFamily awtFontFamily = new AWTFontFamily(fontFamily);
//            awtFontFamilies.add(awtFontFamily);
//            AWTGVTFont font = new AWTGVTFont(fontFamily, 0, 12);
//            awtFonts.add(font);
//        }

    }

    /**
     * This keeps track of all the resolved font families. This is to hopefully
     * reduce the number of font family objects used.
     */
    protected static final Map resolvedFontFamilies = new HashMap();

    public AWTFontFamily resolve(String familyName, FontFace fontFace) {
        String fontName = (String)fonts.get(fontFace.getFamilyName().toLowerCase());
        if (fontName == null) {
            return null;
        } else {
            GVTFontFace face = FontFace.createFontFace(fontName, fontFace);
            return new AWTFontFamily(fontFace);
        }
    }

    public GVTFontFamily loadFont(InputStream in, FontFace ff) throws Exception {
    	String uniqueFontFamilyName = ff.getFamilyName()+" "+ff.getFontWeight()+" "+ff.getFontStyle();
//    	Font out = new Font(uniqueFontFamilyName, 0, 12);
    	
//        Map<TextAttribute, Object> fontDetails = new HashMap<>();
//        fontDetails.put(TextAttribute.FAMILY, ff.getFamilyName());
//        fontDetails.put(TextAttribute.WEIGHT, ff.getFontWeight());
// //       fontDetails.put(TextAttribute., fontDetails)
//        
//        Font font = Font.createFont(Font.TRUETYPE_FONT, in);
//        Font f= new Font(fontDetails);
//        
////    fontDetails.put(TextAttribute., fontDetails)
//    Font renamed = font.deriveFont(fontDetails);
//    return new AWTFontFamily(ff, renamed);
    	
    	// original version 
    	Font font = Font.createFont(Font.TRUETYPE_FONT, in);

    	// derive unique name
//    	Map<TextAttribute, Object> fontDetails = new HashMap<>();
//    	fontDetails.put(TextAttribute.FAMILY, uniqueFontFamilyName);
//    	font = font.deriveFont(fontDetails);

        return new AWTFontFamily(ff, font);
    }

    /** {@inheritDoc} */
    public GVTFontFamily resolve(String familyName) {

        familyName = familyName.toLowerCase();

        // first see if this font family has already been resolved
        GVTFontFamily resolvedFF =
                (GVTFontFamily)resolvedFontFamilies.get(familyName);

        if (resolvedFF == null) { // hasn't been resolved yet
            // try to find a matching family name in the list of
            // available fonts
            String awtFamilyName = (String)fonts.get(familyName);
            if (awtFamilyName != null) {
                resolvedFF = new AWTFontFamily(awtFamilyName);
            }

            resolvedFontFamilies.put(familyName, resolvedFF);
        }

        //  if (resolvedFF != null) {
        //      System.out.println("resolved " + fontFamily.getFamilyName() +
        //                         " to " + resolvedFF.getFamilyName());
        //  } else {
        //      System.out.println("could not resolve " +
        //                         fontFamily.getFamilyName());
        //  }
        return resolvedFF;
    }

    /** {@inheritDoc} */
    public GVTFontFamily getFamilyThatCanDisplay(char c) {
        for (int i = 0; i < awtFontFamilies.size(); i++) {
            AWTFontFamily fontFamily = (AWTFontFamily)awtFontFamilies.get(i);
            AWTGVTFont font = (AWTGVTFont)awtFonts.get(i);
            if (font.canDisplay(c) && fontFamily.getFamilyName().indexOf("Song") == -1) {
                // the awt font for "MS Song" doesn't display chinese glyphs correctly
                return fontFamily;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    public GVTFontFamily getDefault() {
        return DEFAULT_FONT_FAMILY;
    }

}
