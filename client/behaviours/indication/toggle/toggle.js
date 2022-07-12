import { getMainSvg } from '/github/kite9-org/kite9/client/bundles/screen.js?v=v0.9'
import { ensureCss } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.9'
import { icon } from '/github/kite9-org/kite9/client/bundles/form.js?v=v0.9'


export function initToggleInstrumentationCallback(on) {
	
	if (on == undefined) {
		on = true;
	}
	
	return function (nav) {
	
		var toggle = nav.querySelector("#_indication-toggle");
		const main = getMainSvg().parentElement;
	
		if (toggle == undefined) {
	
		    toggle = nav.appendChild(icon('_indication-toggle', "Toggle Indicators", '/github/kite9-org/kite9/client/behaviours/indication/toggle/toggle.svg', function() {
		    	if (main.classList.contains('indicators-on')) {
		    		main.classList.remove('indicators-on');
		    		toggle.classList.remove('on');
		    	} else {
		    		main.classList.add('indicators-on');
		    		toggle.classList.add('on');
		    	}
		    }));
		    			
			if (on) {
				main.classList.add("indicators-on");
				toggle.classList.add('on');
			}
		}
	}
	
}