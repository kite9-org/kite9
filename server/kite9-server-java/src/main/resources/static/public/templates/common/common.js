/**
 * This contains basic kite9 classes that we can plug behaviours into.
 *  
 */

import { Instrumentation } from "/public/classes/instrumentation/instrumentation.js";
import { Metadata, initMetadataTransitionCallback } from "/public/classes/metadata/metadata.js";
import { ContextMenu } from "/public/classes/context-menu/context-menu.js";
import { Transition } from '/public/classes/transition/transition.js';
import { Dragger } from '/public/classes/dragger/dragger.js';
import { Containment } from '/public/classes/containment/containment.js';
import { Command, initCommandTransitionCallback } from '/public/classes/command/command.js';
import { Palette, initPaletteHoverableAllowed } from '/public/classes/palette/palette.js';

// animation
import { initTransitionAnimationCallback } from '/public/behaviours/animation/animation.js';

//identity
import { initIdentityInstrumentationCallback, identityMetadataCallback } from "/public/behaviours/identity/identity.js";

//zoomable
import { zoomableInstrumentationCallback, zoomableTransitionCallback, initZoomable } from "/public/behaviours/zoomable/zoomable.js";

//dragable
import { initDragable, initMainHoverableAllowed } from '/public/behaviours/dragable/dragable.js' 

// actionable 
import { initActionable } from '/public/behaviours/actionable/actionable.js' 

//hoverable
import { initHoverable } from '/public/behaviours/hoverable/hoverable.js';

import { once } from '/public/bundles/ensure.js';

export const 
	command = new Command(), 
	metadata = new Metadata(), 
	
	transition = new Transition(
			() => metadata.get('self'),	
			() => metadata.get('topic')), 
			
	instrumentation = new Instrumentation(), 
	
	dragger  = new Dragger(), 
	
	contextMenu = new ContextMenu(),
	
	containment = new Containment(),

    palette =  new Palette("_palette", document.params['palettes']);
	

function initCommon() {
	
	metadata.add(identityMetadataCallback);
	
	instrumentation.add(initIdentityInstrumentationCallback());
	instrumentation.add(zoomableInstrumentationCallback);
	
	transition.document(initMetadataTransitionCallback(metadata));
	transition.document(initCommandTransitionCallback(command));
  
  transition.animation(zoomableTransitionCallback);
  transition.animation(initTransitionAnimationCallback());
	
	command.add((update) => transition.update(update));
		
	dragger.moveWith(() => contextMenu.destroy());

	
	initDragable(dragger); 
	initActionable(contextMenu);

	initHoverable(undefined, initMainHoverableAllowed());		// init for main svg area
  initHoverable(() => palette.get().querySelectorAll("[k9-elem][id]"), initPaletteHoverableAllowed(palette)); // init for palette

	initZoomable();

}

once(() => initCommon());