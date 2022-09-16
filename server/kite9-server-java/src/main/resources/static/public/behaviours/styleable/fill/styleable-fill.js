import { hasLastSelected } from '/public/bundles/api.js'
import { parseStyle, formatStyle } from '/public/bundles/css.js'
import { formObject, fieldset, colour, numeric } from '/public/bundles/form.js'
import { getMainSvg, canRenderClientSide } from '/public/bundles/screen.js';
import { extractFormValues } from '/public/behaviours/styleable/styleable.js';

export const fillIcon = "/public/behaviours/styleable/fill/paintbrush.svg";

const rgba2hex = (rgba) => `#${rgba.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+\.{0,1}\d*))?\)$/).slice(1).map((n, i) => (i === 3 ? Math.round(parseFloat(n) * 255) : parseFloat(n)).toString(16).padStart(2, '0').replace('NaN', '')).join('')}`

function getColour(propName, e) {
	if (canRenderClientSide()) {
		return rgba2hex(e.computedStyleMap().get(propName).toString());
	} else {
		return '';
	}
}

function getOpacity(propName, e) {
	if (canRenderClientSide()) {
		return e.computedStyleMap().get(propName).toString();
	} else {
		return '';
	}
}

function getIntegerPx(propName, e) {
	if (canRenderClientSide()) {
		return window['kite9-visualization-js'].getCssStyleDoubleProperty(propName, e)
	} else {
		return '1';
	}
}

export function initFillBuildControls() {
	return function(selectedElement, style, overlay, cm, event) {
		const fill = colour("fill", style['fill']);
		const stroke = colour("stroke", style['stroke']);
		const inheritedFillOpacity = getOpacity('stroke-opacity', selectedElement);
		const fillOpacity = numeric('fill-opacity', style['fill-opacity'], {min: 0, max: 1, step: 0.1});
		const inheritedStrokeOpacity = getOpacity('stroke-opacity', selectedElement);
		const strokeOpacity = numeric('stroke-opacity', style['stroke-opacity'],  {min: 0, max: 1, step: 0.1});
		const strokeWidth = numeric('stroke-width', style['stroke-width'], getIntegerPx('stroke-width', selectedElement), {min: 0});
		
		const fillControls = [
			fill,
			fillOpacity
		];
		const strokeControls = [
			stroke,
			strokeOpacity,
			strokeWidth
		];
	
		return [ 
			fieldset("Fill", fillControls),
			fieldset("Stroke", strokeControls) ];
	}	
}

export function fillSelector() {
	return getMainSvg().querySelectorAll("[id][k9-ui~=fill].selected");
}

export function initFillChangeEvent(selectedElement, svgStyle) {
	return e => {
		const values = extractFormValues();
		const newStyle = {...svgStyle, ...values};
		const formatted = formatStyle(newStyle);
		if (newStyle['fill']) {
			selectedElement.classList.remove('selected');
			selectedElement.classList.remove('mouseover');
		}

		selectedElement.setAttribute("style", formatted);
		
		// update swatches
		const form = formObject('enum');
		const fill = form.querySelector('#fill-patch');
		const stroke = form.querySelector('#stroke-patch');
		fill.value = getColour('fill', selectedElement);
		stroke.value = getColour('stroke', selectedElement);
	}
}
