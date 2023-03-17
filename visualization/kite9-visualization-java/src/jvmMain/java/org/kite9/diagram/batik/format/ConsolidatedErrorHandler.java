package org.kite9.diagram.batik.format;

import javax.xml.transform.ErrorListener;
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

    public ConsolidatedErrorHandler(Kite9Log log) {
        this.log = log;
    }

    /* Error Listener */
    @Override
    public void warning(TransformerException exception) throws TransformerException {
        log.send("Tranform Warning"+exception.getMessage());
    }

    @Override
    public void warning(TranscoderException ex) throws TranscoderException {
        log.send("Batik Warning: "+ ex.getMessage());
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        log.error("SAX Warning", exception);
    }




    @Override
    public void error(TransformerException exception) throws TransformerException {
        log.error("Transform error", exception);
        wrapTransformerException(exception);
    }

    private void wrapTransformerException(TransformerException exception) {
        if (exception.getLocator() instanceof Node) {
            throw new Kite9XMLProcessingException("Transform Exception", exception, (Node) exception.getLocator());
        } else {
            throw new Kite9XMLProcessingException("Transform Exception", exception);
        }
    }

    @Override
    public void error(TranscoderException ex) throws TranscoderException{
        log.error("Batik error", ex);
        throw ex;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        log.error("SAX Error", exception);
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
        if (isActuallyASaxProblem(exception)) {
            log.error("SAX Fatal", exception);
        }

        throw exception;
    }

    /**
     * Don't report sax error when batik is trying to parse CSS
     */
    private boolean isActuallyASaxProblem(SAXParseException exception) {
        if (exception.getSystemId() == null) {
            return true;
        }

        return !exception.getSystemId().endsWith(".css");
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
