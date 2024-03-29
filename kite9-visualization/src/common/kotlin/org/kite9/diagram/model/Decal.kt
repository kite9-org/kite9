package org.kite9.diagram.model

/**
 * A decal is a decorative element that overlays it's parent [DiagramElement].
 * Decals are only processed during the display phase, as they have no effect on
 * Planarization or Orthogonalization, and don't take up any space of their own, so aren't
 * involved in Compaction.
 *
 * @author robmoffat
 */
interface Decal 