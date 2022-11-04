import { getMainSvg } from '../../../bundles/screen.js'
import { fieldset, select, numeric } from '../../../bundles/form.js' 
import { Selector } from '../../../bundles/types.js';
import { BuildControlsCallback } from '../styleable.js';

export const fontIcon = '/public/behaviours/styleable/font/font.svg';

export const fontSelector: Selector = () => {
	return Array.from(getMainSvg().querySelectorAll("[id][k9-ui~=font].selected"));
}

function onlyUnique(value, index, self) {
	return self.indexOf(value) === index;
}		

export type FontDeclaration = {
	styles: string,
	weights: string
}
	
export function initFontBuildControls() : BuildControlsCallback {
	return function(selectedElement, style) {
		const fontFamily = style['font-family'];
		const fontWeight = style['font-weight'];
		const fontStyle = style['font-style'];
		const fontSize = style['font-size'];
		
		const availableFamilies = document['params']['font-families'] as { [index: string] : FontDeclaration };
		const allStyles = ['', ...Object.values(availableFamilies)
			.map(e => e.styles)
			.flatMap(e => e.split(" "))
			.filter(onlyUnique).sort() ];

		const allWeights = ['',  ...Object.values(availableFamilies)
			.map(e => e.weights)
			.flatMap(e => e.split(" "))
			.filter(onlyUnique).sort() ];

		
		const fontFamilySelect = select('font-family', fontFamily, {}, [ '', ...Object.keys(availableFamilies) ]);
		const fontWeightSelect = select('font-weight', fontWeight, {}, allWeights)
		const fontStyleSelect = select('font-style', fontStyle, {}, allStyles)
		const fontSizeField = numeric('font-size', parseFloat(fontSize));
		
		function updateWeightAndStyle(selected: string) {
			const goodWeights = selected ? [ '', ...availableFamilies[selected].weights.split(" ") ] : allWeights;
			const goodStyles = selected ? [ '', ...availableFamilies[selected].styles.split(" ") ] : allStyles;	
			fontWeightSelect.querySelectorAll("option")
				.forEach(e => goodWeights.indexOf(e.value) > -1 ? 
					e.removeAttribute("disabled") : 
					e.setAttribute("disabled", "disabled"));
			fontStyleSelect.querySelectorAll("option")
				.forEach(e => goodStyles.indexOf(e.value) > -1 ? 
					e.removeAttribute("disabled") : 
					e.setAttribute("disabled", "disabled"));
		}
		
		const dd = fontFamilySelect.querySelector("select");
		dd.addEventListener("input", () => {
				const newFamily = dd.options[dd.selectedIndex].value;
				updateWeightAndStyle(newFamily)
			});
		
		updateWeightAndStyle(fontFamily);
		
		return [ fieldset('Font Settings', [
			fontFamilySelect,
			fontStyleSelect,
			fontWeightSelect,
			fontSizeField
		]) ];
	}
}
