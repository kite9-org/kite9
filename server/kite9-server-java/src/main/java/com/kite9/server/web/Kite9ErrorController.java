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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import org.xml.sax.SAXParseException;

@Controller
public class Kite9ErrorController implements ErrorController {
	
	public static final String HIGHLIGHTER = 
			"<link rel=\"stylesheet\"  href=\"/webjars/highlightjs/9.6.0/styles/github.css\">\n" + 
			"<script src=\"/webjars/highlightjs/9.6.0/highlight.min.js\"></script>\n" +
			"<script src=\"/webjars/highlightjs/9.6.0/languages/xml.min.js\"></script>";;

	public static final String BOOTSTRAP = "<link href=\"/webjars/bootstrap/4.3.1/css/bootstrap.min.css\" rel=\"stylesheet\" />";
	
	@RequestMapping(path = "/error", produces = MediaType.TEXT_HTML_VALUE )
	@ResponseBody
	public String handleError(HttpServletRequest request) {
		Integer statusCode = getStatusCode(request);
		String uri = getRequestURI(request);
		Exception exception = getException(request);
		
		StringBuilder sb = new StringBuilder();
		addHeader(sb, statusCode, uri);
		processException(sb, exception, 1);
		addFooter(sb);
		return sb.toString();
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
	@ResponseBody
	public Map<String, String> handleErrorJson(HttpServletRequest request, HttpServletResponse response) {
		Integer statusCode = getStatusCode(request);
		String uri = getRequestURI(request);
		Exception exception = getException(request);
		Map<String, String> out = new HashMap<String, String>();
		out.put("status", ""+statusCode);
		response.setStatus(statusCode);
		if (exception != null) {
			out.put("message", exception.getMessage());
			StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			out.put("trace", sw.toString());
		}
		
		out.put("uri", uri);
		return out;
	}

	private void addFooter(StringBuilder sb) {
		sb.append("<script>hljs.initHighlightingOnLoad();</script>");
		sb.append("</div></body></html>");
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
			next = e.getCause();
		}
		if (e instanceof Kite9XMLProcessingException) {
			String css = ((Kite9XMLProcessingException) e).getCss();
			String ctx = ((Kite9XMLProcessingException) e).getContext();
			doXMLBasedException(sb, e, css, ctx);
			next = e.getCause();
		} else if (e instanceof BridgeException) {
			BridgeException be = (BridgeException) e;
			String ctx = Kite9XMLProcessingException.toString(be.getElement());
			String css = Kite9XMLProcessingException.debugCss(be.getElement());
			doXMLBasedException(sb, e, css, ctx);
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
		} else if ((e!=null) && (e.getCause() == null)) {
			doStackTraceCard(sb, e);
			next = null;
		}
		
		if (e != null) {
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

	protected void doXMLBasedException(StringBuilder sb, Throwable e, String css, String ctx) {
		if (StringUtils.hasText(ctx)) {
			doCard(sb, ctx, "fragment", "xml");
		}
		
		if (StringUtils.hasText(css)) {
			doCard(sb, css, "style", "css");
			
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

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
