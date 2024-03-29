/**
 * Monika is a small in-situ integration testing library for javascript
 * event listeners. 
 */
 
type EventHandler = (event: Event) => void
 
type NamedEventHandlers = { [key: string] : EventHandler }

type Result = {
	symbol: string,
	message?: string, 
	error?: string;
}

/*
 * stores all the declared handlers for objects in scope, releasing references
 * to objects that are deleted from the DOM
 */
const _declaredHandlers = new WeakMap<EventTarget, NamedEventHandlers>();

/**
 * Adds an event handler to the target with information so that it can be retrieved again.
 */
export function addNamedEventListener(et: EventTarget, type: string, name: string, eh: EventHandler, options?: AddEventListenerOptions) {

	// keep track of the listeners on the object 
	const listeners : NamedEventHandlers = _declaredHandlers.get(et) ?? {};
	
	// remove an old listener if one is set
	const oldListener = listeners[name];
	if (oldListener) {
		et.removeEventListener(type, oldListener);
	}
	listeners[name] = eh;
	_declaredHandlers.set(et, listeners);
	
	et.addEventListener(type, eh, options)
}


export function getNamedEventListener(name: string, e: string | Element | Document) : EventListener { 
	const elem = (e instanceof Element) || (e instanceof Document) ? e : document.getElementById(e);
	if (!elem) {
		throw new MonikaError(`${e} not in dom`);
	}
	const listeners : NamedEventHandlers = _declaredHandlers.get(elem) ?? {};
	return listeners[name];
}

let results : Result[] = [];

export function describe(s: string, f: () => Promise<void>) : () => Promise<void> {
	return async () => {
		results = []
		return it(s, f).then(() => {
			console.table(results);	
		});
	}
}

export async function it(s: string, f: () => Promise<void>) : Promise<void> {
	console.log("Testing: "+s);
	return f().then(() => {
		results.push({symbol: '✅', message: s, error: ''})
	}).catch((e) => {
		results.push({symbol: '❌', message: s, error: e.message});
		console.log(e);
	});
}

class AssertionError extends Error {
  constructor(message: string) {
    super(message);
    this.name = this.constructor.name;
  }
}

class MonikaError extends Error {
  constructor(message: string) {
    super(message);
    this.name = this.constructor.name;
  }
}

class Expectation {

	o: unknown

	constructor(i: unknown) {
		this.o = i;
	}

	toEqual(x: unknown) {
		if (!isEqual(this.o,x)) {
			throw new AssertionError(`${this.o} not equal to ${x}`);
		}
	}
	
	isNotNull() {
		if (this.o == null) {
			throw new AssertionError(`${this.o} is not expected to be null`);
		}
	}
}


export function expect(o: unknown) : Expectation {
	return new Expectation(o);
}

/**
 * Taken from https://gomakethings.com/check-if-two-arrays-or-objects-are-equal-with-javascript/
 */
function isEqual(value, other) {

	// Get the value type
	const type = Object.prototype.toString.call(value);

	// If the two objects are not the same type, return false
	if (type !== Object.prototype.toString.call(other)) return false;

	// If items are not an object or array, return false
	if (['[object Array]', '[object Object]'].indexOf(type) < 0) return false;

	// Compare the length of the length of the two items
	const valueLen = type === '[object Array]' ? value.length : Object.keys(value).length;
	const otherLen = type === '[object Array]' ? other.length : Object.keys(other).length;
	if (valueLen !== otherLen) return false;

	// Compare two items
	const compare = function (item1, item2) {

		// Get the object type
		const itemType = Object.prototype.toString.call(item1);

		// If an object or array, compare recursively
		if (['[object Array]', '[object Object]'].indexOf(itemType) >= 0) {
			if (!isEqual(item1, item2)) return false;
		}

		// Otherwise, do a simple comparison
		else {

			// If the two items are not the same type, return false
			if (itemType !== Object.prototype.toString.call(item2)) return false;

			// Else if it's a function, convert to a string and compare
			// Otherwise, just compare
			if (itemType === '[object Function]') {
				if (item1.toString() !== item2.toString()) return false;
			} else {
				if (item1 !== item2) return false;
			}

		}
	};

	// Compare properties
	if (type === '[object Array]') {
		for (let i = 0; i < valueLen; i++) {
			if (compare(value[i], other[i]) === false) return false;
		}
	} else {
		for (const key in value) {
			if (Object.prototype.hasOwnProperty.call(value, key)) {
				if (compare(value[key], other[key]) === false) return false;
			}
		}
	}

	// If nothing failed, return true
	return true;

}
