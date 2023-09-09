package org.kite9.diagram.batik.format;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ConsolidatedErrorHandler implements
        ErrorListener,
        ErrorHandler,
        org.xml.sax.ErrorHandler,
        Logable {

    private final Kite9Log log;
    
    public ConsolidatedErrorHandler() {
    	this.log = Kite9Log.Companion.instance(this);
    }

    public static String convertLocator(SourceLocator sl) {
        return " [publicId: "+ sl.getPublicId() +
                ", systemId: "+sl.getSystemId()+
                ", lineNumber: "+ sl.getLineNumber()+
                ", columnNumber: "+sl.getColumnNumber()+"]";
    }

    public static String convertLocator(SAXParseException sl) {
        return " [publicId: "+ sl.getPublicId() +
                ", systemId: "+sl.getSystemId()+
                ", lineNumber: "+ sl.getLineNumber()+
                ", columnNumber: "+sl.getColumnNumber()+"]";
    }



    /* Error Listener */
    @Override
    public void warning(TransformerException exception) throws TransformerException {
        log.send("Tranform Warning"+exception.getMessage()+convertLocator(exception.getLocator()));
    }

    @Override
    public void warning(TranscoderException ex) throws TranscoderException {
        log.send("Batik Warning: "+ ex.getMessage());
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        log.error("SAX Warning"+convertLocator(exception), exception);
    }




    @Override
    public void error(TransformerException exception) throws TransformerException {
        log.error("Transform error", exception);
        wrapTransformerException(exception);
    }

    private void wrapTransformerException(TransformerException exception) {
        if (exception.getLocator() instanceof Node) {
            throw new Kite9XMLProcessingException("Transform Exception"+convertLocator(exception.getLocator()), exception, (Node) exception.getLocator());
        } else {
            throw new Kite9XMLProcessingException("Transform Exception"+convertLocator(exception.getLocator()), exception);
        }
    }

    @Override
    public void error(TranscoderException ex) throws TranscoderException{
        log.error("Batik error", ex);
        throw ex;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        log.error("SAX Error"+convertLocator(exception), exception);
        throw exception;
    }





    @Override
    public void fatalError(TransformerException exception) {
        log.error("Transform fatal", exception);
        wrapTransformerException(exception);
    }

    @Override
    public void fatalError(TranscoderException ex) throws TranscoderException {
        log.error("Batik Fatal", ex);
        throw ex;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        log.send("SAX Fatal.  Can't parse "+ exception.getMessage()+convertLocator(exception));

        throw exception;
    }

	@Override
	public String getPrefix() {
		return "XML ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
}
