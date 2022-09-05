import { hasLastSelected } from '/public/bundles/api.js'
import { parseStyle, formatStyle } from '/public/bundles/css.js'
import { textarea, form, ok, cancel, inlineButtons, formValues, fieldset, select, numeric } from '/public/bundles/form.js'
import { getMainSvg, canRenderClientSide } from '/public/bundles/screen.js';


export function initEnumContextMenuCallback(command, overlay, properties, values, icon, name, initChangeEvent, selector, buildControls) {

	var originalStyleMap, style;

	function getValue(propName, e) {
		return style[propName];
	}

	
	function extractFormValues(formName) {
		const asArray = Object.entries(formValues(formName));
		const filtered = asArray.filter(([key, value]) => !((key=='ok') || (key=='cancel')));
		return Object.fromEntries(filtered);
	}
	
	function createStyleSteps(e, oldValues, newValues) {
		function styleEqual(a, b) {
			if (((!a) || (a.length == 0)) && ((!b) || (b.length == 0))) {
				return true;
			} else {
				return a == b;
			}
		}
		
		const out = Object.keys(newValues)
			.filter(s => !styleEqual(oldValues[s],newValues[s]))
			.map(f =>  { return {
			fragmentId: e.getAttribute("id"),
			type: 'ReplaceStyle',
			name: f,
			to: newValues[f],
			from: oldValues[f]
		}});
		
		return out;
	}
	
	function createStyleMap(command, selectedElements) {
		const out = {};
		selectedElements.forEach(e => {
			const adlElement = command.getADLDom(e.getAttribute("id"))
			const style = parseStyle(adlElement.getAttribute("style"));
			out[e.getAttribute("id")] = style;
		})
		
		return out;
	}
	
	if (initChangeEvent == undefined) {
		initChangeEvent = function(selectedElement, svgStyle) {
			return e => {
				const values = extractFormValues('enum');
				const newStyle = {...svgStyle, ...values};
				const formatted = formatStyle(newStyle);
	
				selectedElement.setAttribute("style", formatted);
			};	
		}
	} 
	
	if (selector == undefined) {
		selector = function() {
			return getMainSvg().querySelectorAll("[id][k9-ui~=fill].selected");
		}
	}
	
	if (buildControls == undefined) {
		buildControls = function(selectedElement) {
			return [ fieldset(name, Object.keys(properties).map(p => select(p, getValue(p, selectedElement), {}, [ '', ...values ] ))) ];
		}
	}

	return function(event, cm) {
		
		const selectedElement = hasLastSelected(selector(), true);

		if (selectedElement) {
			
			cm.addControl(event, icon, name, () => {
				cm.clear();
				overlay.ensureOverlay();
				const selectedElements = Array.from(hasLastSelected(selector()));
				originalStyleMap = createStyleMap(command, selectedElements);
				style = originalStyleMap[selectedElement.getAttribute("id")];
				const originalSvgStyle = selectedElement.getAttribute("style")
				const svgStyle = parseStyle(originalSvgStyle);
				
				
				const theForm = form([				
					...buildControls(selectedElement),
					inlineButtons([
						ok('ok', {}, (e) => {
							const values = extractFormValues('enum');
							const steps = selectedElements
								.flatMap(e => createStyleSteps(e, originalStyleMap[e.getAttribute("id")], values));
							selectedElement.classList.add('selected');
							command.pushAllAndPerform(steps);
							cm.destroy();
							overlay.destroy()
							event.stopPropagation();
  				 			event.preventDefault();
						}),
						cancel('cancel', [], () => {
							selectedElement.setAttribute("style", originalSvgStyle);
							selectedElement.classList.add('selected');
							cm.destroy()
							overlay.destroy();
							event.stopPropagation();
  				 			event.preventDefault();
						})
					])
				], 'enum');
				
				const changeEvent = initChangeEvent(selectedElement, svgStyle);
				
				theForm.addEventListener("change", changeEvent);
				theForm.addEventListener("textInput", changeEvent);
				theForm.addEventListener("input", changeEvent);
				
				const htmlElement = cm.get(event);
				htmlElement.appendChild(theForm);	
				changeEvent();
			});
				
		}
	}
}