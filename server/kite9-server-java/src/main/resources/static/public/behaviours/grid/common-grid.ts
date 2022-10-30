import { parseInfo } from '../../bundles/api.js'

export interface Ordinals extends Array<number> {
	max: number, 
	min: number
}

export function getOrdinal(index: number, ordinals: Ordinals) : number {
	
	if (index < ordinals.min) {
		return ordinals[ordinals.min] - (index + ordinals.min);
	} else if (index >= ordinals.max) {
		return ordinals[ordinals.max] + (index - ordinals.max);
	} else {
		let carry = 0;
		while (ordinals[index] == undefined) {
			index--;
			carry++;
		}
		return ordinals[index]+carry;
	}
}

export function nextOrdinal(o, ordinals) {
	const index = ordinals.indexOf(o);
	if (index == -1) {
		if (o < ordinals.min) {
			return ordinals.min;
		} else if (o > ordinals.max) {
			return o + 1;
		}
	}
	return getOrdinal(index+1, ordinals);
}

export function getOrdinals(container) : {xOrdinals: Ordinals, yOrdinals: Ordinals } {
	const xOrdinals = [] as Ordinals;
	xOrdinals.max = Number.MIN_SAFE_INTEGER;
	xOrdinals.min = Number.MAX_SAFE_INTEGER;
	
	const yOrdinals = [] as Ordinals;
	yOrdinals.max = Number.MIN_SAFE_INTEGER;
	yOrdinals.min = Number.MAX_SAFE_INTEGER;
	
	Array.from(container.children)
		.filter(e => e instanceof Element)
		.forEach(e => {
		const details = parseInfo(e as Element);
		if ((details != null) && (details['position']) && details['grid-x']) {
			const position = details['position'];
			const gridX = details['grid-x'];
			const gridY = details['grid-y'];
			
			xOrdinals[gridX[0]] = position[0];
			xOrdinals[gridX[1]-1] = position[1];
			yOrdinals[gridY[0]] = position[2];
			yOrdinals[gridY[1]-1] = position[3];
			
			xOrdinals.max = Math.max(xOrdinals.max, gridX[1]-1);
			xOrdinals.min = Math.min(xOrdinals.min, gridX[0]);
			yOrdinals.max = Math.max(yOrdinals.max, gridY[1]-1);
			yOrdinals.min = Math.min(yOrdinals.min, gridY[0]);
		}
	});

	return {
		xOrdinals: xOrdinals,
		yOrdinals: yOrdinals,
	}
}