/*
 * Copyright 1997-2025 Optimatika
 */
package org.ojalgo.concurrent;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class MultiviewSetTest {

    @Test
    public void testPollSkipsStaleEntries() {
        MultiviewSet<Integer> set = new MultiviewSet<>();
        MultiviewSet<Integer>.PrioritisedView vNat = set.newView(naturalOrder());
        MultiviewSet<Integer>.PrioritisedView vRev = set.newView(reverseOrder());

        set.add(1);
        set.add(2);
        set.add(3);

        // Remove one entry from the backing set only; views still contain it as a stale element
        TestUtils.assertEquals(true, set.remove(2));

        // Poll from natural order view; should never return the removed element
        Integer a = vNat.poll();
        Integer b = vNat.poll();
        Integer c = vNat.poll();

        // We should get 1 and 3 in natural order, and then null
        TestUtils.assertEquals(Integer.valueOf(1), a);
        TestUtils.assertEquals(Integer.valueOf(3), b);
        TestUtils.assertTrue(c == null);

        // Backing set should now be empty after consuming remaining live elements
        TestUtils.assertEquals(0, set.size());

        // Other view also yields nothing now
        TestUtils.assertTrue(vRev.poll() == null);
    }

    @Test
    public void testPollFromOneViewRemovesGlobally() {
        MultiviewSet<Integer> set = new MultiviewSet<>();
        MultiviewSet<Integer>.PrioritisedView vNat = set.newView(naturalOrder());
        MultiviewSet<Integer>.PrioritisedView vRev = set.newView(reverseOrder());

        set.add(10);
        set.add(20);

        Integer first = vNat.poll(); // expect 10
        TestUtils.assertEquals(Integer.valueOf(10), first);

        // 10 is now removed from the backing set; reverse view should return the remaining live element (20)
        Integer second = vRev.poll();
        TestUtils.assertEquals(Integer.valueOf(20), second);

        // Both views now empty
        TestUtils.assertTrue(vNat.poll() == null);
        TestUtils.assertTrue(vRev.poll() == null);
        TestUtils.assertEquals(0, set.size());
    }

    @Test
    public void testClearClearsViews() {
        MultiviewSet<Integer> set = new MultiviewSet<>();
        MultiviewSet<Integer>.PrioritisedView vNat = set.newView(naturalOrder());
        MultiviewSet<Integer>.PrioritisedView vRev = set.newView(reverseOrder());

        set.add(1);
        set.add(2);

        set.clear();

        TestUtils.assertEquals(0, set.size());
        TestUtils.assertEquals(true, vNat.isEmpty());
        TestUtils.assertEquals(true, vRev.isEmpty());
    }
}