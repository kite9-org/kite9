/**
 * Returns the collection of elements so long as one of them has the lastSelected class.
 */
export function hasLastSelected(e: Element[]) : Element[] {
	for (let i = 0; i < e.length; i++) {
		const item = e[i];
		if (item.classList.contains("lastSelected")) {
			return e;
		}
	}
	
	return [];
}

/**
 * Returns the element from the collection with the lastSelected class, or null.
 */
export function onlyLastSelected(e: Element[]) : Element | null {
	for (let i = 0; i < e.length; i++) {
		const item = e[i];
		if (item.classList.contains("lastSelected")) {
			return item;
		}
	}
	
	return null;
}

/**
 * Given a list of Ids, return elements that are dependent on them
 */
export function getDependentElements(ids : string[]) : Element[] {
	return Array.from(document.querySelectorAll("div.main svg [id][k9-info*='link:']")).filter(e => {
		const parsed = parseInfo(e);
		const depIds : string[] = parsed['link'];
		const anyMatch = depIds
			.map(di => ids.indexOf(di) != -1)
			.reduce((a, b) => a || b, false);
		return anyMatch;
	});	
}

/**
 * Returns a connection linking to id1 and (if provided) also id2.
 */
export function getExistingConnections(id1: string, id2: string | null = undefined): Element[] {
	return Array.from(document.querySelectorAll("div.main svg [id][k9-info*='link:']")).filter(e => {
		const parsed = parseInfo(e);
		const ids = parsed['link'];

		if (ids) {

			if (id2) {
				return ((ids[0] == id1) && (ids[1] == id2)) ||
					((ids[1] == id1) && (ids[0] == id2));
			} else {
				return (ids[0] == id1) || (ids[1] == id1);
			}

		}

		return false;
	});
}

export function reverseDirection(d : string) : string {
    switch (d) {
    case "LEFT":
            return "RIGHT";
    case "UP":
            return "DOWN";
    case "DOWN":
            return "UP";
    case "RIGHT":
            return "LEFT";
    }

    return d;
}

type Info = {
	'link'?: string[],
	'terminates-at'?: string,
	'layout'? : string,
	'end'? : string,
	'terminates'?: string,
	'direction'?: string,
	'temporary'?: boolean
}

export function parseInfo(t : Element) : Info  {
	if ((t!= null) &&(t.hasAttribute("k9-info"))) {
		const parts = t.getAttribute("k9-info").split(';');
		const out = {}
		parts.forEach(p => {
			p=p.trim();
			const colon = p.indexOf(":");
			if (colon > -1) {
				const name = p.substring(0, colon).trim();
				const value = p.substring(colon+1).trim();
				
				if (value.startsWith("[") && value.endsWith("]")) {
					const v2 = value.substring(1, value.length-1)
						.split(",")
						.map(s => s.startsWith("'") ? s.substring(1, s.length-1) : parseFloat(s.trim()));
					out[name]=v2;
				} else {
					out[name]=value;
				}
			}
		});
		return out as Info;
	} else {
		return {} as Info;
	}
}

export function getParentElement(elem: Element): Element | null {
	let p = elem.parentElement;
	while ((p != null) && (!p.hasAttribute("k9-elem"))) {
		p = p.parentElement;
	}

	if (p instanceof Element) {
		return p as Element;
	} else {
		return null;
	}
}

export function getParentElements(elements : Element[]) : Element[] {
	return [...new Set(Array.from(elements).flatMap(e => getParentElement(e)))];
}


export function getNextSiblingElement(elem : Element) : Element | null {
	const p = elem;
	const children = Array.from(p.parentElement.children).filter(e => e.hasAttribute("k9-elem"));
	const thisOne = children.indexOf(elem);
	if (thisOne < children.length-1) {
		return children[thisOne+1];
	} else {
		return null;
	}
}

export function getNextSiblingId(elem : Element) : string | null {
	elem = getNextSiblingElement(elem);
	return elem == null ? null : elem.getAttribute("id");
}


export function getContainedChildIds(elem : Element, criteria : (e: Element) => boolean = () => true) : string[] {
	
	const out : string[] = [];
	
	function traverse(e: ParentNode) {
		for (const c of Array.from(e.children)) {
			if (c.hasAttribute("id") && c.hasAttribute("k9-elem")) {
				const id = c.getAttribute("id");
				if ((!out.includes(id)) && (criteria(c))) {
					out.push(id);
				}	
			} else  {
				traverse(c);
			}
			
		}
	}
	
	traverse(elem);
	return out;
}



