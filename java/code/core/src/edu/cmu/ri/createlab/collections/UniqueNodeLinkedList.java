package edu.cmu.ri.createlab.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>UniqueNodeLinkedList</code> is a linked list which requires that all added elements be unique.  Doesn't allow
 * <code>null</code> elements.  Most operations execute in constant time.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class UniqueNodeLinkedList<T>
   {
   private static final class Node<T>
      {
      private Node<T> previousNode = null;
      private Node<T> nextNode = null;
      private final T value;

      private Node(@NotNull final T value)
         {
         this.value = value;
         }

      @Nullable
      public Node<T> getPreviousNode()
         {
         return previousNode;
         }

      private void setPreviousNode(@Nullable final Node<T> previousNode)
         {
         this.previousNode = previousNode;
         }

      @Nullable
      public Node<T> getNextNode()
         {
         return nextNode;
         }

      private void setNextNode(@Nullable final Node<T> nextNode)
         {
         this.nextNode = nextNode;
         }

      public boolean isHead()
         {
         return previousNode == null;
         }

      public boolean isTail()
         {
         return nextNode == null;
         }

      @NotNull
      public T getValue()
         {
         return value;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("Node");
         sb.append("{previousNode=").append(previousNode == null ? null : previousNode.getValue());
         sb.append(", value=").append(value);
         sb.append(", nextNode=").append(nextNode == null ? null : nextNode.getValue());
         sb.append('}');
         return sb.toString();
         }
      }

   private Node<T> head = null;
   private Node<T> tail = null;
   private final Map<T, Node<T>> elementToNodeMap = new HashMap<T, Node<T>>();

   /**
    * Returns the head element of the list, or <code>null</code> if the list is empty. This is a constant-time
    * operation.
    */
   @Nullable
   public T getHead()
      {
      return head == null ? null : head.getValue();
      }

   /**
    * Returns the tail element of the list, or <code>null</code> if the list is empty. This is a constant-time
    * operation.
    */
   @Nullable
   public T getTail()
      {
      return tail == null ? null : tail.getValue();
      }

   /**
    * Finds the given <code>element</code> in the list, and returns the previous element.  Returns <code>null</code> if
    * any of the following conditions are satisfied:
    * <ol>
    *    <li>the given <code>element</code> is <code>null</code></li>
    *    <li>the given <code>element</code> does not exist in the list</li>
    *    <li>the given <code>element</code> is the head of the list</li>
    * </ol>
    * This is a constant-time operation.
    */
   @Nullable
   public T getPrevious(@Nullable final T element)
      {
      if (element != null)
         {
         final Node<T> existingNode = elementToNodeMap.get(element);
         if (existingNode != null)
            {
            final Node<T> previousNode = existingNode.getPreviousNode();
            if (previousNode != null)
               {
               return previousNode.getValue();
               }
            }
         }
      return null;
      }

   /**
    * Finds the given <code>element</code> in the list, and returns the next element.  Returns <code>null</code> if
    * any of the following conditions are satisfied:
    * <ol>
    *    <li>the given <code>element</code> is <code>null</code></li>
    *    <li>the given <code>element</code> does not exist in the list</li>
    *    <li>the given <code>element</code> is the tail of the list</li>
    * </ol>
    * This is a constant-time operation.
    */
   @Nullable
   public T getNext(@Nullable final T element)
      {
      if (element != null)
         {
         final Node<T> existingNode = elementToNodeMap.get(element);
         if (existingNode != null)
            {
            final Node<T> nextNode = existingNode.getNextNode();
            if (nextNode != null)
               {
               return nextNode.getValue();
               }
            }
         }
      return null;
      }

   /**
    * Adds the given <code>newElement</code> to the end of the list unless it already exists in the list.  Returns
    * <code>true</code> only if the <code>newElement</code> is non-<code>null</code> and doesn't already exist in the
    * list. This is a constant-time operation.
    */
   public boolean add(@Nullable final T newElement)
      {
      if (newElement != null && !contains(newElement))
         {
         final Node<T> node = new Node<T>(newElement);

         if (isEmpty())
            {
            head = node;
            tail = node;

            // store the new newElement and its node in the map
            elementToNodeMap.put(newElement, node);
            }
         else
            {
            insertAfter(newElement, tail.getValue());
            }

         return true;
         }

      return false;
      }

   /**
    * Inserts the <code>newElement</code> before the <code>existingElement</code>.  Returns <code>true</code> only if all of
    * the following conditions are satisfied:
    * <ol>
    *    <li>the given elements are both non-<code>null</code></li>
    *    <li>the <code>existingElement</code> is in the list</li>
    *    <li>the <code>newElement</code> is not in the list</li>
    * </ol>
    * This is a constant-time operation.
    */
   public boolean insertBefore(@Nullable final T newElement, @Nullable final T existingElement)
      {
      if (contains(existingElement) && (newElement != null) && !elementToNodeMap.containsKey(newElement))
         {
         final Node<T> existingNode = elementToNodeMap.get(existingElement);
         final Node<T> existingPreviousNode = existingNode.getPreviousNode();
         final Node<T> newNode = new Node<T>(newElement);

         // update the existing previous node, if any
         if (existingPreviousNode != null)
            {
            existingPreviousNode.setNextNode(newNode);
            }

         // set the pointers for the new node
         newNode.setPreviousNode(existingPreviousNode);
         newNode.setNextNode(existingNode);

         // set the previous pointer on the existing node
         existingNode.setPreviousNode(newNode);

         // finally, if the new node's previous node is null, then we have a new head
         if (newNode.getPreviousNode() == null)
            {
            head = newNode;
            }

         // store the new element and its node in the map
         elementToNodeMap.put(newElement, newNode);

         return true;
         }
      return false;
      }

   /**
    * Inserts the <code>newElement</code> after the <code>existingElement</code>.  Returns <code>true</code> only if all of
    * the following conditions are satisfied:
    * <ol>
    *    <li>the given elements are both non-<code>null</code></li>
    *    <li>the <code>existingElement</code> is in the list</li>
    *    <li>the <code>newElement</code> is not in the list</li>
    * </ol>
    * This is a constant-time operation.
    */
   public boolean insertAfter(@Nullable final T newElement, @Nullable final T existingElement)
      {
      if (contains(existingElement) && (newElement != null) && !elementToNodeMap.containsKey(newElement))
         {
         final Node<T> existingNode = elementToNodeMap.get(existingElement);
         final Node<T> existingNextNode = existingNode.getNextNode();
         final Node<T> newNode = new Node<T>(newElement);

         // update the existing next node, if any
         if (existingNextNode != null)
            {
            existingNextNode.setPreviousNode(newNode);
            }

         // set the pointers for the new node
         newNode.setPreviousNode(existingNode);
         newNode.setNextNode(existingNextNode);

         // set the next pointer on the existing node
         existingNode.setNextNode(newNode);

         // finally, if the new node's next node is null, then we have a new tail
         if (newNode.getNextNode() == null)
            {
            tail = newNode;
            }

         // store the new element and its node in the map
         elementToNodeMap.put(newElement, newNode);

         return true;
         }
      return false;
      }

   //The next three methods are all O(n) and are for manipulating the list based on indexes
   public boolean insertAtIndex(@Nullable final T element, final int index)
      {
      if (index < 0)
         {
         return false;
         }
      if (index == 0)
         {
         if (isEmpty())
            {
            return add(element);
            }
         else
            {
            return insertBefore(element, head.value);
            }
         }
      if (index > size())
         {
         return false;
         }
      T elementBefore = getAsList().get(index - 1);
      return insertAfter(element, elementBefore);
      }

   public T removeAtIndex(final int index)
      {
      T result;
      if (index < 0)
         {
         result = null;
         }
      else if (index == 0)
         {
         result = head.value;
         if (!remove(head.value))
            {
            result = null;
            }
         }
      else if (index > size())
         {
         return null;
         }
      else
         {
         result = getAsList().get(index);
         if (!remove(result))
            {
            result = null;
            }
         }
      return result;
      }

   public int indexOf(@Nullable final T element)
      {
      return getAsList().indexOf(element);
      }

   /**
    * Returns <code>true</code> if the given element is non-<code>null</code> and is contained in the list. This is a
    * constant-time operation.
    */
   public boolean contains(@Nullable final T element)
      {
      return element != null && elementToNodeMap.containsKey(element);
      }

   /**
    * Removes the given <code>elementToRemove</code> from the list.  Returns <code>true</code> only if the
    * <code>elementToRemove</code> is non-<code>null</code> and exists in the list. This is a constant-time operation.
    */
   public boolean remove(@Nullable final T elementToRemove)
      {
      if (elementToRemove != null)
         {
         final Node<T> nodeToRemove = elementToNodeMap.get(elementToRemove);

         if (nodeToRemove != null)
            {
            // first get the previous and next nodes
            final Node<T> previousNode = nodeToRemove.getPreviousNode();
            final Node<T> nextNode = nodeToRemove.getNextNode();

            // now fix the pointers
            if (previousNode != null)
               {
               previousNode.setNextNode(nextNode);
               }
            if (nextNode != null)
               {
               nextNode.setPreviousNode(previousNode);
               }

            if (nodeToRemove.isHead())
               {
               head = nextNode;
               }
            if (nodeToRemove.isTail())
               {
               tail = previousNode;
               }

            // finally, remove the node from the map
            elementToNodeMap.remove(elementToRemove);

            return true;
            }
         }
      return false;
      }

   /** Removes all elements from the list. This is a constant-time operation.*/
   public void clear()
      {
      elementToNodeMap.clear();
      head = null;
      tail = null;
      }

   /** Returns <code>true</code> if the list is empty */
   public boolean isEmpty()
      {
      return elementToNodeMap.isEmpty();
      }

   /** Returns the number of elements in the list. This is a constant-time operation.*/
   public int size()
      {
      return elementToNodeMap.size();
      }

   /**
    * Returns the contents of this linked list as a {@link List}.  Guaranteed to not return <code>null</code>.  Will
    * return an empty {@link List} if this linked list contains no elements.  This is an O(n) operation.
    */
   @NotNull
   public List<T> getAsList()
      {
      final List<T> list = new ArrayList<T>(size());

      if (!isEmpty())
         {
         Node<T> node = head;
         while (node != null)
            {
            list.add(node.getValue());
            node = node.getNextNode();
            }
         }

      return list;
      }
   }
