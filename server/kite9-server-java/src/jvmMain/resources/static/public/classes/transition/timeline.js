const PATH_PARSE = /([A-Za-z])([0-9\-.\s]+)*/g;
/**
 * Timeline is used to tweening between two different diagram states.
 */
export class Timeline {
    constructor(duration) {
        this.duration = duration;
        this.elements = [];
    }
    ease(x) {
        return (-Math.cos(x * Math.PI)) / 2 + .5;
    }
    perform(time) {
        if (this.startTime) {
            const fraction = (time - this.startTime) / this.duration;
            if (fraction <= 1) {
                const f2 = this.ease(fraction);
                this.elements.forEach(f => f(f2));
                requestAnimationFrame((x) => this.perform(x));
            }
            else {
                this.startTime = undefined;
                this.elements.forEach(f => f(1));
            }
        }
        else {
            this.startTime = time;
            requestAnimationFrame((x) => this.perform(x));
        }
    }
    play() {
        requestAnimationFrame(() => this.perform());
    }
    /**
     * Animates an attribute
     */
    attribute(target, attribute, from, to, suffix = '') {
        this.elements.push(function (f) {
            const newVal = (to - from) * f + from;
            target.setAttribute(attribute, newVal + suffix);
        });
    }
    /**
     * Animates a css style in the style declaration.
     */
    style(target, style, from, to, suffix = '') {
        this.elements.push(function (f) {
            const newVal = (to - from) * f + from;
            target.style[style] = newVal + suffix;
        });
    }
    /**
     * animates the transform for an element, using scale and translate only.
     */
    transform(target, from, to) {
        function interp(f, from, to) {
            return (to - from) * f + from;
        }
        this.elements.push(function (f) {
            const tx = interp(f, from.translateX, to.translateX);
            const ty = interp(f, from.translateY, to.translateY);
            const sx = interp(f, from.scaleX, to.scaleX);
            const sy = interp(f, from.scaleY, to.scaleY);
            const newTrans = (tx != 0 ? "translateX(" + tx + "px) " : "") +
                (ty != 0 ? "translateY(" + ty + "px) " : "") +
                (sx != 1 ? "scaleX(" + sx + ") " : "") +
                (sy != 1 ? "scaleY(" + sy + ") " : "");
            target.style["transform"] = newTrans;
        });
    }
    /**
     * The path assumes that:
     * - All parts are upper-case operations.
     * - A path consists of a move (M) followed by a number of
     * - L, (Q/A) steps, ending in an L.
     */
    path(target, from, to) {
        function buildList(s) {
            return Array.from(s.matchAll(PATH_PARSE))
                .map(i => {
                return {
                    "step": i[1],
                    "values": i[2].trim().split(" ").map(s => parseFloat(s))
                };
            });
        }
        // arc changes from clockwise to anti
        function specialArcCase(fromEl, toEl) {
            return (fromEl.step == 'A') && (toEl.step == 'A') && (fromEl.values[4] != toEl.values[4]);
        }
        function lastTwo(array) {
            return array.slice(-2);
        }
        const fromArray = buildList(from);
        const toArray = buildList(to);
        let fromI = 0;
        let toI = 0;
        let lastFrom, lastTo;
        const mapping = [];
        while ((fromI < fromArray.length) || (toI < toArray.length)) {
            const fromEl = fromI < fromArray.length ? fromArray[fromI] : fromArray[fromArray.length - 1];
            const toEl = toI < toArray.length ? toArray[toI] : toArray[toArray.length - 1];
            if ((fromEl.step == toEl.step) && (!specialArcCase(fromEl, toEl))) {
                mapping.push({
                    "step": fromEl.step,
                    "from": fromEl.values,
                    "to": toEl.values
                });
                fromI++;
                toI++;
                lastFrom = lastTwo(fromEl.values);
                lastTo = lastTwo(toEl.values);
            }
            else if (fromEl.step == 'Q') {
                // removing the q
                mapping.push({
                    "step": fromEl.step,
                    "from": fromEl.values,
                    "to": [...lastTo, ...lastTo]
                });
                fromI++;
                lastFrom = lastTwo(fromEl.values);
            }
            else if (toEl.step == 'Q') {
                // adding the q
                mapping.push({
                    "step": toEl.step,
                    "from": [...lastTwo(fromEl.values), ...lastTwo(fromEl.values)],
                    "to": toEl.values
                });
                toI++;
                lastTo = lastTwo(toEl.values);
            }
            else if (fromEl.step == 'A') {
                // removing the a
                mapping.push({
                    "step": fromEl.step,
                    "from": fromEl.values,
                    "to": [0, 0, 0, 0, fromEl.values[4], ...lastTo]
                });
                lastFrom = lastTwo(fromEl.values);
                fromI++;
            }
            else if (toEl.step == 'A') {
                // adding the a
                mapping.push({
                    "step": toEl.step,
                    "from": [0, 0, 0, 0, toEl.values[4], ...lastFrom],
                    "to": toEl.values
                });
                lastTo = lastTwo(toEl.values);
                toI++;
            }
        }
        function interp(f, e) {
            const numbers = e['from'].map((k, i) => {
                const j = e['to'][i];
                const newVal = (j - k) * f + k;
                return newVal;
            });
            return e['step'] + " " + numbers.reduce((a, b) => a + " " + b);
        }
        this.elements.push(function (f) {
            if (f == 0) {
                target.setAttribute("d", from);
            }
            else if (f == 1) {
                target.setAttribute("d", to);
            }
            else {
                const newPath = mapping
                    .map(e => interp(f, e))
                    .reduce((a, b) => a + " " + b);
                target.setAttribute("d", newPath);
            }
        });
    }
}
