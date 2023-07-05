import { getMainSvg, svg } from '../../../bundles/screen.js'
import { parseInfo, isTerminator } from '../../../bundles/api.js'
import { OptionalDirection } from '../../../bundles/types.js';


function terminatorSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-info*=terminator]")) 
		.filter(e => isTerminator(e)) as SVGGraphicsElement[];
}

function getDirection(e: Element): OptionalDirection {
	if (e == null) {
		return undefined;
	} else {
		const info = parseInfo(e);
		const l = info['direction'];
		return l;
	}
}

export function initTerminatorDirectionIndicator(selector = terminatorSelector) {


	const INDICATOR_SELECTOR = ":scope > g.k9-direction";

	const drawingFunctions = {
		"up": () => svg("polygon", { "points": "-10 12, 0 -8, 10 12" }),
		"down": () => svg("polygon", { "points": "10 -12, 0 8, -10 -12" }),
		"left": () => svg("polygon", { "points": "12 -10, -8 0, 12 10" }),
		"right": () => svg("polygon", { "points": "-12 -10, 8 0, -12 10" })
	}

	const noneFunction = () => svg("ellipse", { "cx": "0", "cy": 0, "rx": 8, "ry": 8 });

	function ensureDirectionIndicator(e: Element, direction: OptionalDirection) {
		let indicator = e.querySelector(INDICATOR_SELECTOR);
		if ((indicator != null) && (indicator.getAttribute("direction") != direction)) {
			e.removeChild(indicator);
		} else if (indicator != null) {
			return;
		}

		indicator = svg('g', {
			'class': 'k9-direction',
			'k9-highlight': 'fill',
			'direction': direction,
		}, [direction ? drawingFunctions[direction]() : noneFunction()]);

		e.appendChild(indicator)
	}

	window.addEventListener('DOMContentLoaded', function() {
		selector().forEach(function(v) {
			const direction = getDirection(v);
			ensureDirectionIndicator(v, direction)
		})
	})
}
