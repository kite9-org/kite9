import { hasLastSelected, onlyLastSelected } from '../../../bundles/api.js'
import { textarea, fieldset, ok, cancel, inlineButtons, formValues } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { Selector } from '../../../bundles/types.js';
import { Command } from '../../../classes/command/command.js';
import { ContextMenu, ContextMenuCallback } from '../../../classes/context-menu/context-menu.js';

type TextCollector = (e: Element) => string

export function initEditContextMenuCallback(
	command: Command, 
	selector: Selector = undefined, 
	textCollector: TextCollector = undefined) : ContextMenuCallback {
	
	function createEditStep(e: Element, newText: string, oldText: string) {
		return {
			"type": 'ReplaceText',
			"fragmentId": e.getAttribute('id'),
			"to": newText,
			"from": oldText
			
		}
	}
	
	if (selector == undefined) {
		selector = function() {
			return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~='edit'].selected"));
		}
	}
	
	if (textCollector == undefined) {
		textCollector = function(e) {
			let text = e.querySelector("[k9-ui~='text']");
			text = (text != null) ? text : e;
			const lines = text.querySelectorAll('text');
			return Array.from(lines).map(l => l.textContent.trim()).reduce((a, b) => a +"\n" + b); 
		}
	}

	/**
	 * Provides a text-edit option for the context menu
	 */
	return function(event: Event, cm: ContextMenu) {
		
		const selectedElements = hasLastSelected(selector());
		
		if (selectedElements.length > 0) {
			
			cm.addControl(event, "/public/behaviours/editable/text/edit.svg", 'Edit Text', () => {
				const defaultText = textCollector(onlyLastSelected(selectedElements));
				cm.clear();
				const htmlElement = cm.get(event);
				htmlElement.appendChild(fieldset('Edit Text', [
					textarea('Enter Text', defaultText, { rows: 10 }),
					inlineButtons([
						ok('ok', {}, (e) => {              
							e.preventDefault();
							const values = formValues();
							const steps = Array.from(selectedElements).map(e => createEditStep(e, values.enterText, defaultText));
							command.pushAllAndPerform(steps);
							cm.destroy();
						}),
						cancel('cancel', [], () => cm.destroy())
					])
				]));
				
			});
		}
	}
}
