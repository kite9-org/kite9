

const PATH_PARSE = /([A-Za-z])([0-9\-\.\s]+)*/g;


export class Timeline {


	constructor(duration) {
		this.duration = duration;
		this.elements = [];
	}
	
	ease(x) {
		return (-Math.cos(x * Math.PI))/2 + .5;
	}

	perform(time) {
		if (this.startTime) {
			const fraction = (time - this.startTime) / this.duration;

			if (fraction <= 1) {
				const f2 = this.ease(fraction);
				console.log(fraction+" "+f2)
				this.elements.forEach(f => f(f2));
				requestAnimationFrame((x) => this.perform(x));
			} else {
				this.startTime = undefined;
				this.elements.forEach(f => f(1));
			}
		} else {
			this.startTime = time;
			requestAnimationFrame((x) => this.perform(x));
		}
	}

	play() {
		requestAnimationFrame(() => this.perform());
	}

	attribute(target, attribute, from, to) {
		this.elements.push(function(f) {
			const newVal = (to - from) * f + from;
			target.setAttribute(attribute, newVal);
		});
	}
	
	style(target, style, from, to) {
		this.elements.push(function(f) {
			const newVal = (to - from) * f + from;
			target.style[style] = newVal;			
		});
	}

	/**
	 * animates the transform for an element, using scale and translate only.
	 * Format of from, to is: { 
     *	translateX: 0,
     *	translateY: 0,
     *	scaleX: 1,
     *	scaleY: 1
     * };
	 */
	transform(target, from, to) {
		function interp(f, from, to) {
			return (to - from) * f + from;
		}
		
		this.elements.push(function(f) {
			const tx = interp(f, from.translateX, to.translateX);
			const ty = interp(f, from.translateY, to.translateY);
			const sx = interp(f, from.scaleX, to.scaleX);
			const sy = interp(f, from.scaleY, to.scaleY);
			
			const newTrans = (((tx != 0) || (ty != 0)) ? "translate("+tx+","+ty+") " : "")+
				(((sx != 1) || (sy != 1)) ? "scale("+sx+","+sy+") " : "");
			
			
			target.setAttribute("transform", newTrans);		
		});		
	}

	/**
	 * The path assumes that:
	 * - All parts are upper-case operations.
	 * - A path consists of a move (M) followed by a number of
	 * - L, Q steps, ending in an L.
	 */
	path(target, from, to) {
		
		function buildList(s) {
			return Array.from(s.matchAll(PATH_PARSE))
				.map(i => {
					return {
						"step" : i[1],
						"values" : i[2].trim().split(" ").map(s => parseFloat(s))	
					}
				});
		}
		
		const fromArray = buildList(from);
		const toArray = buildList(to);
		var fromI = 0;
		var toI = 0;
		
		const mapping = [];
		
		while ((fromI < fromArray.length) || (toI < toArray.length)) {
			const fromEl = fromI < fromArray.length ? fromArray[fromI] : fromArray[fromI-1];
			const toEl = toI < toArray.length ? toArray[toI] : toArray[toI-1];
			
			if (fromEl.step == toEl.step) {
				mapping.push({
					"step" : fromEl.step,
					"from" : fromEl.values,
					"to": toEl.values
				});
				fromI++;
				toI++;
			} else if (fromEl.step == 'Q') {
				mapping.push({
					"step" : fromEl.step,
					"from" : fromEl.values,
					"to": [ ...toEl.values, ...toEl.values ]
				});
				fromI++;
			} else if (toEl.step = 'Q') {
				mapping.push({
					"step" : toEl.step,
					"from" : [ ...fromEl.values, ...fromEl.values ],
					"to": toEl.values
				});
				toI++;
			}
		}
		
		function interp(f, e) {
			const numbers = e['from'].map((k, i) => {
				const j = e['to'][i];			
				const newVal = (j - k) * f + k;
				return newVal;
			})
			return e['step']+" "+numbers.reduce((a, b) => a+" "+b);
		}

		this.elements.push(function(f) {
			if (f == 0) {
				target.setAttribute("d", from);	
			} else if (f == 1) {
				target.setAttribute("d", to);	
			} else {
				const newPath = mapping
					.map(e => interp(f, e))
					.reduce((a, b) => a+" "+b);
				target.setAttribute("d", newPath);	
			}	
		});		
	}
}