import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.7'
import { hasLastSelected } from '/github/kite9-org/kite9/client/bundles/api.js?v=v0.7'


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
		
		const e = hasLastSelected(selector(), true);
		
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
