package org.kite9.diagram.visualization.format.pdf;

import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.format.AbstractScalingGraphicsSourceRenderer;
import org.kite9.framework.logging.LogicException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfTransparencyGroup;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFRenderer extends AbstractScalingGraphicsSourceRenderer<byte[]> {
    
    public PDFRenderer(Integer width, Integer height) {
		super(width, height);
	}

	public PDFRenderer() {
		this(null, null);
    }

    Document doc;
    ByteArrayOutputStream baos;
    PdfContentByte cb;
    SortedMap<Float, PdfTemplate> layers;
    Map<Float, Graphics2D> layerGraphics;
    Dimension2D size;
    
	public Graphics2D getGraphics(int layer, float transparency, float scale, Dimension2D imageSize, Dimension2D diagramSize) {
		if ((this.size == null) || (!this.size.equals(imageSize))) {
			initialize(imageSize);
			this.size = imageSize;
		}
		
		Graphics2D graphics2d = layerGraphics.get(transparency);
		if (graphics2d!=null) {
			return graphics2d;
		}
		
		PdfTemplate tp = cb.createTemplate((float) imageSize.x(), (float) imageSize.y());
		PdfTransparencyGroup group = new PdfTransparencyGroup();
		tp.setGroup(group);
	    graphics2d = tp.createGraphics((float) imageSize.x(), (float) imageSize.y());
	    applyScaleAndTranslate(graphics2d, scale, imageSize, diagramSize);
		layerGraphics.put(transparency, graphics2d);
		setRenderingHints(graphics2d);
		layers.put(transparency, tp);
		return graphics2d;
	}

	private void initialize(Dimension2D size) {
		try {
			float width = (float) size.getWidth();
			float height = (float) size.getHeight();
			Rectangle rect = new Rectangle(width, height);
			doc = new Document(rect);
			baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(doc, baos);
			doc.open();
			cb = writer.getDirectContent();
			layerGraphics = new HashMap<Float, Graphics2D>();
			layers = new TreeMap<Float, PdfTemplate>();
		} catch (DocumentException e) {
			throw new LogicException("Could not create PDF in memory", e);
		}
		
	}

	public byte[] render(Diagram d)  {
		try {
			Dimension2D out = size(d);
			dea.initialize(this, out);
			drawDiagramElements(d);
			dea.finish();
			
			for (Graphics2D g2 : layerGraphics.values()) {
				g2.dispose();
			}
			
			for (PdfTemplate pt : layers.values()) {
				cb.addTemplate(pt, 0,0);
			}
			
			doc.close();
			baos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new LogicException("Could not complete render to PDF: ", e);
		}
	}
	

}