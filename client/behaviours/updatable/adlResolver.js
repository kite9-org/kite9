import { ensureJs, once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.8'
import { encodeADLElement } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.8'


export function createAdlToSVGResolver(transition, command, metadata) {

	const XSL_TEMPLATE_NAMESPACE = "http://www.kite9.org/schema/xslt";
	const ADL_NAMESPACE = "http://www.kite9.org/schema/adl";
	const DEFAULT_TEMPLATE = "/public/templates/basic/basic-template.xsl";
    const META_NAMESPACE = "http://www.kite9.org/schema/metadata";


	var transformer = null;	  // set on first use
	var template = null;
	
	/**
	 * Replace with ES6 module as soon as possible
	 */
	once(function() {
		ensureJs('/webjars/kotlin/1.4.30/kotlin.js')
			.then(() => {
				return ensureJs('/webjars/kite9-visualization-js/0.1-SNAPSHOT/kite9-visualization-js.js');
			})	
			.then(() => {
				window['kite9-visualization-js'].initCSS();
			})
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
			update.setAttribute("media", "editor");
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
		const docElement = result.documentElement
		update.appendChild(docElement)
		window['kite9-visualization-js'].formatSVG(docElement);
		
		// put it back in the result 
		result.appendChild(docElement);
		transition.change(result);

		// handle adl update
		metadata.process(doc);
		const docText = new XMLSerializer().serializeToString(doc.documentElement);
		command.adlUpdated(encodeADLElement(docText));
	}

	return (text) => {
		const parser = new DOMParser();
		const doc = parser.parseFromString(text, "text/xml");
		const docNS = doc.documentElement.namespaceURI
		
		if (docNS == META_NAMESPACE) {
			metadata.process(doc);
		} else if (docNS == ADL_NAMESPACE) {
			// transformable ADL
			const currentTemplate = getTemplateUri(doc);
		
			if (currentTemplate != template) {
				var xhr = new XMLHttpRequest;
				xhr.open('GET', currentTemplate);
				xhr.responseType = 'document';
				xhr.overrideMimeType('text/xml');
				xhr.onload = function () {
				  	if (xhr.readyState === xhr.DONE && xhr.status === 200) {
						transformer = new XSLTProcessor();
						transformer.importStylesheet(xhr.responseXML);	
						template = currentTemplate;
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
		} else {
			alert("Don't know how to process: "+docNS)
		}
  	}
	
} 