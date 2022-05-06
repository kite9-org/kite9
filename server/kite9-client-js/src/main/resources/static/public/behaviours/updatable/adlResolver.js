import { ensureJs, once } from '/public/bundles/ensure.js';

export function createAdlToSVGResolver(transition, metadata) {

	const XSL_TEMPLATE_NAMESPACE = "http://www.kite9.org/schema/xslt";
	const ADL_NAMESPACE = "http://www.kite9.org/schema/adl";
	const DEFAULT_TEMPLATE = "/public/templates/basic/basic-template.xsl";

	var transformer = null;	  // set on first use
	var template = null;
	
	/**
	 * Replace with ES6 module as soon as possible
	 */
	once(function() {
		ensureJs('/webjars/kotlin/1.4.30/kotlin.js');	
		setTimeout(() => {
			ensureJs('/webjars/kite9-visualization-js/0.1-SNAPSHOT/kite9-visualization-js.js');
			setTimeout(() => window['kite9-visualization-js'].initCSS(), 100);
		}, 20)

		// not using this since we run into licensing issues.
		//ensureJs('/public/external/SaxonJS2.js');
	});
	
	/** 
	 * Makes sure we have somewhere in the document to do the Kite9 layout
	 */
	function ensureUpdateArea() {
		// this is where changes go for layout	
		var update = document.getElementById("_update")
		if (update == undefined) {
		 	update = document.createElement("div");
			update.setAttribute("id", "_update");
			document.body.appendChild(update);
		}
				
		return update;
	}
	
	/**
	 * Resolves the URL for the template, contained in the template attribute of the doc.
	 */
	function getTemplateUri(doc) {
	
		const template = doc.documentElement.getAttributeNS(XSL_TEMPLATE_NAMESPACE, "template");
		
		if ((template == null) || (template.length == 0)) {
			if (ADL_NAMESPACE == doc.documentElement.namespaceURI) {
				// default to the basic template
				return new URL(DEFAULT_TEMPLATE, document.location.href).href;
			}
		}
		
		return new URL(template, document.location.href).href;
	}
	
	function layoutSVGDocument(result, doc) {
		// we have to add the result to the main dom 
		// to get the computedStyleMap and format
		const update = ensureUpdateArea();
		update.replaceChildren();
		const docElement = result.documentElement
		update.appendChild(docElement)
		window['kite9-visualization-js'].formatSVG(docElement);
		
		// put it back in the result 
		result.appendChild(docElement);
		update.replaceChildren();
		
		transition.change(result);
		metadata.update(doc);
	}

	return (text) => {
		const parser = new DOMParser();
		const doc = parser.parseFromString(text, "text/xml");
		const currentTemplate = getTemplateUri(doc);
		
		if (currentTemplate != template) {
			var xhr = new XMLHttpRequest;
			xhr.open('GET', currentTemplate);
			xhr.responseType = 'document';
			xhr.overrideMimeType('text/xml');
			xhr.onload = function () {
			  	if (xhr.readyState === xhr.DONE && xhr.status === 200) {
					transformer = new XSLTProcessor();
					template = currentTemplate;
					transformer.importStylesheet(xhr.responseXML);	
					const result = transformer.transformToDocument(doc);
					layoutSVGDocument(result, doc)
					
				} else {
					console.error("Couldn't transform'");
				}
			};
				
			xhr.send();		
		} else {
			return layoutSVGDocument(transformer.transformToDocument(doc), doc);
		}
  	}
	
} 