
export function createSVGResolver(transition) {
	
	return (text) => {
		var parser = new DOMParser();
		var doc = parser.parseFromString(text, "image/svg+xml");
		transition.change(doc);
	}
	
}