/**
 * This contains basic kite9 classes that we can plug behaviours into, for manipulating ADL documents.
 */

import { Instrumentation } from "../../classes/instrumentation/instrumentation.js"
import { Metadata } from "../../classes/metadata/metadata.js"
import { ContextMenu } from "../../classes/context-menu/context-menu.js"
import { Transition } from '../../classes/transition/transition.js'
import { Dragger } from '../../classes/dragger/dragger.js'
import { Containment } from '../../classes/containment/containment.js'
import { Command, initCommandTransitionCallback } from '../../classes/command/command.js'
import { Palette } from '../../classes/palette/palette.js'
import { Overlay } from '../../classes/overlay/overlay.js'

//updatable
import { initMetadataBasedUpdater } from '../../behaviours/updatable/updatable.js'

// animation
import { initTransitionAnimationCallback } from '../../behaviours/animation/animation.js'

//identity
import { initIdentityInstrumentationCallback, identityMetadataCallback } from "../../behaviours/identity/identity.js"

//zoomable
import { zoomableInstrumentationCallback, zoomableTransitionCallback, initZoomable } from "../../behaviours/zoomable/zoomable.js"

//dragable
import { initDragable, initMainHoverableAllowed } from '../../behaviours/dragable/dragable.js' 

// actionable 
import { initActionable } from '../../behaviours/actionable/actionable.js' 

//hoverable
import { initHoverable } from '../../behaviours/hoverable/hoverable.js'

import { once } from '../../bundles/ensure.js'
import { getDocumentParam } from "../../bundles/api.js"

export const 
	command = new Command(), 
	metadata = new Metadata(), 
	transition = new Transition(), 
	instrumentation = new Instrumentation(), 
	dragger  = new Dragger(), 
	contextMenu = new ContextMenu(),
	containment = new Containment(),
	palette =  new Palette("_palette", getDocumentParam('palettes')),
	paletteContextMenu = new ContextMenu(),
	overlay = new Overlay();
	

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

	initHoverable(() => Array.from(palette.get().querySelectorAll("[k9-elem][id]"))); // init for palette

	initZoomable();

}

once(() => initCommon());