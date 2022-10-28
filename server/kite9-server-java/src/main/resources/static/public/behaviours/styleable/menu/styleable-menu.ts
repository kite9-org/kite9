import { hasLastSelected } from '../../../bundles/api.js'
import { textarea, form, ok, cancel, inlineButtons, formValues } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';

export function initStyleMenuContextMenuCallback(submenu, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui].selected");
		}
	}

	return function(event, cm) {
		
		const selectedElements = hasLastSelected(selector());

		if (selectedElements.length > 0) {
			
			cm.addControl(event, "/public/behaviours/styleable/menu/style.svg", 'Styles', () => {
				cm.clear();
				submenu.forEach(item => {
					item(event, cm);
				});
			});
				
		}
	}
}
