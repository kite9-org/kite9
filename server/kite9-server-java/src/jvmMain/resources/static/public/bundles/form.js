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
import { ensureCss } from './ensure.js';
ensureCss('/public/external/normform-2.0.css');
ensureCss('/webjars/hint.css/2.3.2/hint.css');
export const DEFAULT_FORM_ID = 'default-form';
export function icon(id, title, image, onClick = undefined, atts = {}) {
    const a = create('a', { 'class': 'hint--bottom hint--bounce icon', 'aria-label': title, 'id': id }, [
        create('img', { 'src': image, ...atts })
    ]);
    if (onClick) {
        a.addEventListener("click", onClick);
    }
    return a;
}
export function largeIcon(id, title, image, onClick, atts = {}) {
    const a = create('a', { 'class': 'hint--bottom hint--bounce large-icon', 'aria-label': title, 'id': id }, [
        create('img', { 'src': image, ...atts })
    ]);
    a.addEventListener("click", onClick);
    return a;
}
export function img(id, src, atts = {}) {
    return create('img', { 'id': id, 'src': src, ...atts });
}
export function change(e, f) {
    e.addEventListener("change", f);
    return e;
}
export function formObject(id = DEFAULT_FORM_ID) {
    return document.forms[id];
}
export function formValues(id = DEFAULT_FORM_ID) {
    const e = formObject(id);
    const out = {};
    function getValue(e) {
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
                name = e.name;
                if (name) {
                    out[name] = e.value;
                }
                break;
        }
    }
    getValue(e);
    return out;
}
export function form(contents, id, action) {
    id = id == undefined ? DEFAULT_FORM_ID : id;
    return create("form", { "class": "normform", "style": "background: #fff; ", "id": id, "action": action }, contents);
}
export function fieldset(legend, contents, atts = {}) {
    return create("fieldset", atts, [create("legend", {}, [txt(legend)]), ...contents]);
}
/**
 * From https://stackoverflow.com/questions/1573053/javascript-function-to-convert-color-names-to-hex-codes
 */
export function hexColour(str) {
    if (str == undefined) {
        return undefined;
    }
    const ctx = document.createElement('canvas').getContext('2d');
    ctx.fillStyle = str;
    return ctx.fillStyle;
}
export function colour(placeholder, value, atts = {}) {
    const id = idFrom(placeholder);
    const text = create('input', {
        'class': 'form-control',
        'placeholder': 'default',
        'type': 'text',
        'value': value ? value : '',
        'id': id,
        'name': id, ...atts
    });
    const patch = create('input', {
        'class': 'form-control',
        'type': 'color',
        ...atts,
        'id': id + "-patch"
    });
    const label = create('label', { "for": id }, [txt(placeholder)]);
    const controlDiv = div({ "class": "inline-buttons" }, [patch, text]);
    patch.addEventListener("input", () => text.value = hexColour(patch.value));
    return div({ "class": "" }, [label, controlDiv]);
}
export function text(placeholder, value, atts = {}) {
    return input(placeholder, 'text', value, atts);
}
export function p(text, atts = {}) {
    return create('p', atts, [txt(text)]);
}
export function hidden(placeholder, value) {
    const id = idFrom(placeholder);
    return create('input', { 'type': 'hidden', 'value': value, 'id': id, 'name': id });
}
export function numeric(placeholder, value, atts = {}) {
    return input(placeholder, 'number', value, atts);
}
export function email(placeholder, value, atts = {}) {
    return input(placeholder, 'email', value, atts);
}
export function password(placeholder, value, atts = {}) {
    return input(placeholder, 'password', value, atts);
}
export function select(placeholder, value, atts = {}, options) {
    const id = idFrom(placeholder);
    const selected = { "selected": "true" };
    const unselected = {};
    function isSelected(o, i) {
        return (value != undefined) ? (value == o) : (i == 0);
    }
    return create('div', {}, [
        create('label', { "for": id }, [txt(placeholder)]),
        create('div', { 'class': 'select-dropdown' }, [
            create('select', { 'id': id, 'name': id, ...atts }, options.map((o, i) => create('option', isSelected(o, i) ? selected : unselected, [txt(o)])))
        ])
    ]);
}
export function formFields(content) {
    return create('div', { 'class': 'form-fields' }, content);
}
export function inlineButtons(buttons) {
    return create('div', { 'class': 'inline-buttons' }, buttons);
}
export function ok(placeholder, atts, callback) {
    const id = idFrom(placeholder);
    const out = create("input", { ...atts, 'type': 'submit', 'name': id, 'id': id, 'value': placeholder }, [txt(placeholder)]);
    out.addEventListener('click', callback);
    return out;
}
export function cancel(placeholder, atts, callback) {
    const id = idFrom(placeholder);
    const out = create("input", { ...atts, 'type': 'reset', 'name': id, 'id': id, 'value': placeholder }, [txt(placeholder)]);
    out.addEventListener('click', callback);
    return out;
}
export function submit(placeholder, atts) {
    const id = idFrom(placeholder);
    const out = create("input", { ...atts, 'type': 'submit', 'name': id, 'id': id, 'value': placeholder }, [txt(placeholder)]);
    return out;
}
export function div(atts = {}, contents) {
    return create('div', { 'class': 'inline-buttons', ...atts }, contents);
}
export function checkbox(placeholder, value, atts) {
    const id = idFrom(placeholder);
    return create("div", {}, [
        create('input', { ...atts, 'type': 'checkbox', 'id': id, 'value': value }),
        create('label', { 'for': id }, [
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
export function textarea(placeholder, value, atts = {}) {
    const id = idFrom(placeholder);
    return create('div', {}, [
        create('label', { 'for': id }, [txt(placeholder)]),
        create('textarea', { name: id, id: id, ...atts }, [txt(value)])
    ]);
}
function input(placeholder, type, value, atts = {}) {
    const id = idFrom(placeholder);
    return create('div', {}, [
        create('label', { "for": id }, [txt(placeholder)]),
        create('input', { 'class': 'form-control', 'placeholder': placeholder, 'type': type, 'value': value, 'id': id, 'name': id, ...atts })
    ]);
}
function idFrom(str) {
    str = str.charAt(0).toLowerCase() + str.slice(1);
    return str.replace(/[^a-zA-Z0-9-]/g, '');
}
function txt(str) {
    const e = document.createTextNode(str ? str : '');
    return e;
}
function create(tag, atts = {}, contents) {
    const e = document.createElement(tag);
    const keys = Object.keys(atts);
    keys.forEach(k => {
        const val = atts[k];
        if (k.startsWith("on")) {
            const event = k.substring(2);
            e.addEventListener(event, val);
        }
        else {
            e.setAttribute(k, val);
        }
    });
    if (contents) {
        contents.forEach(c => e.appendChild(c));
    }
    return e;
}
