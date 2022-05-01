import { ensureJs, once } from '/public/bundles/ensure.js';

export function createAdlToSVGResolver() {

	const XSL_TEMPLATE_NAMESPACE = "http://www.kite9.org/schema/xslt";
	const ADL_NAMESPACE = "http://www.kite9.org/schema/adl";

	var transformer = null;	  // set on first use
	
	/**
	 * Replace with ES6 module as soon as possible
	 */
	once(function() {
		ensureJs('/webjars/kotlin/1.4.30/kotlin.js');	
		setTimeout(() => {
			ensureJs('/webjars/kite9-visualization-js/0.1-SNAPSHOT/kite9-visualization-js.js');
			window['kite9-visualization-js'].initCSS();
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
		
		if ((template == null) || (template.length() == 0)) {
			if (ADL_NAMESPACE == doc.documentElement.namespaceURI) {
				// default to the basic template
				return new URL("/public/templates/basic/basic-template.xsl", document.location.href).href;
			}
		}
		
		return new URL(template, document.location.href).href;
	}

	(doc) => {
		if (transformer == null) {
			// using this to make sure document location is correct.
			var xhr = new XMLHttpRequest;
			xhr.open('GET', getTemplateUri(doc));
			xhr.responseType = 'document';
			xhr.overrideMimeType('text/xml');
			xhr.onload = function () {
			  	if (xhr.readyState === xhr.DONE && xhr.status === 200) {
					this.transformer = new XSLTProcessor();
					this.transformer.importStylesheet(xhr.responseXML);	
					const result = this.transformer.transformToDocument(doc);
					
					// we have to add the result to the main dom 
					// to get the computedStyleMap and format
					const update = ensureUpdateArea();
					update.replaceChildren();
					const docElement = result.documentElement
					update.appendChild(docElement)
					window['kite9-visualization-js'].formatSVG(docElement);
					
					// put it back in the result 
					result.appendChild(docElement);
					callback(result);
				} else {
					console.error("Couldn't transform'");
				}
			};
				
			xhr.send();		
		} else {
			return callback(this.transformer.transformToDocument(doc));
		}
  	}
	
} 