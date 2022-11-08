import { getAffordances } from '../../../bundles/api.js'
import { formatStyle, Styles } from '../../../bundles/css.js'
import { formObject, fieldset, colour, numeric } from '../../../bundles/form.js'
import { getMainSvg, canRenderClientSide } from '../../../bundles/screen.js';
import { BuildControlsCallback, extractFormValues } from '../styleable.js';

export const fillIcon = "/public/behaviours/styleable/fill/paintbrush.svg";

const rgba2hex = (rgba : string) => `#${rgba.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+\.{0,1}\d*))?\)$/).slice(1).map((n, i) => (i === 3 ? Math.round(parseFloat(n) * 255) : parseFloat(n)).toString(16).padStart(2, '0').replace('NaN', '')).join('')}`

function getColour(propName: string, e: Element) {
	if (canRenderClientSide()) {
		return rgba2hex(e['computedStyleMap']().get(propName).toString());
	} else {
		return '';
	}
}

export function initFillBuildControls() : BuildControlsCallback {
	return function(selectedElement, style) {
		const affordances = getAffordances(selectedElement);
		const needsFill = affordances.includes('fill');
		const needsStroke = affordances.includes('stroke');
		const out = []
		
		if (needsFill) {
			const fill = colour("fill", style['fill']);
			const fillOpacity = numeric('fill-opacity', parseFloat(style['fill-opacity']), {min: 0, max: 1, step: 0.1});
			const fillControls = [
				fill,
				fillOpacity
			];
			
			out.push(fieldset("fill", fillControls));

		}
		
		if (needsStroke) {
			const stroke = colour("stroke", style['stroke']);
			const strokeOpacity = numeric('stroke-opacity', parseFloat(style['stroke-opacity']),  {min: 0, max: 1, step: 0.1});
			const strokeWidth = numeric('stroke-width', parseFloat(style['stroke-width']), {min: 0});
			
			const strokeControls = [
				stroke,
				strokeOpacity,
				strokeWidth
			];
			
			out.push(fieldset("stroke", strokeControls));

		}
	
		return out;
	}	
}

export function fillSelector() {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=fill].selected,[id][k9-ui~=stroke].selected"));
}

export function initFillChangeEvent(selectedElement: Element, svgStyle: Styles) : EventListener {
	return () => {
		const values = extractFormValues();
		const newStyle = {...svgStyle, ...values};
		const formatted = formatStyle(newStyle);
		if (newStyle['fill']) {
			selectedElement.classList.remove('selected');
			selectedElement.classList.remove('mouseover');
		}

		selectedElement.setAttribute("style", formatted);
		
		// update swatches
		const form = formObject();
		const fill = form.querySelector('#fill-patch') as HTMLInputElement;
		const stroke = form.querySelector('#stroke-patch') as HTMLInputElement;
		
		if (fill) {
			fill.value = getColour('fill', selectedElement);
		}
		
		if (stroke) {
			stroke.value = getColour('stroke', selectedElement);
		}
	}
}
