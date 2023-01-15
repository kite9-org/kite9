/**
 * Monika is a small in-situ integration testing library for javascript
 * event listeners. 
 */

type HandlerCoords = {
	name: string,
	type: string,
	target: EventTarget
}

type Result = {
	success: boolean,
	message?: string, 
	error?: Error;
}

/**
 * Adds an event handler to the target with information so that it can be retrieved again.
 */
export function addMonikaEventListener(et: EventTarget, type: string, name: string, eh: (event: Event) => void, options?: AddEventListenerOptions) {
	eh['monika'] = {
		name: name,
		type: type,
		target: et
	} as HandlerCoords;
	
	// keep track of the listeners on the object 
	const listeners = et['monika-listeners'] ?? {};
	
	// remove an old listener if one is set
	const oldListener = listeners[name];
	if (oldListener) {
		et.removeEventListener(type, oldListener);
	}
	listeners[name] = eh;
	et['monika-listeners'] = listeners;
	
	et.addEventListener(type, eh, options)
}


export function getMonikaEventListener(name: string, e: string | Element) : EventListener { 
	const elem = e instanceof Element ? e : document.getElementById(e);
	if (!elem) {
		throw new MonikaError(`${e} not in dom`);
	}
	const listeners = elem['monika-listeners'] ?? {};
	return listeners[name];
}

let results : Result[] = [];

export function describe(s: string, f: () => Promise<void>) : () => Promise<void> {
	return () => {
		results = []
		return it(s, f).then(() => {
			console.table(results);	
		});
	}
}

export function it(s: string, f: () => Promise<void>) : Promise<void> {
	return f().then(() => {
		results.push({success: true, message: s})
	}).catch((e) => {
		results.push({success: false, message: s, error: e});
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
		if (this.o !== x) {
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
