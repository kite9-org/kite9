
export function createSVGResolver() {
	
	return (text) => {
		var parser = new DOMParser();
		var doc = parser.parseFromString(text, "image/svg+xml");
		return doc;
	}
	
}