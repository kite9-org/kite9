import { ensureJs, once } from '../../../bundles/ensure.js'
import { encodeADLElement } from '../../../bundles/api.js'
import { Transition } from '../../../classes/transition/transition.js';
import { Metadata } from '../../../classes/metadata/metadata.js';
import { ADLUpdateCallback, UpdateableResolver } from '../navigable.js';

type Kite9Library = {
    initCSS(): void,
    formatSVG(e: Element) : void
}

export function createAdlToSVGResolver(
	transition: Transition,
	adlCallback: ADLUpdateCallback,
	metadata: Metadata) : UpdateableResolver {

	function getKite9Library() : Kite9Library {
	    return window['kite9-parent-kite9-visualization'] as Kite9Library;
	}

	const XSL_TEMPLATE_NAMESPACE = "http://www.kite9.org/schema/xslt";
	const ADL_NAMESPACE = "http://www.kite9.org/schema/adl";
	const DEFAULT_TEMPLATE = "/public/templates/basic/basic-template.xsl";
	const META_NAMESPACE = "http://www.kite9.org/schema/metadata";


	let transformer = null;	  // set on first use
	let template = null;

	/**
	 * Replace with ES6 module as soon as possible
	 */
	once(function() {
		ensureJs('/webjars/kotlin/1.4.30/kotlin.js')
			.then(() => {
				return ensureJs('/public/external/kite9-parent-kite9-visualization.js');
			})
			.then(() => {
				getKite9Library().initCSS();
			})
	});

	/** 
	 * Makes sure we have somewhere in the document to do the Kite9 layout
	 */
	function ensureUpdateArea() {
		// this is where changes go for layout	
		let update = document.getElementById("_update")
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
	function getTemplateUri(doc: Document) {

		const template = doc.documentElement.getAttributeNS(XSL_TEMPLATE_NAMESPACE, "template");

		if ((template == null) || (template.length == 0)) {
			if (ADL_NAMESPACE == doc.documentElement.namespaceURI) {
				// default to the basic template
				return new URL(DEFAULT_TEMPLATE, document.location.href).href;
			}
		}

		return new URL(template, document.location.href).href;
	}

	function layoutSVGDocument(result: Document, doc: Document) {
		// we have to add the result to the main dom 
		// to get the computedStyleMap and format
		const update = ensureUpdateArea();
		const docElement = result.documentElement
		update.appendChild(docElement)
		getKite9Library().formatSVG(docElement);

		// put it back in the result 
		result.appendChild(docElement);
		transition.change(result);

		// handle adl update
		metadata.process(doc);
		const docText = new XMLSerializer().serializeToString(doc.documentElement);
		adlCallback(encodeADLElement(docText));
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
				const xhr = new XMLHttpRequest();
				xhr.open('GET', currentTemplate);
				xhr.responseType = 'document';
				xhr.overrideMimeType('text/xml');
				xhr.onload = function() {
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
			alert("Don't know how to process: " + docNS)
		}
	}

} 