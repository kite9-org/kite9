package org.kite9.diagram.batik.format;

import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.logging.Kite9Log;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class LoggingErrorListener implements ErrorListener, ErrorHandler {

    private final Kite9Log log;

    public LoggingErrorListener(Kite9Log log) {
        this.log = log;
    }

    @Override
    public void warning(TransformerException exception) {
        warning(new TranscoderException(exception));
    }

    @Override
    public void error(TransformerException exception) {
        error(new TranscoderException(exception));
    }

    @Override
    public void fatalError(TransformerException exception) {
        fatalError(new TranscoderException(exception));
    }

    @Override
    public void error(TranscoderException ex)  {
        throw new Kite9XMLProcessingException("Batik Issue", ex);
    }

    @Override
    public void fatalError(TranscoderException ex)  {
        throw new Kite9XMLProcessingException("Batik Issue", ex);
    }

    @Override
    public void warning(TranscoderException ex) {
        log.send(ex.getMessage());
    }
}
