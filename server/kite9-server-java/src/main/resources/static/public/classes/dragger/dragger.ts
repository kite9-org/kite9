import { getSVGCoords, getMainSvg, currentTarget } from '../../bundles/screen.js'
import { handleTransformAsStyle, getKite9Target, getParentElement, onlyUnique, getNextSiblingId, isLink, isConnected } from '../../bundles/api.js'

export type DropLocatorFunction = (dragTarget: Element, dropTarget: Element) => boolean
export type DropLocatorCallback = (dragTargets : Element[], target: Element) => Element[]
export type DragLocatorCallback = (e: Event) => Element[]
export type MoveCallback = (dragTargets : Element[], e?: Event, dropTargets?: Element[]) => void
export type DropCallback = (state: StateItem[], e: Event, dropTargets: Element[]) => void
export type StateItem = { 
	dragTarget: Element, 
	dragParent: Element, 
	dragParentId: string, 
	dragBefore? : Element, 
	dragBeforeId?: string,
	embeddedShapeOrigin: {x : number, y: number},
	url?: string
}

/**
 * Manages state while dragging, as well as maintaining a _moveLayer <g> which holds all the elements
 * being dragged.
 * 
 * - moveCallbacks:  function called to notify of a move
 * - dropcallbacks:  function called to notify of a drop
 * - dragLocators: given a mouse event, figure out what is being dragged (or []).
 * - dropLocators: given a mouse event, return the drop target (or null).  Earlier calls take priority.
 */
export class Dragger {

	// where the dragging happens
	svg = null;

	// keeps track of the current drag
	dragOrigin = null;
	state : StateItem[] | null = null;
	shapeLayer = null;
	mouseDown = false;
	delta = null;
	dropTargets = [];
	draggingWithButtonDown = true;

	// plugged-in functionality.
	moveCallbacks : MoveCallback[] = []; 
	dropCallbacks : DropCallback[] = []; 
	dropLocators : DropLocatorCallback[] = []; 
	dragLocators : DragLocatorCallback[] = []; 
	
	constructor() {
		this.svg = getMainSvg();
	}	
	
	moveWith(cb: MoveCallback) {
		this.moveCallbacks.push(cb);
	}
	
	dropWith(cb : DropCallback) {
		// nb: additions are added to front of array
		this.dropCallbacks.unshift(cb);
	}
	
	dragLocator(cb : DragLocatorCallback) {
		this.dragLocators.push(cb);
	}
	
	dropLocator(cb : DropLocatorCallback) {
		this.dropLocators.push(cb);
	}

	getTranslate(dt : Element) {
		if (dt == this.svg) {
			return {x: 0, y: 0};
		}
		
		const transMatrix = (dt as SVGGraphicsElement).getCTM();

		const out = {
				x: Number(transMatrix.e), 
				y: Number(transMatrix.f)
			};
		
		//console.log(dt+" "+out);
		return out;
	}

	getDifferentialTranslate(dt : Element) {
		const transMatrix = this.getTranslate(dt as SVGGraphicsElement);
		const parentMatrix = this.getTranslate(dt.parentElement);
		
		const out = {
			x: transMatrix.x - parentMatrix.x, 
			y: transMatrix.y - parentMatrix.y
		}
		
		return out;
	}

	
	initMoveState(out, evt : Event) {
		if (out.length > 0) {
			this.dragOrigin = getSVGCoords(evt);
			
			// make sure the order of the state is such that we don't run 
			// into trouble with insertBefore.
			
			let keepGoing = true;
			while (keepGoing) {
				keepGoing = false;
				const ordered = out.map(s => s.dragTarget);
				for (let i = 0; i < out.length; i++) {
					const antecedent = ordered.indexOf(out[i].dragBefore);
					if (antecedent > i) {
						const removed = out.splice(antecedent, 1);
						out.unshift(removed[0]);
						keepGoing = true;
					} 
				}
			}
			
			this.state = out;
		}
		
	}
	
