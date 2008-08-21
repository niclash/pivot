/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ListenerList;

public class ArrayList<T> implements List<T>, Serializable {
    public static final long serialVersionUID = 0;

    // TODO We're temporarily using a java.util.ArrayList to back this list.
    // Eventually, we'll replace this with an internal array representation.
    protected java.util.ArrayList<T> arrayList = null;

    private Comparator<T> comparator = null;
    private transient ListListenerList<T> listListeners = new ListListenerList<T>();

    public ArrayList() {
        arrayList = new java.util.ArrayList<T>();
    }

    public ArrayList(T[] items) {
        arrayList = new java.util.ArrayList<T>(items.length);
        for (int i = 0; i < items.length; i++) {
            arrayList.add(items[i]);
        }
    }

    public ArrayList(Sequence<T> sequence) {
        arrayList = new java.util.ArrayList<T>(sequence.getLength());

        for (int i = 0, n = sequence.getLength(); i < n; i++) {
            T item = sequence.get(i);
            arrayList.add(item);
        }
    }

    public ArrayList(Comparator<T> comparator) {
        arrayList = new java.util.ArrayList<T>();
        this.comparator = comparator;
    }

    public ArrayList(int initialCapacity) {
        arrayList = new java.util.ArrayList<T>(initialCapacity);
    }

    public int add(T item) {
        int index = -1;

        if (comparator == null) {
            index = getLength();
        }
        else {
            // Perform a binary search to find the insertion point
            index = Search.binarySearch(this, item, comparator);
            if (index < 0) {
                index = -(index + 1);
            }
        }

        arrayList.add(index, item);
        listListeners.itemInserted(this, index);

        return index;
    }

    public void insert(T item, int index) {
        if (comparator != null
            && Search.binarySearch(this, item, comparator) != -(index + 1)) {
            throw new IllegalArgumentException("Illegal insertion point.");
        }

        arrayList.add(index, item);

        listListeners.itemInserted(this, index);
    }

    public T update(int index, T item) {
        if (comparator != null
            && Search.binarySearch(this, item, comparator) != index) {
            throw new IllegalArgumentException("Illegal item modification.");
        }

        T previousItem = arrayList.get(index);
        arrayList.set(index, item);

        listListeners.itemUpdated(this, index, previousItem);

        return previousItem;
    }

    public int remove (T item) {
        int index = indexOf(item);

        if (index == -1) {
            throw new IllegalArgumentException("item is not an element of this list.");
        }

        remove(index, 1);

        return index;
    }

    @SuppressWarnings("unchecked")
    public Sequence<T> remove(int index, int count) {
        ArrayList<T> removed = new ArrayList<T>();

        // Remove the items from the array list
        // TODO Allocate the array list size first, or use a linked list
        for (int i = count - 1; i >= 0; i--) {
            removed.insert(arrayList.remove(index + i), 0);
        }

        listListeners.itemsRemoved(this, index, removed);

        return removed;
    }

    public void clear() {
        arrayList.clear();
        listListeners.itemsRemoved(this, 0, null);
    }

    public T get(int index) {
        return arrayList.get(index);
    }

    public int indexOf(T item) {
        int index = -1;
        if (comparator == null) {
            // TODO Ensure that we use the equals() method here when
            // managing list contents internally
            index = arrayList.indexOf(item);
        }
        else {
            // Perform a binary search to find the index
            index = Search.binarySearch(this, item, comparator);
            if (index < 0) {
                index = -1;
            }
        }

        return index;
    }

    public int getLength() {
        return arrayList.size();
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = this.comparator;

        if (previousComparator != comparator) {
            if (comparator != null) {
                // Temporarily clear the comparator so it doesn't interfere
                // with the sort
                this.comparator = null;
                Sort.quickSort(this, comparator);
            }

            this.comparator = comparator;

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    public Iterator<T> iterator() {
        // TODO Return a fail-fast iterator, similar to java.util.ArrayList
        // We can use a modificationCount value; each call to a mutator method can
        // increment the count - the iterator will retain a copy of the modifier count
        // when it is created. We can potentially reset the modifier count when all
        // outstanding iterators are finalized.

        return arrayList.iterator();
    }

    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (int i = 0, n = getLength(); i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(get(i));
        }

        sb.append("]");

        return sb.toString();
    }
}