export function getContainingDiagram(elem? : Element) : Element {
	if (elem == null) {
		return null;
	}
	const pcd = getContainingDiagram(elem.parentElement);
	if (pcd) {
		return pcd;
	} else if (elem.hasAttribute("k9-elem")) {
		return elem;
	}
}

type Transform = {
	scaleX: number,
	scaleY: number,
	translateX: number,
	translateY: number,
	translate?: number[],
	scale?: number | number[],
	matrix?: number[] 
}

export function transformToCss(a : Transform) : string {
	let out = '';
	if ((a.scaleX) && (a.scaleX != 1 )) {
		out = out + "scaleX("+a.scaleX+") ";
	}
	if ((a.scaleX) && (a.scaleY != 1 )) {
		out = out + "scaleY("+a.scaleY+") ";	
	}
	if ((a.translateX) && (a.translateX != 0 )) {
		out = out + "translateX("+a.translateX+"px) ";
	}
	if ((a.translateY) && (a.translateY != 0 )) {
		out = out + "translateY("+a.translateY+"px) ";
	}
	if (a.matrix) {
		out = "matrix("+a.matrix+") ";	
	}
	
	return out;
}


export function number(value: string) : number {
	if (value == null) {
		return null;
	} else {
		const out = Number(value.replace(/[a-z]+$/, ''));
		if (isNaN(out)) {
			return 0;
		} else {
			return out;
		}
	} 
}

export function parseTransform(a: string): Transform {
	const b: Transform = {
		translateX: 0,
		translateY: 0,
		scaleX: 1,
		scaleY: 1
	};

	if (a == null) {
		return b;
	}
	
	const arr = a.match(/(\w+\((-?\d*\.?\d*e?-?\d*[(px), ]*)+\))+/g)
	for (const i in arr) {
		const c = arr[i].match(/[\w.-]+/g);
		const name = c.shift();
		if (c.length > 1) {
			b[name] = c.map(e => number(e));
		} else {
			b[name] = number(c[0]);
		}
	}

	if (b.translate) {
		b.translateX = b.translate[0];
		b.translateY = b.translate[1];
		delete b.translate;
	}

	if (b.scale) {
		if (b.scale instanceof Array) {
			b.scaleX = b.scale[0];
			b.scaleY = b.scale[1];
		} else {
			b.scaleX = b.scale;
			b.scaleY = b.scale;
		}
		delete b.scale;
	}

	return b;
}

export function changeId(e: Element, oldId: string, newId: string) {
	let descend = true;
	if (e.hasAttribute("id")) {
		const idAttr = e.getAttribute("id");		
		const partIndex = idAttr.indexOf("@");
		
		if (partIndex > -1) {
			const id = idAttr.substring(0, partIndex);
			const part = idAttr.substring(partIndex+1);
			
			if (id.indexOf(oldId) == 0) {
				newId = id.replace(oldId, newId);
				e.setAttribute("id", newId+"@"+part);
			} else if (oldId.indexOf(id) == 0) {
				// do nothing
			} else {
				descend = false;
			}
		} else {
			const id = idAttr;
			
			if (id == oldId) {
				e.setAttribute("id", newId);
			} else {
				descend = false;
			}
		}
	}
	
	if (descend) {
		let cntr = 0;
		for (let i = 0; i < e.children.length; i++) {
			const c = e.children[i];
			const cOld = c.getAttribute("id");
			const cNew = cOld == undefined ? newId : newId + "-" + (cntr ++);
			changeId(c, cOld, cNew);
		}
	}
}

export function suffixIds(elements: Element[], suffix : string) {
	elements.forEach(e => {
		if (e.hasAttribute("id")) {
			const idAttr = e.getAttribute("id");
			
			const partIndex = idAttr.indexOf("@");
			if (partIndex > -1) {
				const id = idAttr.substring(0, partIndex);
				const part = idAttr.substring(partIndex+1);
				e.setAttribute("id", id+suffix+"@"+part);
			} else {
				e.setAttribute("id", idAttr+suffix);
			}
		}
	});
}


