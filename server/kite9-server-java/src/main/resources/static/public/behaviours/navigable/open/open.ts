import { getMainSvg } from '../../../bundles/screen.js'
import { hasLastSelected } from '../../../bundles/api.js'


export function initOpenContextMenuCallback(command, selector) {
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~=open]");
		}
	}
	
	/**
	 * Provides a link option for the context menu
	 */
	return function(event, contextMenu) {
		
		const e = onlyLastSelected(selector());
		
		if (e) {
			contextMenu.addControl(event, "/public/behaviours/navigable/open/open.svg", "Open",
				function(e2, selector) {
					contextMenu.destroy();
					var url = e.getAttribute("id");
					window.location.href = url;
			});
		}
	}
}
