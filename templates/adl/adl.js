/**
 * This contains basic kite9 classes that we can plug behaviours into, for manipulating ADL documents.
 */

import { Instrumentation } from "/github/kite9-org/kite9/client/classes/instrumentation/instrumentation.js?v=v0.5";
import { Metadata } from "/github/kite9-org/kite9/client/classes/metadata/metadata.js?v=v0.5";
import { ContextMenu } from "/github/kite9-org/kite9/client/classes/context-menu/context-menu.js?v=v0.5";
import { Transition } from '/github/kite9-org/kite9/client/classes/transition/transition.js?v=v0.5'
import { Dragger } from '/github/kite9-org/kite9/client/classes/dragger/dragger.js?v=v0.5'
import { Containment } from '/github/kite9-org/kite9/client/classes/containment/containment.js?v=v0.5'
import { Command, initCommandTransitionCallback } from '/github/kite9-org/kite9/client/classes/command/command.js?v=v0.5'
import { Palette, initPaletteHoverableAllowed } from '/github/kite9-org/kite9/client/classes/palette/palette.js?v=v0.5'

//updatable
import { initMetadataBasedUpdater } from '/github/kite9-org/kite9/behaviours/updatable/updatable.js?v=v0.5'

// animation
import { initTransitionAnimationCallback } from '/github/kite9-org/kite9/behaviours/animation/animation.js?v=v0.5'

//identity
import { initIdentityInstrumentationCallback, identityMetadataCallback } from "/github/kite9-org/kite9/behaviours/identity/identity.js?v=v0.5";

//zoomable
import { zoomableInstrumentationCallback, zoomableTransitionCallback, initZoomable } from "/github/kite9-org/kite9/behaviours/zoomable/zoomable.js?v=v0.5";

//dragable
import { initDragable, initMainHoverableAllowed } from '/github/kite9-org/kite9/behaviours/dragable/dragable.js?v=v0.5' 

// actionable 
import { initActionable } from '/github/kite9-org/kite9/behaviours/actionable/actionable.js?v=v0.5' 

//hoverable
import { initHoverable } from '/github/kite9-org/kite9/behaviours/hoverable/hoverable.js?v=v0.5'

import { once } from '/github/kite9-org/kite9/client/bundles/ensure.js?v=v0.5'

export const 
	command = new Command(), 
	metadata = new Metadata(), 
	transition = new Transition(), 
	instrumentation = new Instrumentation(), 
	dragger  = new Dragger(), 
	contextMenu = new ContextMenu(),
	containment = new Containment(),
	palette =  new Palette("_palette", document.params['palettes']);
	

function initCommon() {
	
	metadata.add(identityMetadataCallback);

	instrumentation.add(initIdentityInstrumentationCallback());
	instrumentation.add(zoomableInstrumentationCallback);

	transition.document(initCommandTransitionCallback(command));

	transition.animation(zoomableTransitionCallback);
	transition.animation(initTransitionAnimationCallback());

	command.add(initMetadataBasedUpdater(command, metadata, transition));

	dragger.moveWith(() => contextMenu.destroy());


	initDragable(dragger);
	initActionable(contextMenu);

	initHoverable(undefined, initMainHoverableAllowed());		// init for main svg area

	initHoverable(() => palette.get().querySelectorAll("[k9-elem][id]"), initPaletteHoverableAllowed(palette)); // init for palette

	initZoomable();

}

once(() => initCommon());