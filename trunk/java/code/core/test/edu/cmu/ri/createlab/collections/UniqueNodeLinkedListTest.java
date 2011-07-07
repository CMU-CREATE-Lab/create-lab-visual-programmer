package edu.cmu.ri.createlab.collections;

import java.util.List;
import junit.framework.TestCase;

/**
 * <p>
 * <code>UniqueNodeLinkedListTest</code> tests the {@link UniqueNodeLinkedList} class.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class UniqueNodeLinkedListTest extends TestCase
   {
   public UniqueNodeLinkedListTest(final String test)
      {
      super(test);
      }

   public void testGetHeadAndGetTail()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertNull("Expected list.getHead() to be null", list.getHead());
      assertNull("Expected list.getTail() to be null", list.getTail());

      for (int i = 0; i < 10; i++)
         {
         assertTrue("Expected result of list.add(" + i + ") to be true", list.add(i));
         assertEquals("Expected list.getHead() to be 0", (Integer)0, list.getHead());
         assertEquals("Expected list.getTail() to be " + i, (Integer)i, list.getTail());
         }

      for (int i = 9; i >= 0; i--)
         {
         assertTrue("Expected result of list.remove(" + i + ") to be true", list.remove(i));
         if (i == 0)
            {
            assertNull("Expected list.getHead() to be null", list.getHead());
            assertNull("Expected list.getTail() to be null", list.getTail());
            }
         else
            {
            assertEquals("Expected list.getHead() to be 0", (Integer)0, list.getHead());
            assertEquals("Expected list.getTail() to be " + i, (Integer)(i - 1), list.getTail());
            }
         }

      for (int i = 0; i < 10; i++)
         {
         assertTrue("Expected result of list.add(" + i + ") to be true", list.add(i));
         assertEquals("Expected list.getHead() to be 0", (Integer)0, list.getHead());
         assertEquals("Expected list.getTail() to be " + i, (Integer)i, list.getTail());
         }

      for (int i = 0; i < 10; i++)
         {
         assertTrue("Expected result of list.remove(" + i + ") to be true", list.remove(i));
         if (i == 9)
            {
            assertNull("Expected list.getHead() to be null", list.getHead());
            assertNull("Expected list.getTail() to be null", list.getTail());
            }
         else
            {
            assertEquals("Expected list.getHead() to be " + (i + 1), (Integer)(i + 1), list.getHead());
            assertEquals("Expected list.getTail() to be 9", (Integer)9, list.getTail());
            }
         }

      for (int i = 0; i < 10; i++)
         {
         assertTrue("Expected result of list.add(" + i + ") to be true", list.add(i));
         assertEquals("Expected list.getHead() to be 0", (Integer)0, list.getHead());
         assertEquals("Expected list.getTail() to be " + i, (Integer)i, list.getTail());
         }

      final int[] intsToRemove = new int[]{5, 4, 6, 3, 7, 2, 8, 1, 9, 0};
      for (final int i : intsToRemove)
         {
         list.remove(i);
         if (i == 0)
            {
            assertNull("Expected list.getHead() to be null", list.getHead());
            assertNull("Expected list.getTail() to be null", list.getTail());
            }
         else if (i == 9)
            {
            assertEquals((Integer)0, list.getHead());
            assertEquals((Integer)0, list.getTail());
            }
         else
            {
            assertEquals((Integer)0, list.getHead());
            assertEquals((Integer)9, list.getTail());
            }
         }

      for (int i = 0; i < 5; i++)
         {
         assertTrue("Expected result of list.add(" + i + ") to be true", list.add(i));
         assertEquals("Expected list.getHead() to be 0", (Integer)0, list.getHead());
         assertEquals("Expected list.getTail() to be " + i, (Integer)i, list.getTail());
         }

      list.remove(0);
      assertEquals((Integer)1, list.getHead());
      assertEquals((Integer)4, list.getTail());
      list.remove(4);
      assertEquals((Integer)1, list.getHead());
      assertEquals((Integer)3, list.getTail());
      list.remove(1);
      assertEquals((Integer)2, list.getHead());
      assertEquals((Integer)3, list.getTail());
      list.remove(3);
      assertEquals((Integer)2, list.getHead());
      assertEquals((Integer)2, list.getTail());
      list.remove(2);
      assertNull("Expected list.getHead() to be null", list.getHead());
      assertNull("Expected list.getTail() to be null", list.getTail());
      }

   public void testGetPreviousAndGetNext()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertNull(list.getPrevious(0));
      assertNull(list.getNext(0));

      for (int i = 0; i < 10; i++)
         {
         list.add(i);
         }

      assertNull(list.getPrevious(42));
      assertNull(list.getNext(42));

      assertNull(list.getPrevious(null));
      assertNull(list.getNext(null));

      for (int i = 0; i < 10; i++)
         {
         final Integer previous = list.getPrevious(i);
         final Integer next = list.getNext(i);

         if (i == 0)
            {
            assertNull(previous);
            }
         else if (i == 9)
            {
            assertNull(next);
            }
         else
            {
            assertEquals((Integer)(i - 1), previous);
            assertEquals((Integer)(i + 1), next);
            }
         }
      }

   public void testAdd()
      {
      // add() is tested a lot elsewhere, so just do a few other tests here
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertFalse(list.add(null));
      assertTrue(list.add(1));
      assertFalse(list.add(1));  // can't add elements that already exist
      }

   public void testInsertAfter()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertFalse(list.insertAfter(1, 0));
      assertFalse(list.insertAfter(null, 0));
      assertFalse(list.insertAfter(1, null));
      assertFalse(list.insertAfter(null, null));

      assertTrue(list.add(0));
      for (int i = 1; i < 10; i++)
         {
         assertTrue(list.insertAfter(i, 0));
         assertFalse(list.insertAfter(i, 0));  // can't add elements that already exist
         }

      list.clear();
      assertTrue(list.isEmpty());

      assertTrue(list.add(0));
      for (int i = 1; i < 10; i++)
         {
         assertTrue(list.insertAfter(i, i - 1));
         assertFalse(list.insertAfter(i, i - 1));  // can't add elements that already exist
         }
      }

   public void testInsertBefore()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertFalse(list.insertBefore(1, 0));
      assertFalse(list.insertBefore(null, 0));
      assertFalse(list.insertBefore(1, null));
      assertFalse(list.insertBefore(null, null));

      assertTrue(list.add(0));
      for (int i = 1; i < 10; i++)
         {
         assertTrue(list.insertBefore(i, 0));
         assertFalse(list.insertBefore(i, 0));  // can't add elements that already exist
         }

      list.clear();
      assertTrue(list.isEmpty());

      assertTrue(list.add(0));
      for (int i = 1; i < 10; i++)
         {
         assertTrue(list.insertBefore(i, i - 1));
         assertFalse(list.insertBefore(i, i - 1));  // can't add elements that already exist
         }
      }

   public void testContains()
      {
      // add() is tested a lot elsewhere, so just do a few other tests here
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertFalse(list.contains(null));
      assertFalse(list.contains(0));

      for (int i = 0; i < 10; i++)
         {
         assertFalse(list.contains(i));
         list.add(i);
         assertTrue(list.contains(i));
         list.remove(i);
         assertFalse(list.contains(i));
         }
      }

   public void testRemove()
      {
      // remove() is tested a lot elsewhere, so just do a few other tests here
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertFalse(list.remove(null));
      assertFalse(list.remove(0));
      assertFalse(list.remove(42));
      }

   public void testClear()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      list.clear();

      assertEquals("Expected size to be zero", 0, list.size());
      assertTrue("Expected isEmpty to be true", list.isEmpty());

      for (int i = 0; i < 10; i++)
         {
         list.add(i);
         }

      assertEquals("Expected size to be 10", 10, list.size());

      list.clear();

      assertEquals("Expected size to be zero", 0, list.size());
      assertTrue("Expected isEmpty to be true", list.isEmpty());
      }

   public void testSizeAndIsEmpty()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      assertEquals("Expected size to be zero", 0, list.size());
      assertTrue("Expected isEmpty to be true", list.isEmpty());

      assertTrue("Expected result of list.add(1) to be true", list.add(1));
      assertEquals("Expected size to be 1", 1, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertFalse("Expected result of list.add(1) to be false", list.add(1));
      assertEquals("Expected size to be 1", 1, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertTrue("Expected result of list.add(2) to be true", list.add(2));
      assertEquals("Expected size to be 2", 2, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertFalse("Expected result of list.add(2) to be false", list.add(2));
      assertEquals("Expected size to be 2", 2, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertTrue("Expected result of list.remove(1) to be true", list.remove(1));
      assertEquals("Expected size to be 1", 1, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertFalse("Expected result of list.remove(1) to be false", list.remove(1));
      assertEquals("Expected size to be 1", 1, list.size());
      assertFalse("Expected isEmpty to be false", list.isEmpty());

      assertTrue("Expected result of list.remove(2) to be true", list.remove(2));
      assertEquals("Expected size to be 0", 0, list.size());
      assertTrue("Expected isEmpty to be true", list.isEmpty());

      assertFalse("Expected result of list.remove(2) to be false", list.remove(2));
      assertEquals("Expected size to be 0", 0, list.size());
      assertTrue("Expected isEmpty to be true", list.isEmpty());
      }

   public void testGetAsList()
      {
      final UniqueNodeLinkedList<Integer> list = new UniqueNodeLinkedList<Integer>();

      final List<Integer> list1 = list.getAsList();
      assertNotNull(list1);
      assertEquals(0, list.size());

      for (int i = 0; i < 10; i++)
         {
         list.add(i);
         }

      final List<Integer> list2 = list.getAsList();
      assertNotNull(list2);
      assertEquals(10, list2.size());
      for (int i = 0; i < 10; i++)
         {
         assertEquals((Integer)i, list2.get(i));
         }
      }
   }