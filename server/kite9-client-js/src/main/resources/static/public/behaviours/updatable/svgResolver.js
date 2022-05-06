
export function createSVGResolver(transition, metadata) {
	
	return (text) => {
		var parser = new DOMParser();
		var doc = parser.parseFromString(text, "image/svg+xml");
		transition.change(doc);
		metadata.update(doc);
	};
	
}