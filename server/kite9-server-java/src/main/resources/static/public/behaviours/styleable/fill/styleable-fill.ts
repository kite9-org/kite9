import { getAffordances } from '../../../bundles/api.js'
import { formatStyle, Styles } from '../../../bundles/css.js'
import { formObject, fieldset, colour, numeric, hexColour } from '../../../bundles/form.js'
import { getMainSvg } from '../../../bundles/screen.js';
import { BuildControlsCallback, extractFormValues } from '../styleable.js';

export const fillIcon = "/public/behaviours/styleable/fill/paintbrush.svg";

export function initFillBuildControls() : BuildControlsCallback {
	return function(selectedElement, style) {
		const affordances = getAffordances(selectedElement);
		const needsFill = affordances.includes('fill');
		const needsStroke = affordances.includes('stroke');
		const out = []
		
		if (needsFill) {
			const fill = colour("fill", style['fill']);
			const fillOpacity = numeric('fill-opacity', style['fill-opacity'], {min: 0, max: 1, step: 0.1});
			const fillControls = [
				fill,
				fillOpacity
			];
			
			out.push(fieldset("fill", fillControls));

		}
		
		if (needsStroke) {
			const stroke = colour("stroke", style['stroke']);
			const strokeOpacity = numeric('stroke-opacity', style['stroke-opacity'],  {min: 0, max: 1, step: 0.1});
			const strokeWidth = numeric('stroke-width', style['stroke-width'], {min: 0});
			
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
			fill.value = hexColour(newStyle['fill']);
		}
		
		if (stroke) {
			stroke.value = hexColour(newStyle['stroke']);
		}
	}
}
