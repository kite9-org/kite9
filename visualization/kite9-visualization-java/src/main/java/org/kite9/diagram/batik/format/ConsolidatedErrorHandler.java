package org.kite9.diagram.batik.format;

import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.kite9.diagram.logging.Kite9Log;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class ConsolidatedErrorHandler implements
        ErrorListener,
        ErrorHandler,
        org.xml.sax.ErrorHandler {

    private final Kite9Log log;

    public ConsolidatedErrorHandler(Kite9Log log) {
        this.log = log;
    }

    /* Error Listener */
    @Override
    public void warning(TransformerException exception) throws TransformerException {
        log.send("Tranform Warning"+exception.getMessage());
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
        log.error("Transform error", exception);
        throw exception;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
        log.error("Transform fatal", exception);
        throw exception;
    }

    /* Batik Error Handler */
    @Override
    public void error(TranscoderException ex) throws TranscoderException{
        log.error("Batik error", ex);
        throw ex;
    }

    @Override
    public void fatalError(TranscoderException ex) throws TranscoderException {
        log.error("Batik Fatal", ex);
        throw ex;
    }

    @Override
    public void warning(TranscoderException ex) throws TranscoderException {
        log.send("Transform Warning: "+ ex.getMessage());
    }

    /* SAX Error Handler */
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        log.error("SAX Warning", exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        log.error("SAX Error", exception);
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        log.error("SAX Fatal", exception);
        throw exception;
    }
}
