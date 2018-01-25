package org.kite9.diagram.batik.format;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.batik.bridge.FontFace;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.ParsedURL;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * When we are constructing an SVG using a Graphics2D object, this
 * class allows us to add extra details in the XML that otherwise wouldn't be
 * part of the SVG.
 * 
 * @author robmoffat
 *
 */
public class ExtendedSVGGraphics2D extends SVGGraphics2D implements ExtendedSVG, Logable {

	private Element currentSubgroup;
	private Kite9Log log = new Kite9Log(this);
	private List<GVTFontFace> existingFontFaces = new ArrayList<>();
	private StringBuilder styleInfo = new StringBuilder(1000);
	private Rectangle2D textBounds = new Rectangle2D.Float(0, 0, 0, 0);

	public ExtendedSVGGraphics2D(ExtendedSVGGeneratorContext ctx, Element currentSubgroup) {
		super(ctx, false);
		this.currentSubgroup = currentSubgroup;
		getDOMTreeManager().setTopLevelGroup(currentSubgroup);
		clearUnsupportedAttributes();
	}
	
	public ExtendedSVGGraphics2D(Document doc) {
		super(ExtendedSVGGeneratorContext.buildSVGGeneratorContext(doc) ,false);
		this.currentSubgroup = getTopLevelGroup();
		clearUnsupportedAttributes();
	}
	
	private void clearUnsupportedAttributes() {
		// since we are converting from svg to svg, there should be no unsupported attributes
		setUnsupportedAttributes(Collections.emptySet());
	}
	
	@Override
	public void createGroup(String id) {
		if (id != null) {
			Element newGroup = currentSubgroup.getOwnerDocument().createElement("g");
			currentSubgroup.appendChild(newGroup);
			newGroup.setAttribute("id", id);
			this.currentSubgroup = newGroup;
			setTopLevelGroup(newGroup);
			log.send("Started. Current group: "+this.currentSubgroup.getAttribute("id"));
		}
	}
	
	public void finishGroup(String id) {
		if (id != null) {
			if (!id.equals(currentSubgroup.getAttribute("id"))) {
				throw new Kite9ProcessingException("Was expecting current group with id: "+id);
			}
			
			Element parent = (Element) currentSubgroup.getParentNode();
			setTopLevelGroup(parent);
			this.currentSubgroup = parent;
			log.send("Finished.  Current group: "+this.currentSubgroup.getAttribute("id"));
		}
	}

	@Override
	public String getPrefix() {
		return "GRUP";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
	static class PlaceholderFont extends Font {
		
		
		private String family;
		
		public PlaceholderFont(Map<? extends Attribute, ?> attributes, String family) {
			super(attributes);
			this.family = family;
		}

		@Override
		public String getFamily() {
			return family;
		}

		@Override
		public Font deriveFont(Map<? extends Attribute, ?> attributes) {
		    return new PlaceholderFont(attributes, family);
		}
			
			
	}

	/**
	 * By the time we get here, every remaining font family is valid, so
	 * all we have to do is ensure that the font has been added to the output document 
	 * in the correct place.
	 */
	@Override
	public Font handleGVTFontFamilies(List<GVTFontFamily> families) {
		for (GVTFontFamily ff : families) {
			
			if (ff.getFontFace() != null) {
				//addFontFace(ff.getFontFace());
				return new PlaceholderFont(null, ff.getFamilyName());
			}
			
		}
		
		return null;
	}

	/**
	 * Adds a <style> tag inside <defs> which contains the new @font-face definition.
	 * We use this in preference to svg <font-face>, which is now deprecated, and
	 * also much more confusing.
	 */
	public void addFontFace(GVTFontFace ff) {
		for (GVTFontFace existing : existingFontFaces) {
			if (existing.getFamilyName().equals(ff.getFamilyName())) {
				return;   // already present
			}
		}
		
		styleInfo.append("@font-face {\n");

		addAttributeIfPresent(ff.getFamilyName(), "font-family", styleInfo);
		// variant?
		// stretch?
		addAttributeIfPresent(ff.getFontWeight(), "font-weight", styleInfo);
		addAttributeIfPresent(ff.getFontStyle(), "font-style", styleInfo);
		
		if (ff instanceof FontFace) {
			List<ParsedURL> sources = getFontSourcees((FontFace) ff);
			styleInfo.append("  src: ");
			boolean first = true;
			for (ParsedURL parsedURL : sources) {
				if (!first) {
					styleInfo.append(",\n      ");
				}
				styleInfo.append("url('");
//				styleInfo.append(this.getResourceReferencer().getReference(parsedURL).getUrl());
				styleInfo.append("');");
				first = false;
			}
			styleInfo.append(";\n");
		} 
		
		styleInfo.append("}\n");
		
		existingFontFaces.add(ff);
	}

	private static final Field SOURCES; 
	
	static {
		try {
			SOURCES = FontFace.class.getDeclaredField("srcs");
		} catch (Exception e) {
			throw new Kite9ProcessingException(e);
		}
		SOURCES.setAccessible(true);
	}

	@SuppressWarnings("unchecked")
	private List<ParsedURL> getFontSourcees(FontFace ff) {
		try {
			return (List<ParsedURL>) SOURCES.get(ff);
		} catch (Exception e) {
			throw new Kite9ProcessingException(e);
		}
	}

	/**
	 * Adds custom styling to the SVG document.
	 */
	@Override
	public Element getRoot(Element svgRoot) {
		Element root = super.getRoot(svgRoot);
		if (styleInfo.length() > 0) {
			Element styles = getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_STYLE_TAG);
			root.appendChild(styles);
			styles.setTextContent(styleInfo.toString());
		}
		
		return root;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private void addAttributeIfPresent(String value, String attribute, StringBuilder sb) {
		if ((value != null) && (value.length() > 0)) {
			sb.append("  ");
			sb.append(attribute);
			sb.append(": ");
			sb.append(value);
			sb.append(";\n");
		}
	}

	@Override
	public Rectangle2D getTextBounds() {
		return textBounds;
	}

	@Override
	public void setTextBounds(Rectangle2D r) {
		this.textBounds = r;
	}

}
