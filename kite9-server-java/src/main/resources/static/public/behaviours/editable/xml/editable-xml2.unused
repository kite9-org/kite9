// attempt to migrate to codemirror 6

import { Modal } from '../../../classes/modal/modal.js'
import { hasLastSelected, encodeADLElement } from '../../../bundles/api.js'
import { form, ok, cancel, inlineButtons, formFields } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { ensureCss } from '../../../bundles/ensure.js'
import { Command } from '../../../classes/command/command.js';
import { Selector } from '../../../bundles/types.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';
import { EditorView } from "../../../../node_modules/@codemirror/view/dist/index.js"
import { basicSetup } from "../../../../node_modules/@codemirror/basic-setup/dist/index.js"
import { xml } from  "../../../../node_modules/@codemirror/lang-xml/dist/index.js"
import { EditorState, Text } from "../../../../node_modules/@codemirror/state/dist/index.js"

type XMLCollector = (e: Element) => string

export function initXMLContextMenuCallback(
	command: Command,
	selector: Selector = undefined,
	xmlCollector: XMLCollector = undefined): ContextMenuCallback {

	const xmlModal = new Modal('_xml-editor');

	ensureCss('/webjars/codemirror/5.58.3/lib/codemirror.css');

	function createUpdateStep(e: Element, text: string) {
		const id = e.getAttribute('id');
		return {
			"type": 'ReplaceXML',
			"fragmentId": id,
			"to": encodeADLElement(text),
			"from": command.getAdl(id)
		}
	}

	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui].selected"));
		}
	}

	if (xmlCollector == undefined) {
		xmlCollector = function(e) {
			const id = e.getAttribute("id");
			const adlElement = command.getADLDom(id)
			const adlElementText = new XMLSerializer().serializeToString(adlElement);
			return adlElementText;
		}
	}


	function createXMLEditor(inside: Element, value: string) {
		const state = EditorState.create({
			doc: Text.of(value.split("\n")),
			extensions: [
				basicSetup,
				xml(),
				EditorState.tabSize.of(8)
			]
		})
		
		const view = new EditorView({
			state,
			parent: inside
		})
		
		return view;
	}




	/**
	 * Provides a text-edit option for the context menu
	 */
	return function(event: Event, cm: ContextMenu) {

		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length == 1) {
			const theElement = selectedElements[0];

			cm.addControl(event, "/public/behaviours/editable/xml/xml.svg", 'Edit XML', () => {
				const defaultText = xmlCollector(theElement);
				cm.destroy();
				xmlModal.clear();
				const editableArea = formFields([]);

				xmlModal.getContent().appendChild(form([
					editableArea,
					inlineButtons([
						ok('ok', {}, (e) => {
							e.preventDefault();
							const steps = [createUpdateStep(theElement, mirror.state.doc.toString())];
							command.pushAllAndPerform(steps);
							xmlModal.destroy();
						}),
						cancel('cancel', [], () => xmlModal.destroy())
					])
				], 'editXml'));

				xmlModal.open()
				const mirror = createXMLEditor(editableArea, defaultText);
				//mirror.setSize(editableArea.clientWidth, editableArea.clientHeight);
			});
		}
	}
}
