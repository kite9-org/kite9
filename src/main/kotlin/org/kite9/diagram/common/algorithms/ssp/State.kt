package org.kite9.diagram.common.algorithms.ssp

open class State<P : PathLocation<P>>(private val ssp: AbstractSSP<P>) {

	var pq = PriorityQueue<P>(10000)
    private val locationToPathMap: MutableMap<Any?, P> = HashMap(2000)
	private var adds: Long = 0
	private var maxStack: Long = 0

    fun add(path: P): Boolean {
        return try {
            adds++
            val location = ssp.getLocation(path)
            val existing =  locationToPathMap[location]
            val newBetter = existing == null || existing.compareTo(path) > 0
            if (newBetter) {
                existing?.setActive(false)
                //System.out.println("Replacing: \n\t "+existing+"\n\t"+path+"\n\t"+location);
                pq.add(path)
                locationToPathMap[location] = path
                maxStack = pq.size().toLong().coerceAtLeast(maxStack)
                true
            } else {
                //System.out.println("Not Adding: "+path);
                false
            }
        } catch (e: Exception) {
            throw SSPTooLargeException("SSP too large: queue=" + pq.size() + " map=" + locationToPathMap.size)
        }
    }

    open fun remove(): P {
        return pq.remove()!!
    }

    fun getAdds() : Long {
        return adds;
    }

    fun getMaxStack() : Long {
        return maxStack;
    }
 }