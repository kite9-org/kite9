package org.kite9.diagram.batik.format;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.dom.util.SAXIOException;
import org.apache.batik.ext.awt.image.codec.png.PNGEncodeParam;
import org.apache.batik.ext.awt.image.codec.png.PNGImageEncoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

/**
 * The main purpose of this class is to embed some ADL actually within the PNG, so that someone can come along and edit it later, 
 * if they extract the ADL.
 * 
 * @author robmoffat
 *
 */
public class ADLEmbeddingPNGTranscoder extends PNGTranscoder {
	
	private final Kite9DocumentLoader docLoader;
	private final String adlToEmbed;

	public ADLEmbeddingPNGTranscoder(Kite9DocumentLoader docLoader, String adlToEmbed) {
		this.docLoader = docLoader;
		this.adlToEmbed = adlToEmbed;
	}

	@Override
	protected BridgeContext createBridgeContext(String version) {
		return new BridgeContext(userAgent, docLoader) {
			
			/**
		     * This is a duplicate of the original method, but 
		     * contains better error-handling, so we don't swallow the exception.
		     * Also handles references embedded in the css
		     */
		    public Node getReferencedNode(Element e, String uri) {
		        SVGDocument document = (SVGDocument)e.getOwnerDocument();
		        URIResolver ur = createURIResolver(document, documentLoader);
		        Node ref;
		        
		        try {
		            ref = ur.getNode(uri, e);
		            if (ref == null) {
		                throw new BridgeException(this, e, ERR_URI_BAD_TARGET,
		                                          new Object[] {uri});
		            } else {
		                SVGOMDocument refDoc =
		                    (SVGOMDocument) (ref.getNodeType() == Node.DOCUMENT_NODE
		                                       ? ref
		                                       : ref.getOwnerDocument());
		                // This is new rather than attaching this BridgeContext
		                // with the new document we now create a whole new
		                // BridgeContext to go with the new document.
		                // This means that the new document has it's own
		                // world of stuff and it should avoid memory leaks
		                // since the new document isn't 'tied into' this
		                // bridge context.
		                if (refDoc != document) {
		                    createSubBridgeContext(refDoc);
		                }
		                return ref;
		            }
		        } catch (SAXIOException ex) {
		        	// to be consistent with safari and chome, if an element reference is on a stylesheet, then 
		        	// we should take the reference from the current document.
		        	try {
		        		ParsedURL pUrl = new ParsedURL(uri);
		        		String fragment = pUrl.getRef();
		        		ref = ur.getNode("#" + fragment, e);
		        		return ref;
		        	} catch (Exception ex2) {
		        		// throw the original exception
		        		throw new Kite9ProcessingException("Problem with getting URL:"+uri, ex);
		        	}
		        } catch (Exception ex) {
		            throw new Kite9ProcessingException("Problem with getting URL:"+uri, ex);
		        }
		    }

			
		};
	}

	@Override
	public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException {
		WriteAdapter adapter =  new  PNGTranscoder.WriteAdapter() {

			@Override
			public void writeImage(PNGTranscoder transcoder, BufferedImage img, TranscoderOutput output)
					throws TranscoderException {
				PNGEncodeParam params = PNGEncodeParam.getDefaultEncodeParam(img);
				if (params instanceof PNGEncodeParam.RGB) {
		            ((PNGEncodeParam.RGB)params).setBackgroundRGB
		                (new int [] { 255, 255, 255 });
		        }
				params.setSRGBIntent(PNGEncodeParam.INTENT_PERCEPTUAL);
				params.setText(new String[] { "kite-adl", adlToEmbed });
				
				float PixSzMM = transcoder.getUserAgent().getPixelUnitToMillimeter();
		        // num Pixs in 1 Meter
		        int numPix      = (int)((1000/PixSzMM)+0.5);
		        params.setPhysicalDimension(numPix, numPix, 1); // 1 means 'pix/meter'

		        try {
		            OutputStream ostream = output.getOutputStream();
		            PNGImageEncoder pngEncoder = new PNGImageEncoder(ostream, params);
		            pngEncoder.encode(img);
		            ostream.flush();
		        } catch (IOException ex) {
		            throw new TranscoderException(ex);
		        }
			}
			
			
			
		};
	    adapter.writeImage(this, img, output);
	}
}