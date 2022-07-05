package com.kite9.server.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.bridge.BridgeException;
import org.apache.batik.dom.util.SAXIOException;
import org.apache.batik.transcoder.TranscoderException;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.NestedServletException;
import org.xml.sax.SAXParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.security.LoginRequiredException;
import com.kite9.server.security.LoginRequiredException.Type;

@Controller
public class Kite9ErrorController implements ErrorController {
	
	public static final String HIGHLIGHTER = 
			"<link rel=\"stylesheet\"  href=\"/webjars/highlightjs/9.6.0/styles/github.css\">\n" + 
			"<script src=\"/webjars/highlightjs/9.6.0/highlight.min.js\"></script>\n" +
			"<script src=\"/webjars/highlightjs/9.6.0/languages/xml.min.js\"></script>";;

	public static final String BOOTSTRAP = "<link href=\"/webjars/bootstrap/4.3.1/css/bootstrap.min.css\" rel=\"stylesheet\" />";
	
	@RequestMapping(path = "/error", produces = MediaType.TEXT_HTML_VALUE )
	public ResponseEntity<String> handleError(HttpServletRequest request) {
		ResponseEntity<String> lr = checkLoginRequired(request);
		if (lr != null) {
			return lr;
		}
	
		Integer statusCode = getStatusCode(request);
		String uri = getRequestURI(request);
		Exception exception = getException(request);
		
		StringBuilder sb = new StringBuilder();
		addHeader(sb, statusCode, uri);
		processException(sb, exception, 1);
		addFooter(sb);
		return new ResponseEntity<String>(sb.toString(), HttpStatus.resolve(statusCode));
	}
	
	private ResponseEntity<String> checkLoginRequired(HttpServletRequest request) {
		Throwable exception = getException(request);
		while (exception instanceof NestedServletException) {
			exception = ((NestedServletException)exception).getCause();
		}
		if (exception instanceof LoginRequiredException) {
			LoginRequiredException lre = (LoginRequiredException) exception;
			Type t = lre.getType();
			K9URI redirect = lre.getRedirectUri(); 
			HttpHeaders headers = new HttpHeaders();
			headers.add("Location", t.path+"?state=bllabh");    
			return new ResponseEntity<String>(headers,HttpStatus.FOUND);	
			
		}
		
		return null;
	}


	private Exception getException(HttpServletRequest request) {
		return (Exception) request.getAttribute("javax.servlet.error.exception");
	}

	private String getRequestURI(HttpServletRequest request) {
		return (String) request.getAttribute("javax.servlet.error.request_uri").toString();
	}

	private Integer getStatusCode(HttpServletRequest request) {
		return (Integer) request.getAttribute("javax.servlet.error.status_code");
	}
	
