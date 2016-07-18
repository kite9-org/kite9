package org.kite9.diagram.visualization.display.style.io;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.visualization.display.style.LocalFont;
import org.kite9.framework.logging.LogicException;

/**
 * Contains logic to load fonts directly from the project, rather than use system fonts.
 * This is so the look and feel is portable across platforms.
 *
 * @author robmoffat
 *
 */
public abstract class FontHelper {

	private static Map<String, LocalFont> fontMap = new HashMap<String, LocalFont>();
	
	public static LocalFont getFont(String name, int i) {
		String key = name+":"+i;
		LocalFont out = fontMap.get(key);
		if (out==null) {
			if (i!=1) {
				LocalFont f = getFont(name, 1);
				out = new LocalFont(f.deriveFont((float) i), f.getFontFileName());
			} else {
				try {
					InputStream is = getFontStream(name);
					out =  new LocalFont(Font.createFont(Font.TRUETYPE_FONT, is), "/fonts/"+name+".ttf");
					is.close();
				} catch (FontFormatException e) {
					throw new LogicException("Couldn't source font "+name, e);
				} catch (IOException e) {
					throw new LogicException("Couldn't source font "+name, e);
				}
			}
			fontMap.put(key, out);
		}
		
		return out;
	}

	public FontHelper() {
		super();
	}

	public static InputStream getFontStream(String name) {
		String file = "/fonts/"+name+".TTF";
		InputStream is = FontHelper.class.getResourceAsStream(file);
		if (is==null) {
			file =  "/fonts/"+name+".ttf";
			is = FontHelper.class.getResourceAsStream(file);
		}
		
		return is;
	}

	protected abstract Map<String, LocalFont> getFontsInternal();
	
	

}