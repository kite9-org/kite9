import { suffixIds, addQueryParam } from '../../bundles/api.js'
import { icon } from '../../bundles/form.js'
import { ensureCss } from '../../bundles/ensure.js'

type Callback = (p: Palette, e: Element) => void
type UpdateCallback = (e?: Element) => void

type PaletteProps = {
	number: number,
	uri: string,
	selector: string
}

type SelectorFunction = (p: SVGElement | HTMLDivElement) => boolean
type ActionFunction = (e: Event) => void

/**
 * Provides functionality for populating/ showing/ hiding a palette.  
 */
export class Palette {

	id: string
	callbacks: Callback[] = [];
	paletteMap: PaletteProps[] = [];
	expanded: HTMLDivElement | null = null;
	updateCallbacks: UpdateCallback[] = [];

	// state
	openEvent: Event
	currentSelector: SelectorFunction;
	currentAction: ActionFunction;

	constructor(id: string, uriList: string[]) {
		const done = [];

		uriList = uriList == undefined ? [] : uriList;

		for (let i = 0; i < uriList.length; i += 2) {
			const uri = uriList[i];
			const selector = uriList[(i + 1)];

			if (!done.includes(uri)) {
				this.paletteMap.push({
					number: i / 2,
					uri: uri,
					selector: selector
				});
				done.push(uri);
			}
		}

		this.id = (id == undefined) ? "_palette" : id;

		/* const cssId = 'palette';
		 if (!document.getElementById(cssId)) {
		   const head = document.getElementsByTagName('head')[0];
		   const link = document.createElement('link');
		   link.id = cssId;
		   link.rel = 'stylesheet';
		   link.type = 'text/css';
		   link.href = '/public/classes/palette/palette.css';
		   link.media = 'all';
		   head.appendChild(link);
		 }*/

		ensureCss('/public/classes/palette/palette.css')

		let darken = document.getElementById("_darken");
		if (!darken) {
			darken = document.createElement("div");
			darken.setAttribute("id", "_darken");
			darken.setAttribute("class", "darken");
			document.querySelector("body").appendChild(darken);
			darken.style.display = 'none';
		}

		let palette = document.getElementById(this.id);
		if (!palette) {
			// create palette
			palette = document.createElement("div");
			palette.setAttribute("id", this.id);
			palette.setAttribute("class", "palette indicators-on");
			document.querySelector("body").appendChild(palette);

			// create area for control buttons
			const control = document.createElement("div");
			control.setAttribute("class", "control");
			palette.appendChild(control);

			// create concertina area
			const concertina = document.createElement("div");
			concertina.setAttribute("class", "concertina");
			palette.appendChild(concertina);

			this.paletteMap.forEach(p => this.loadPalette(p, concertina));
		}
	}

	loadPalette(p: PaletteProps, concertina: Element) {
		const id = "_palette-" + p.number;
		const item = document.createElement("div");
		item.setAttribute("k9-palette", p.selector);
		item.setAttribute("class", "palette-item");
		item.setAttribute("k9-palette-uri", p.uri);
		item.setAttribute("id", id);
		concertina.appendChild(item);

		// create loading indicator
		const loading = document.createElement("img");
		loading.setAttribute("src", "/public/classes/palette/loading.svg");
		item.appendChild(loading);

		// populate it
		fetch(p.uri, {
			credentials: 'include',
			method: 'GET',
			headers: {
				"Accept": "image/svg+xml"
			}
		})
			.then(response => {
				if (!response.ok) {
					return response.json().then(j => {
						loading.setAttribute("src", "/public/classes/palette/missing.svg");
						throw new Error(j.message);
					});
				}

				return response;
			})
			.then(response => response.text())
			.then(text => {
				console.log("Loaded " + p.uri);
				const parser = new DOMParser();
				return parser.parseFromString(text, "image/svg+xml");
			})
			.then(doc => {
				// set new ids
				removeScripts(doc);
				const diagramElements = Array.from(doc.querySelectorAll("[k9-elem][id]"));
				suffixIds(diagramElements, id);
				item.appendChild(doc.documentElement);
				item.removeChild(loading);

				this.callbacks.forEach(cb => {
					cb(this, item);
				})

				const evt = document.createEvent('Event');
				evt.initEvent('DOMContentLoaded', false, false);
				window.dispatchEvent(evt);
			})
			.catch(e => {
				alert("Problem loading palette: " + e);
			})
	}


	add(cb: Callback) {
		this.callbacks.push(cb);
	}

	addUpdate(cb: UpdateCallback) {
		this.updateCallbacks.push(cb);
	}

	getId(): string {
		return this.id;
	}

	get(event: Event) {
		return document.getElementById(this.id);
	}

	getOpenEvent(): Event {
		return this.openEvent;
	}

	getOpenPanel(): Element | null {
		return this.expanded;
	}

	getCurrentSelector(): SelectorFunction {
		return this.currentSelector;
	}

	getCurrentAction(): ActionFunction {
		return this.currentAction;
	}