	/**
	 * Non-html error handler.  Returns JSON irrespective of the media type.
	 */
	@RequestMapping(path = "/error", produces = { MediaType.ALL_VALUE })
	public void handleErrorJson(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> out = new HashMap<String, Object>();
		Integer statusCode = getStatusCode(request);
		out.put("status", ""+statusCode);
		String uri = getRequestURI(request);
		out.put("uri", uri);
		response.setStatus(statusCode);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		
		Exception exception = getException(request);
		if (exception != null) {
			processException(out, exception, 0);
		}
		
		try {
			String outStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(out);
			response.getWriter().write(outStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addFooter(StringBuilder sb) {
		sb.append("<script>hljs.initHighlightingOnLoad();</script>");
		sb.append("</div></body></html>");
	}


	private void processException(Map<String, Object> out, Throwable e, int level) {
		Throwable next = null;
		if (e != null) {
			if (e instanceof Kite9XMLProcessingException) {
				String css = ((Kite9XMLProcessingException) e).getCss();
				String ctx = ((Kite9XMLProcessingException) e).getContext();
				String doc = ((Kite9XMLProcessingException) e).getComplete();
				out.put(""+level, doJsonBasedException(e, css, ctx, doc));
				next = e.getCause();
			} else if (e instanceof BridgeException) {
				BridgeException be = (BridgeException) e;
				String ctx = Kite9XMLProcessingException.toString(be.getElement());
				String css = Kite9XMLProcessingException.debugCss(be.getElement());
				String doc = Kite9XMLProcessingException.toString(be.getElement().getOwnerDocument());
				out.put(""+level, doJsonBasedException(e, css, ctx, doc));
				next = e.getCause();
			} else if (e instanceof SAXParseException) {
				SAXParseException pe = (SAXParseException) e;
				String publicId = pe.getPublicId();
				String systemId = pe.getSystemId();
				int lineNumber = pe.getLineNumber();
				int columnNumber = pe.getColumnNumber();
				String saxInfo = String.format("publicId: %s\nsystemId: %s\nlineNumber: %s\ncolumnNumber: %s", publicId, systemId, lineNumber, columnNumber);
				out.put(""+level, doJsonBasedException(e, null,saxInfo,null));
				next = pe.getException();
			} else if (e instanceof SAXIOException) {
				SAXIOException pe = (SAXIOException) e;
				out.put(""+level, doJsonBasedException(e, null,null,null));
				next = pe.getSAXException();
			} else if (e instanceof TranscoderException) {
				out.put(""+level, doJsonBasedException(e, null,null,null));
				next = ((TranscoderException) e).getException();
			} else if (e.getCause() == null) {
				out.put(""+level, doJsonBasedException(e, null,null,null));
				next = null;
			} else {
				next = e.getCause();
			}

		}
		
		if (next != null) {
			processException(out, next, level+1);
		}
	}	
	
	
	private void processException(StringBuilder sb, Throwable e, int level) {
		Throwable next = null;
		if (e != null) {
			sb.append("<div class=\"card\" style=\"margin-bottom: 30pt;\"><div class=\"card-body\"><h4 class=\"card-title\">");
			sb.append("("+level+") ");
			sb.append(e.getClass().getName());
			sb.append("</h4>");
			sb.append("<h6 class=\"card-subtitle mb-2 text-muted\">");
			sb.append(formatExceptionMessage(e));
			sb.append("<div class=\"card-text\" style=\"padding: 20p; \">");
			
			if (e instanceof Kite9XMLProcessingException) {
				String css = ((Kite9XMLProcessingException) e).getCss();
				String ctx = ((Kite9XMLProcessingException) e).getContext();
				String doc = ((Kite9XMLProcessingException) e).getComplete();
				doXMLBasedException(sb, e, css, ctx, doc);
				next = e.getCause();
			} else if (e instanceof BridgeException) {
				BridgeException be = (BridgeException) e;
				String ctx = Kite9XMLProcessingException.toString(be.getElement());
				String css = Kite9XMLProcessingException.debugCss(be.getElement());
				String doc = Kite9XMLProcessingException.toString(be.getElement().getOwnerDocument());
				
				doXMLBasedException(sb, e, css, ctx, doc);
				next = e.getCause();
			} else if (e instanceof SAXParseException) {
				SAXParseException pe = (SAXParseException) e;
				String publicId = pe.getPublicId();
				String systemId = pe.getSystemId();
				int lineNumber = pe.getLineNumber();
				int columnNumber = pe.getColumnNumber();
				String saxInfo = String.format("publicId: %s\nsystemId: %s\nlineNumber: %s\ncolumnNumber: %s", publicId, systemId, lineNumber, columnNumber);
				doCard(sb, saxInfo, "details", "plaintext");
				next = pe.getException();
			} else if (e instanceof SAXIOException) {
				SAXIOException pe = (SAXIOException) e;
				doStackTraceCard(sb, pe);
				next = pe.getSAXException();
			} else if (e instanceof TranscoderException) {
				doStackTraceCard(sb, e);
				next = ((TranscoderException) e).getException();
			} else if (e.getCause() == null) {
				doStackTraceCard(sb, e);
				next = null;
			} else {
				next = e.getCause();
			}

			sb.append("</div></div></div>");
		}
		
		if (next != null) {
			processException(sb, next, level+1);
		}
	}

	protected String formatExceptionMessage(Throwable e) {
		String msg = e.getMessage();
		msg = msg == null ? "" : msg;
		return "<ul><li>"+msg.replaceAll("\n", "</li><li>")+"</li></ul>";
	}
	
	protected Map<String, Object> doJsonBasedException(Throwable e, String css, String ctx, String doc) {
		Map<String, Object> out = new HashMap<String, Object>();
		
		if (StringUtils.hasText(ctx)) {
			out.put("fragment", ctx);
		}
		
		if (StringUtils.hasText(css)) {
			out.put("style", css);
		}

		if (StringUtils.hasText(doc)) {
			out.put("doc", doc);
			
		}
		
		if (e != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw); 
			out.put("trace", sw.toString());
			out.put("message", e.getMessage());
		}
		
		
		
		return out;
	}

	protected void doXMLBasedException(StringBuilder sb, Throwable e, String css, String ctx, String doc) {
		if (StringUtils.hasText(ctx)) {
			doCard(sb, ctx, "fragment", "xml");
		}
		
		if (StringUtils.hasText(css)) {
			doCard(sb, css, "style", "css");
			
		}

		if (StringUtils.hasText(doc)) {
			doCard(sb, doc, "document", "doc");
			
		}
		
		doStackTraceCard(sb, e);
	}

	protected void doStackTraceCard(StringBuilder sb, Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw); 
		doCard(sb, sw.toString(), "Stack Trace", "plaintext");
	}

	protected void doCard(StringBuilder sb, String ctx, String title, String format) {
		sb.append("<div class=\"card\" style=\"margin-bottom: 30pt;\"><div class=\"card-body\">");
		sb.append("<h5 class=\"card-title\">"+title+"</h5>");
		sb.append("<div class=\"card-text\"><pre><code class=\""+format+"\">");
		sb.append(HtmlUtils.htmlEscape(ctx));
		sb.append("</code></pre></div></div></div>");
	}

	private void addHeader(StringBuilder sb, Integer statusCode, String uri) {
		sb.append("<html><head><title>"+statusCode+"</title>");
		sb.append(HIGHLIGHTER);
		sb.append(BOOTSTRAP);
		sb.append("</head><body><div class=\"container\">");
		sb.append("<h1>Kite9 Error <span class=\"badge badge-danger\">"+statusCode+"</span></h1>");
		sb.append("<div class=\"alert alert-info\">" + uri+ "</div>");
	}
//
//	@Override
//	public String getErrorPath() {
//		return "/error";
//	}

}
