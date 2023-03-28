export function createSVGResolver(transition, metadata) {
    const META_NAMESPACE = "http://www.kite9.org/schema/metadata";
    const SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    return (text) => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(text, "image/svg+xml");
        const docNS = doc.documentElement.namespaceURI;
        if (docNS == META_NAMESPACE) {
            metadata.process(doc);
        }
        else if (docNS == SVG_NAMESPACE) {
            metadata.process(doc);
            transition.change(doc);
        }
    };
}
