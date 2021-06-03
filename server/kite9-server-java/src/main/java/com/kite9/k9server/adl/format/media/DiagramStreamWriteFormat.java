package com.kite9.k9server.adl.format.media;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import org.kite9.diagram.format.Kite9Transcoder;

import java.io.OutputStream;

public interface DiagramStreamWriteFormat<T>  extends DiagramWriteFormat<T> {

    public void handleWrite(ADLDom toWrite, OutputStream baos, Kite9Transcoder<T> t);


}
