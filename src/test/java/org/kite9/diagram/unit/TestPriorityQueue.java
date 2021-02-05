package org.kite9.diagram.unit;

import org.junit.Assert;
import org.junit.Test;
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue;

import java.util.Random;

public class TestPriorityQueue {


    @Test
    public void testPriorityQueue1() {
        long t0 = System.currentTimeMillis();
        PriorityQueue<Integer> pq = new PriorityQueue<Integer>(1000, null);
        Random r = new Random();
        for (int i = 0; i < 10000; i++) {
            pq.add(r.nextInt());
        }

        int last = Integer.MIN_VALUE;

        for (int i = 0; i < 400; i++) {
            int out = pq.remove();
            Assert.assertTrue(out >= last);
            last = out;
        }

        for (int i = 0; i < 10000; i++) {
            pq.add(r.nextInt());
        }

        last = Integer.MIN_VALUE;
        Assert.assertEquals(20000 - 400, pq.size());
        while(pq.size() > 0) {
            int out = pq.remove();
            Assert.assertTrue(out >= last);
            last = out;
        }

        long elapsed = System.currentTimeMillis() - t0;
        Assert.assertTrue(elapsed < 1000);
        System.out.println("Elapsed: "+elapsed+" ms");
    }
}