	beginMove(evt : Event, draggingWithButtonDown = true) {
		this.draggingWithButtonDown = draggingWithButtonDown;
		const elements = this.dragLocators.flatMap(dl => dl(evt));
		const uniqueElements = [...new Set(elements)];
		const out = this.beginMoveWithElements(uniqueElements);
		this.initMoveState(out, evt);
	}
	
	beginAdd(elementToUrlMap: Map<Element, string>, evt : Event) {
		const uniqueElements = Array.from(elementToUrlMap.keys());
		const out = this.beginMoveWithElements(uniqueElements);
		out.forEach(s => {
			const url = elementToUrlMap.get(s.dragTarget);
			s.url = url;
		})
		this.initMoveState(out, evt);
	}
		
	beginMoveWithElements(uniqueElements : Element[]) {
		const out = []
			
		uniqueElements.forEach(e => {
			handleTransformAsStyle(e as SVGElement);
			
			if (this.shapeLayer == null) {
				this.shapeLayer = document.createElementNS("http://www.w3.org/2000/svg", "g");
				this.shapeLayer.setAttributeNS(null, 'pointer-events', 'none');
				this.shapeLayer.setAttribute("id", "_moveLayer");
				this.svg.appendChild(this.shapeLayer);
			}
			
			const shapeOrigin = this.getTranslate(e);
			const embeddedShapeOrigin = this.getDifferentialTranslate(e);
			const parent = getParentElement(e);
			
			out.push({
				dragTarget: e,
				dragParent: e.parentElement,
				dragParentId: parent == undefined ? undefined : parent.getAttribute("id"),
				dragBefore: e.nextSibling != this.shapeLayer ? e.nextSibling : null,
				dragBeforeId: getNextSiblingId(e),
				shapeOrigin: shapeOrigin,
				embeddedShapeOrigin: embeddedShapeOrigin
			})
			
			this.shapeLayer.appendChild(e);
			e.setAttributeNS(null, 'pointer-events', 'none');
			(e as SVGElement).style.setProperty('transform', 'translateX(' + shapeOrigin.x + 'px) translateY('
					+ shapeOrigin.y + 'px)');
		});
		
		return out;
	}

	grab() {
		this.mouseDown = true;
		this.draggingWithButtonDown = true;
	}

	drag(evt : MouseEvent) {
		if (this.draggingWithButtonDown) {
			if (!this.mouseDown) {
				return;
			}
		}
		
		if (!this.state) {
			if (evt.buttons == 1) {
				this.beginMove(evt);
			}
		}
		
		if (this.state) {
			// calculate move in true coords
			const trueCoords = getSVGCoords(evt);
			this.delta = {
					x:  trueCoords.x - this.dragOrigin.x, 
					y:  trueCoords.y - this.dragOrigin.y
			};

			// apply a new tranform translation to the dragged element, to display
			//    it in its new location
			this.shapeLayer.style.setProperty('transform', 'translateX(' + this.delta.x + 'px) translateY('
					+ this.delta.y + 'px)');
			
			const dragTargets = this.state.map(s => s.dragTarget)
			
			let newDropTargets = [];
			const target = currentTarget(evt);
            
			this.dropLocators.forEach(dl => {
				newDropTargets = newDropTargets.concat(dl(dragTargets, target));
			})
			
			this.updateDropTargets(newDropTargets);

			this.moveCallbacks.forEach(mc => mc(dragTargets, evt, this.dropTargets));

			// prevents touch devices interpreting this as a call to move the screen around
			evt.preventDefault();
		} 
		
	}
	
