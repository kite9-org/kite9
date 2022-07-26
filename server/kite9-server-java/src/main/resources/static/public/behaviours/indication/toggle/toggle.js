import { getMainSvg } from '/public/bundles/screen.js'
import { ensureCss } from '/public/bundles/ensure.js'
import { icon } from '/public/bundles/form.js'


export function initToggleInstrumentationCallback(on) {
	
	if (on == undefined) {
		on = true;
	}
	
	return function (nav) {
	
		var toggle = nav.querySelector("#_indication-toggle");
		const main = getMainSvg().parentElement;
	
		if (toggle == undefined) {
	
		    toggle = nav.appendChild(icon('_indication-toggle', "Toggle Indicators", '/public/behaviours/indication/toggle/toggle.svg', function() {
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