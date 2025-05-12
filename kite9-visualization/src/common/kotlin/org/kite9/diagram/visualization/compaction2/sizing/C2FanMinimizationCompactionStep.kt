//package org.kite9.diagram.visualization.compaction2.sizing
//
//import org.kite9.diagram.common.elements.Dimension
//import org.kite9.diagram.model.Connected
//import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
//import org.kite9.diagram.visualization.compaction2.C2Compaction
//import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
//import org.kite9.diagram.visualization.compaction2.C2Slideable
////import org.kite9.diagram.visualization.compaction2.anchors.FanAnchor
//import org.kite9.diagram.visualization.display.CompleteDisplayer
//import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
//
///**
// * In some sorted order, minimizes the size of Rectangular elements in the
// * diagram.
// *
// * @author robmoffat
// */
//class C2FanMinimizationCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {
//    override fun compact(c: C2Compaction, g: Group) {
//        size(c, Dimension.H)
//        size(c, Dimension.V)
//    }
//
//    private fun size(c2: C2Compaction, dimension: Dimension) {
//        val so = c2.getSlackOptimisation(dimension)
//        val largestMap = mutableMapOf<Connected, Pair<FanAnchor, C2Slideable>>()
//        val smallestMap = mutableMapOf<Connected, Pair<FanAnchor, C2Slideable>>()
//        so.getAllSlideables().forEach { s ->
//            s.getFanAnchors().forEach { f ->
//                val e = f.e
//                val existingLargest = largestMap[e]
//                val existingSmallest = smallestMap[e]
//                if ((existingLargest == null) || (existingLargest.first.s < f.s)) {
//                    largestMap[e] = Pair(f, s)
//                }
//
//                if ((existingSmallest == null) || (existingSmallest.first.s > f.s)) {
//                    smallestMap[e] = Pair(f, s)
//                }
//            }
//        }
//
//        largestMap.keys.forEach { c ->
//            val lm = largestMap[c]!!
//            val sm = smallestMap[c]!!
//            minimizeDistance(so, sm.second, lm.second)
//        }
//    }
//
//    private fun minimizeDistance(
//        opt: C2SlackOptimisation,
//        left: C2Slideable,
//        right: C2Slideable
//    ) {
//        val minDist = left.minimumDistanceTo(right) + 10
//        opt.ensureMaximumDistance(left,right, minDist)
//    }
//
//    override val prefix: String
//        get() = "CFMS"
//
//    override val isLoggingEnabled: Boolean
//        get() = true
//}