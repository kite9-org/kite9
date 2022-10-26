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

export function icon(id: string, title: string, image: string, onClick: (e: Event) => void, atts: object = {}): Element {
	const a = create('a', { 'class': 'hint--bottom hint--bounce icon', 'aria-label': title, 'id': id }, [
		create('img', { 'src': image, ...atts })
	]);
	a.addEventListener("click", onClick);
	return a;
}

export function largeIcon(id: string, title: string, image: string, onClick: (e: Event)=> void, atts: object = {}) : Element {
   const a = create('a', {'class': 'hint--bottom hint--bounce large-icon', 'aria-label': title, 'id': id}, [
     create('img', { 'src': image, ...atts})
   ]);
   a.addEventListener("click", onClick);
   return a;
}

export function img(id: string, src: string, atts: object = {}) : Element {
	return create('img', { 'id': id, 'src' : src, ...atts})
}

export function change(e: Element, f : (e: Event) => void) : Element {
	e.addEventListener("change", f);
	return e;
}

export function formObject(id : string) : Element {
	return id != undefined ? document.forms[id] : document.forms['no-form-id'];
}

export function formValues(id: string) : object {
	const e = formObject(id);
	
	const out = {};
	
	function getValue(e) {
		switch (e.tagName.toLowerCase()) {
		case 'fieldset':
		case 'form':
		case 'div':
			Array.from(e.children).forEach(c => getValue(c));
			break;
		case 'input':
		case 'textarea':
		case 'select':
			if (e.name) {
				out[e.name] = e.value;
			}
			break;
		}
	}
	
	getValue(e);
	return out;
}

export function form(contents: Element[], id: string, action: string) : Element {
	id = id == undefined ? 'no-form-id' : id;
	return create("form", {"class": "normform", "style": "background: #fff; ", "id": id, "action" : action }, contents);
}

export function fieldset(legend: string, contents: Node[], atts : object = {} ){
	return create("fieldset", atts, [ create("legend", {}, [ txt(legend) ] ), ...contents] )
}

export function colour(placeholder:string, value: string, atts: object = {}) : Element {
	const id = idFrom(placeholder);
	const text = create('input', { 
		'class' : 'form-control', 
		'placeholder': 'default', 
		'type': 'text', 
		'value': value, 
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
		text.value = patch.value
	);

	return div({"class": ""}, [ label, controlDiv]);
}

export function text(placeholder:string, value: string, atts: object = {}) : Element {
	return input(placeholder, 'text', value, atts);
}

export function p(text: string, atts: object) : Element {
	return create('p', atts, [ txt(text)]);
}

export function hidden(placeholder:string, value: string) : Element {
	const id = idFrom(placeholder);
	return create('input', {'type': 'hidden', 'value': value, 'id': id, 'name': id })
}

export function numeric(placeholder:string, value: string, atts: object = {}) : Element {
	return input(placeholder, 'number', value, atts);
}

export function email(placeholder:string, value: string, atts: object = {}) : Element {
	return input(placeholder, 'email', value, atts);
}

export function password(placeholder:string, value: string, atts: object = {}) : Element {
	return input(placeholder, 'password', value, atts);
}

export function select(placeholder: string, value: string, atts: object = {}, options: string[]) : Element {
	const id = idFrom(placeholder);
	return create('div', {}, [
		create('label', { "for" : id }, [ txt(placeholder) ]),
		create('div', { 'class' : 'select-dropdown' }, [
			create('select', {'id': id, 'name': id, ...atts}, 
				options.map((o, i) => create('option', 
						{'selected' : (value != undefined) ? (value==o) : (i==0) }, [txt(o)])))
		])
	]);
	
}

export function formFields(content: Element[]) : Element {
	return create('div', {'class' : 'form-fields' }, content);
}

export function inlineButtons(buttons: Element[]) : Element {
	return create('div', {'class' : 'inline-buttons' }, buttons);
}

export function ok(placeholder : string, atts: object, callback: (e:Event) => void) : Element {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'submit', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	out.addEventListener('click', callback);
	return out;
}

export function cancel(placeholder : string, atts: object, callback: (e:Event) => void) : Element {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'reset', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	out.addEventListener('click', callback);
	return out;
}

export function submit(placeholder: string, atts: object) : Element {
	const id = idFrom(placeholder);
	const out = create("input", {...atts, 'type': 'submit', 'name' : id, 'id' : id, 'value': placeholder}, [ txt(placeholder)]);
	return out;
}

export function div(atts: object = {}, contents? : Node[]) : Element {
	return create('div', {'class' : 'inline-buttons', ...atts }, contents);
}

export function checkbox(placeholder: string, value: string, atts: object) : Element {
	const id = idFrom(placeholder);
	return create("div", {}, [
		create('input', {...atts, 'type' : 'checkbox', 'id': id, 'value' : value}),
		create('label', {'for' : id}, [
			txt(placeholder)
		])
	]);
}

export function radios(placeholder: string, value: string, atts: object = {}, options : string[]) : Element {
	const id = idFrom(placeholder);
	return create('div', {}, options.map(o => {
		const oid = idFrom(o);
		return create('div', {}, [
			create('input', {'type': 'radio', 'id': oid, 'name': id }),
			create('label', {'for': oid } , [ txt(o) ])
		])
	}));
}

export function textarea(placeholder: string, value: string, atts: object = {}) : Element {
	const id = idFrom(placeholder);
	return create('div', {}, [
		create('label', { 'for' : id}, [ txt(placeholder) ]),
		create('textarea', {name: id, id: id, ...atts}, [ txt(value)])
	]);
}


function input(placeholder: string, type : string, value : string, atts : object = {}) : Element {
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

function txt(str) : Text {
	const e = document.createTextNode(str ? str : '');
	return e;
}

function create(tag: string, atts: object = {}, contents?: Node[]) : Element {
	
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