	open(event: Event, selectorFunction: SelectorFunction, actionFunction: ActionFunction) {
		this.openEvent = event;
		this.currentSelector = selectorFunction;
		this.currentAction = actionFunction;

		const darken = document.getElementById("_darken");
		const palette = document.getElementById(this.id);
		const concertina = palette.querySelector("div.concertina");
		const control = palette.querySelector("div.control");

		// hide palettes without the selector
		const toShow: HTMLDivElement[] = [];

		palette.querySelectorAll("div.palette-item")
			.forEach(f => {
				const e = (f as HTMLDivElement)
				if (selectorFunction(e)) {
					e.style.display = 'block';
					toShow.push(e);

					// highlight selectable items on the palette
					const diagramElements = e.querySelectorAll("[k9-palette][id]");
					diagramElements.forEach(de2 => {
						const de = (de2 as SVGGraphicsElement)
						if (selectorFunction(de)) {
							de.style.cursor = 'grab';
							de.classList.remove('inactive');
						} else {
							de.classList.add('inactive');
							de.style.cursor = 'not-allowed';
						}
					})

				} else {
					e.style.display = 'none';
				}
			});

		// keep track of which palette we are showing
		if (this.expanded == undefined) {
			this.expanded = toShow[0];
		}

		// remove old control buttons
		while (control.firstChild) {
			control.removeChild(control.firstChild);
		}

		// add cancel button
		const cancel = icon('palette-cancel', 'Close', "/public/classes/palette/cancel.svg", () => this.destroy());
		control.appendChild(cancel);

		let paletteWidth = 100, paletteHeight = 100;
		let selectedDot: Element;

		function expandPanel(p: Palette, e: HTMLDivElement, dot: Element) {
			if (p.expanded) {
				p.expanded.style.maxHeight = "0px";
			}
			e.style.maxHeight = height + "px";
			p.expanded = e;
			control.querySelectorAll("img").forEach(e => e.classList.remove("selected"));
			if (dot != null) {
				dot.classList.add("selected");
			}
			p.updateCallbacks.forEach(cb => cb(e));
		}

		function getTitle(palette: Element): string {
			const d = "Untitled Palette";
			const diagram = palette.querySelector("g[k9-elem=diagram]");
			const title = diagram == undefined ? d : diagram.getAttribute("title");
			return title ? title : d;
		}

		function getIcon(palette: Element): string {
			const d = "/public/classes/palette/dot.svg";
			const diagram = palette.querySelector("g[k9-elem=diagram]");
			const icon = diagram == undefined ? d : diagram.getAttribute("icon");
			return icon ? icon : d;
		}


		// display new control buttons and size the overall thing
		toShow.forEach((e) => {
			e.style.maxHeight = "0px";
			e.style.visibility = 'show';
			e.style.display = 'block';
			const svg = e.querySelector(":first-child") as SVGSVGElement;
			if (!(svg.tagName.toLowerCase() == 'img')) {
				paletteWidth = Math.max(svg.width.baseVal.valueInSpecifiedUnits, paletteWidth);
				paletteHeight = Math.max(svg.height.baseVal.valueInSpecifiedUnits, paletteHeight);
			}

			if (toShow.length > 1) {
				const dot = icon('', getTitle(e), getIcon(e), () => expandPanel(this, e, dot));
				dot.classList.remove("hint--bottom");
				dot.classList.add("hint--right");

				if (e == this.expanded) {
					selectedDot = dot;
				}
				control.appendChild(dot);
			}
		});

		// ensure the palette appears in the centre of the screen
		const width = Math.min(paletteWidth + 30, window.innerWidth - 100);
		const height = Math.min(paletteHeight + 30, window.innerHeight - 100);

		palette.style.marginTop = (-height / 2) + "px";
		palette.style.marginLeft = (-width / 2) + "px";
		(concertina as HTMLDivElement).style.width = (width) + "px";
		(concertina as HTMLDivElement).style.height = (height) + "px";
		palette.style.visibility = 'visible';
		darken.style.display = 'block';

		expandPanel(this, this.expanded, selectedDot);

		return palette;
	}

	destroy() {
		this.currentAction = undefined;
		const palette = document.getElementById(this.id);
		const darken = document.getElementById("_darken");
		palette.style.visibility = 'hidden';
		darken.style.display = 'none';
		this.updateCallbacks.forEach(cb => cb());
		this.expanded = undefined;
	}
}


/**
 * These are not needed for palettes, because when you pull something off a palette, it will use the
 * main documents stylesheet.  Also, these interact with the main svg area, breaking the styling.
 */
function removeScripts(doc: Document) {
	doc.querySelectorAll("script,style,defs").forEach(n => n.parentElement.removeChild(n));
}

export function initPaletteHoverableAllowed(palette: Palette) {

	return function(v: SVGElement) {
		const currentSelector = palette.getCurrentSelector();
		return currentSelector(v);
	}

}

/**
 * For the purposes of referencing the ADL of an element on a palette
 */
export function getElementUri(e: SVGElement, palettePanel: HTMLDivElement) {
	const paletteId = palettePanel.getAttribute("id");
	const id = e.getAttribute("id");
	return addQueryParam(palettePanel.getAttribute("k9-palette-uri"), "format", "adl") + "#" + id.substring(0, id.length - paletteId.length);
}