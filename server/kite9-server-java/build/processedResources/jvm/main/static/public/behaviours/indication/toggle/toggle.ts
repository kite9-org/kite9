import { getMainSvg } from '../../../bundles/screen.js'
import { icon } from '../../../bundles/form.js'
import { InstrumentationCallback } from '../../../classes/instrumentation/instrumentation.js';

export function initToggleInstrumentationCallback(on = true) : InstrumentationCallback {

	return function(nav) {

		let toggle = nav.querySelector("#_indication-toggle");
		const main = getMainSvg().parentElement;

		if (toggle == undefined) {

			toggle = nav.appendChild(icon('_indication-toggle', "Toggle Indicators", '/public/behaviours/indication/toggle/toggle.svg', function() {
				if (main.classList.contains('indicators-on')) {
					main.classList.remove('indicators-on');
					main.classList.add('indicators-off');
					toggle.classList.remove('on');
				} else {
					main.classList.add('indicators-on');
					main.classList.remove('indicators-off');
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