export function handleTransformAsStyle(e : Element) : Transform | null {
	if (e.hasAttribute('transform')) {
		const t = parseTransform(e.getAttribute('transform'));
		const css = transformToCss(t);
		if (e instanceof SVGGraphicsElement) {
			e.style.setProperty('transform', css, '');
		} else {
			e.setAttribute("style", "transform: "+css+";");
		}
		e.removeAttribute('transform');
		return t;
	}
	
	return null;
}

let lastId = 1;

export function createUniqueId() : string {
	lastId++;
	
	while (document.querySelectorAll("[id='"+lastId+"']").length > 0) {
		lastId++;
	}
	
	return ""+lastId;
}


export function getKite9Target(v: Node | null) : Element | null {
	if (v instanceof Element) {
		if (v.hasAttribute("k9-elem") && v.hasAttribute("id")) {
			return v;
		} else {
			return getKite9Target(v.parentNode);
		}
	} else {
		return null;
	} 
}

export function isTerminator(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("terminates:");
}

export function isLink(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("link:");
}

export function isPort(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("port:");
}

export function isConnected(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	const typeSet = (att == undefined ? "" : att).includes("connected");
	const ui = v.getAttribute("k9-ui");
	const uiSet = (ui == undefined ? "" : ui).includes("connect");
	return uiSet && typeSet;
}

export function isDiagram(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("rectangular: diagram");
}

export function isRectangular(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("rectangular:");
}

export function isLabel(v? : Element) : boolean {
	if (v == null)
		return false;
	const att = v.getAttribute("k9-info");
	return (att == undefined ? "" : att).includes("rectangular: label");
}

export function isCell(e? : Element) : boolean {
	if (e == null)
		return false;
	if (e.hasAttribute("k9-info")) {
		const out = e.getAttribute("k9-info");
		if (out.includes("grid-x")) {
			return true;
		} 
	}
	
	return false;
}

export function isGrid(e? : Element) : boolean {
	if (e == null)
		return false;
	if (e.hasAttribute("k9-info")) {
		const out = e.getAttribute("k9-info");
		if (out.includes("layout: GRID;")) {
			return true;
		}
	}
	
	return false;
}


export function connectedElement(terminator : Element, within : SVGSVGElement) : Element {
	const info = parseInfo(terminator)
	const at = info['terminates-at']
	const end = within.getElementById(at);
	return end;
}

export function connectedElementOtherEnd(terminator : Element, within : SVGSVGElement) : Element {
	const info = parseInfo(terminator)
	const parent = within.getElementById(info['terminates']);
	const otherTerminators = getContainerChildren(parent)
		.filter(c => c != terminator)
		.filter(c => isTerminator(c));
		
	if (otherTerminators.length != 1) {
		throw Error("Links should only have two terminators : "+parent.getAttribute('id'));
	}
	
	return connectedElement(otherTerminators[0], within);	
}

export function getContainerChildren(container: Element, ignore : Element[] = []) {
	const allChildren = Array.from(container.querySelectorAll("[id][k9-info]"))
		.filter(e => getParentElement(e) == container)
		.filter(e => ignore.indexOf(e) == -1);
		
	return allChildren;
}

export function xmlDepth(a: Node | null) : number {
	if (a == undefined) {
		return 0;
	} else {
		return 1 + xmlDepth(a.parentNode);
	}
}

export function getCommonContainer(a : Node, b : Node) : Element {
	let aDepth = xmlDepth(a);
	let bDepth = xmlDepth(b);
	
	while (aDepth > bDepth) {
		aDepth --;
		a = a.parentNode;
	}
	
	while (bDepth > aDepth) {
		bDepth --;
		b = b.parentNode;
	}
	
	while (a != b) {
		a = a.parentNode;
		b = b.parentNode;
	} 
	
	return a as Element;
}

export function onlyUnique(value : unknown, index: number, self: unknown[]) { 
    return self.indexOf(value) === index;
}

export function encodeADLElement(text: string) : string {
	return btoa(text);
}

export function addQueryParam(url : string, name: string, value: string) : string {
	if (url.indexOf("?") != -1) {
		return url + "&" + name + "=" + value;
	} else {
		return url + "?" + name + "=" + value;
	}
}

export function getAffordances(element: Element) : string[] {
	const ui = element?.getAttribute("k9-ui")?.split(" ") ?? [];
	return ui;
}

export function getDocumentParam(p: string): string {
	return document['params'][p];
}