	updateDropTargets(newDropTargets : SVGGraphicsElement[]) {
		
		newDropTargets = newDropTargets.filter(onlyUnique);
		
		this.dropTargets
			.filter(t => newDropTargets.indexOf(t) == -1)
			.forEach(t => t.classList.remove("dropping"));
		
		newDropTargets
			.filter(t => this.dropTargets.indexOf(t) == -1)
			.forEach(t => t.classList.add("dropping"));
		
		if (newDropTargets.length > 0) {
			this.svg.style.cursor = "grabbing";
		} else {
			this.svg.style.cursor = "not-allowed";
		}
		
		this.dropTargets = newDropTargets;
	}

	endMove(reset : boolean) {
		if (this.state) {
			const dragTargets = this.state.map(s => s.dragTarget)

			while (this.state.length > 0) {
				for (let i = 0; i<this.state.length; i++) {
					const s = this.state[i];
					
					if ((s.dragParentId == undefined) && (reset)) {
						s.dragTarget.parentElement.removeChild(s.dragTarget);
						this.state.splice(i, 1);
					} else if ((s.dragParent.contains(s.dragBefore)) || (s.dragBefore==null)) {
						s.dragParent.insertBefore(s.dragTarget, s.dragBefore);
						
						const x = s.embeddedShapeOrigin.x + ( reset ? 0 : this.delta.x );
						const y = s.embeddedShapeOrigin.y + ( reset ? 0 : this.delta.y );
						
						(s.dragTarget as SVGElement).style.setProperty('transform', 'translateX(' + x + 'px) translateY('
								+ y + 'px)');
						s.dragTarget.setAttributeNS(null, 'pointer-events', 'all');
						this.state.splice(i, 1);
					} 

				}
			}
			
			if (reset) {
				// this allows the move callbacks to clean up, if need be
				this.moveCallbacks.forEach(mc => mc(dragTargets));
			}
			
			this.state = null;
			if (this.svg.contains(this.shapeLayer)) {
				this.svg.removeChild(this.shapeLayer);
			}
			this.svg.style.cursor = null;
			this.shapeLayer = null;
			this.dragOrigin = null;
			this.dropTargets.forEach(t => t.classList.remove("dropping"));
		}
	}
	

	drop(evt : MouseEvent) {
		// if we aren't currently dragging an element, don't do anything
		if (this.state) {
			if (this.dropTargets.length > 0) {
				this.dropCallbacks
					.forEach(dc => dc(this.state, evt, this.dropTargets))
				this.endMove(false);
				this.dropTargets = [];
			} else {
				this.endMove(true);			
			}
		}
		
		this.mouseDown = false;
		this.draggingWithButtonDown = true;
		this.svg.style.cursor = undefined;
	}	
	
	/**
	 * This function provides some common behaviour for drop
	 * locators, so that you just supply canDropHere(dragTarget, dropTarget).
	 * It worries about handling the fact that you can multi-drag.
	 */
	dropLocatorFn(canDropHere : DropLocatorFunction) {
		
		let lastDropTarget = null;
		let lastDragIds = null;
		
		function dragIds(dragTargets : Element[]) : string {
			return dragTargets
				.map(dt => dt.getAttribute("id"))
				.reduce((a,b) => a+","+b, "");
		}

		const fn = function(dragTargets : Element[], target: Element) : Element[] {
			let dropTarget = getKite9Target(target);

			if (isLink(dropTarget)) {

				// if the drag targets are the same as before, we may be
				// hovering over a link, so keep dropTarget the same.
				if (lastDragIds == dragIds(dragTargets)) {
					return [ lastDropTarget ]
				} 
				
			} else {
				while (dropTarget) {
					const ok = dragTargets
						.map(dt => canDropHere(dt, dropTarget))
						.reduce((a,b) => a&&b, true);
				
					if (ok) {
						lastDragIds = dragIds(dragTargets);
						lastDropTarget = dropTarget;
						return [ dropTarget ];
					} else if (isConnected(dropTarget)) {
						dropTarget = getParentElement(dropTarget);
					} else {
						dropTarget = null;
					}
				}
			}
			
			
			return [ ];
		}	
		
		this.dropLocator(fn);
	}
	
}




