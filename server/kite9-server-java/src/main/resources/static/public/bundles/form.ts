/**
 * Functions for programmatically building a javascript form.
 * 
 * general shape of this:
 * 
 * formElement(placeholder, value, {attName: value, class: classes}, contents)
 * 
 * id and name will be the same, based on the placeholder.
 * 
 * returns:  a div containing the form elements.
 * 
 * These are to be used with the ContextMenu class for building the menu.
 * 
 */
import { ensureCss } from './ensure.js'

ensureCss('/public/external/normform-2.0.css');
ensureCss('/webjars/hint.css/2.3.2/hint.css');

export const DEFAULT_FORM_ID = 'default-form';

export function icon(id: string, title: string, image: string, onClick: (e: Event) => void = undefined, atts: object = {}): HTMLElement {
	const a = create('a', { 'class': 'hint--bottom hint--bounce icon', 'aria-label': title, 'id': id }, [
		create('img', { 'src': image, ...atts })
	]);
	if (onClick) {
		a.addEventListener("click", onClick);
	}
	return a as HTMLElement;
}

export function largeIcon(id: string, title: string, image: string, onClick: (e: Event)=> void, atts: object = {}) : HTMLElement {
   const a = create('a', {'class': 'hint--bottom hint--bounce large-icon', 'aria-label': title, 'id': id}, [
     create('img', { 'src': image, ...atts})
   ]);
   a.addEventListener("click", onClick);
   return a;
}

export function img(id: string, src: string, atts: object = {}) : HTMLElement {
	return create('img', { 'id': id, 'src' : src, ...atts})
}

export function change(e: HTMLElement, f : (e: Event) => void) : HTMLElement {
	e.addEventListener("change", f);
	return e;
}

export function formObject(id : string = DEFAULT_FORM_ID) : HTMLFormElement {
	return document.forms[id];
}

export function formValues(id: string = DEFAULT_FORM_ID) : { [key: string]: string  } {
	const e = formObject(id);
	
	const out = {};
	
	function getValue(e: Element) {
		let name = '';
		switch (e.tagName.toLowerCase()) {
		case 'fieldset':
		case 'form':
		case 'div':
			Array.from(e.children).forEach(c => getValue(c));
			break;
		case 'input':
		case 'textarea':
		case 'select':
			name = (e as HTMLInputElement).name
			if (name) {
				out[name] = (e as HTMLInputElement).value;
			}
			break;
		}
	}
	
	getValue(e);
	return out;
}

export function form(contents: Element[], id: string, action? : '') : HTMLFormElement {
	id = id == undefined ? DEFAULT_FORM_ID : id;
	return create("form", {"class": "normform", "style": "background: #fff; ", "id": id, "action" : action }, contents) as HTMLFormElement;
}

export function fieldset(legend: string, contents: Node[], atts : object = {} ) : HTMLElement {
	return create("fieldset", atts, [ create("legend", {}, [ txt(legend) ] ), ...contents] )
}

/**
 * From https://stackoverflow.com/questions/1573053/javascript-function-to-convert-color-names-to-hex-codes
 */
export function hexColour(str: string | null) : string | null {
	if (str == undefined) {
		return undefined;
	}
    const ctx = document.createElement('canvas').getContext('2d');
    ctx.fillStyle = str;
    return ctx.fillStyle;
}

export function colour(placeholder:string, value: string, atts: object = {}) : HTMLElement {
	const id = idFrom(placeholder);
	const text = create('input', { 
		'class' : 'form-control', 
		'placeholder': 'default', 
		'type': 'text', 
		'value': value ? value : '', 
		'id': id, 
		'name': id, ...atts }) as HTMLInputElement;
	const patch = create('input', { 
		'class' : 'form-control', 
		'type': 'color', 
		...atts,
		'id' : id+"-patch" }) as HTMLInputElement;
	const label = create('label', {"for" : id}, [ txt(placeholder)]);
	const controlDiv = div({"class": "inline-buttons"}, [patch, text]);
	
	patch.addEventListener("input", () => 
		text.value = hexColour(patch.value)
	);

	return div({"class": ""}, [ label, controlDiv]);
}

export function text(placeholder:string, value: string, atts: object = {}) : HTMLElement {
	return input(placeholder, 'text', value, atts);
}

export function p(text: string, atts: object = {}) : HTMLElement {
	return create('p', atts, [ txt(text)]);
}

export function hidden(placeholder:string, value: string) : HTMLElement {
	const id = idFrom(placeholder);
	return create('input', {'type': 'hidden', 'value': value, 'id': id, 'name': id })
}

export function numeric(placeholder:string, value: number, atts: object = {}) : HTMLElement {
	return input(placeholder, 'number', ""+value, atts);
}

export function email(placeholder:string, value: string, atts: object = {}) : HTMLElement {
	return input(placeholder, 'email', value, atts);
}

export function password(placeholder:string, value: string, atts: object = {}) : HTMLElement {
	return input(placeholder, 'password', value, atts);
}

export function select(placeholder: string, value: string, atts: object = {}, options: string[]) : HTMLElement {
	const id = idFrom(placeholder);
	const selected = { "selected": "true" } 
	const unselected = {}
	
	function isSelected(o: string, i: number) {
		return (value != undefined) ? (value == o) : (i == 0);
	}
	
	return create('div', {}, [
		create('label', { "for" : id }, [ txt(placeholder) ]),
		create('div', { 'class' : 'select-dropdown' }, [
			create('select', {'id': id, 'name': id, ...atts}, 
				options.map((o, i) => create('option', isSelected(o, i) ? selected : unselected, [txt(o)])))
		])
	]);
	
}

export function formFields(content: Element[]) : HTMLElement {
	return create('div', {'class' : 'form-fields' }, content);
}

export function inlineButtons(buttons: Element[]) : HTMLElement {
	return create('div', {'class' : 'inline-buttons' }, buttons);
}

export function ok(placeholder : string, atts: object, callback: (e:Event) => void) : HTMLElement {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'submit', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	out.addEventListener('click', callback);
	return out;
}

export function cancel(placeholder : string, atts: object, callback: (e:Event) => void) : HTMLElement {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'reset', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	out.addEventListener('click', callback);
	return out;
}

export function submit(placeholder: string, atts: object) : HTMLElement {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'submit', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	return out;
}

export function div(atts: object = {}, contents? : Node[]) : HTMLElement {
	return create('div', {'class' : 'inline-buttons', ...atts }, contents);
}

export function checkbox(placeholder: string, value: string, atts: object) : HTMLElement {
	const id = idFrom(placeholder);
	return create("div", {}, [
		create('input', {...atts, 'type' : 'checkbox', 'id': id, 'value' : value}),
		create('label', {'for' : id}, [
			txt(placeholder)
		])
	]);
}

/*
export function radios(placeholder: string, value: string, atts: object = {}, options : string[]) : HTMLElement {
	const id = idFrom(placeholder);
	return create('div', {}, options.map(o => {
		const oid = idFrom(o);
		return create('div', {}, [
			create('input', {'type': 'radio', 'id': oid, 'name': id }),
			create('label', {'for': oid } , [ txt(o) ])
		])
	}));
}
*/

export function textarea(placeholder: string, value: string, atts: object = {}) : HTMLElement {
	const id = idFrom(placeholder);
	return create('div', {}, [
		create('label', { 'for' : id}, [ txt(placeholder) ]),
		create('textarea', {name: id, id: id, ...atts}, [ txt(value)])
	]);
}


function input(placeholder: string, type : string, value : string, atts : object = {}) : HTMLElement {
	const id = idFrom(placeholder);
	return create('div', {}, [
		create('label', {"for" : id}, [ txt(placeholder)]),
		create('input', { 'class' : 'form-control', 'placeholder': placeholder, 'type': type, 'value': value, 'id': id, 'name': id, ...atts })
	]);
}

function idFrom(str: string) : string {
	str = str.charAt(0).toLowerCase() + str.slice(1)
	return str.replace(/[^a-zA-Z0-9-]/g, '');
}

function txt(str: string) : Text {
	const e = document.createTextNode(str ? str : '');
	return e;
}

function create(tag: string, atts: object = {}, contents?: Node[]) : HTMLElement {
	
	const e = document.createElement(tag);
	const keys= Object.keys(atts)
	keys.forEach(k => {
		const val = atts[k];
		if (k.startsWith("on")) {
			const event = k.substring(2);
			e.addEventListener(event, val);
		} else {
			e.setAttribute(k, val);
		}
	});
	
	if (contents) {
		contents.forEach(c => e.appendChild(c));
	}
	
	return e;
}