/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation the rights to 
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */
package org.joni.util;

import java.util.Iterator;

import org.joni.exception.InternalException;

abstract class Hash<V> implements Iterable<V> {
    protected OniHashEntry<V>[]table;
    protected int size;
        
    private static final int PRIMES[] = {
        8 + 3, 16 + 3, 32 + 5, 64 + 3, 128 + 3, 256 + 27, 512 + 9, 1024 + 9, 2048 + 5, 4096 + 3,
        8192 + 27, 16384 + 43, 32768 + 3, 65536 + 45, 131072 + 29, 262144 + 3, 524288 + 21, 1048576 + 7,
        2097152 + 17, 4194304 + 15, 8388608 + 9, 16777216 + 43, 33554432 + 35, 67108864 + 15,
        134217728 + 29, 268435456 + 3, 536870912 + 11, 1073741824 + 85, 0
    }; 
    
    private static final int INITIAL_CAPACITY = PRIMES[0];
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    
    public Hash() {
        table = new OniHashEntry[INITIAL_CAPACITY];     
    }
    
    public Hash(int size) {
        for (int i=0, n=MIN_CAPA; i<PRIMES.length; i++, n <<=1) { 
            if (n > size) {
                table = new OniHashEntry[PRIMES[i]];
                return;
            }
        }
        throw new InternalException("run out of polynomials");
    }

    public final int size() {
        return size;
    }

    static class OniHashEntry<V> {
        final int hash;
        OniHashEntry<V> next;
        public V value;

        OniHashEntry(int hash, OniHashEntry<V> next, V value) {
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

    }

    private static final int MIN_CAPA = 8;
    // private static final int DENSITY = 5;
    protected final void checkResize() {
        if (size == table.length) { // size / table.length > DENSITY
            int forSize = table.length + 1; // size + 1;         
            for (int i=0, newCapacity = MIN_CAPA; i < PRIMES.length; i++, newCapacity <<= 1) {
                if (newCapacity > forSize) {                  
                    resize(PRIMES[i]);                  
                    return;                 
                }
            }
            return;
        }
    }

    protected final void resize(int newCapacity) {
        final OniHashEntry<V>[] oldTable = table;
        final OniHashEntry<V>[] newTable = new OniHashEntry[newCapacity];
        for (int j = 0; j < oldTable.length; j++) {
            OniHashEntry<V> entry = oldTable[j];
            oldTable[j] = null;
            while (entry != null) {
                OniHashEntry<V> next = entry.next;
                int i = bucketIndex(entry.hash, newCapacity);
                entry.next = newTable[i];
                newTable[i] = entry;
                entry = next;
            }
        }
        table = newTable;
    }
    
    protected static int bucketIndex(final int h, final int length) {
        return (h % length); 
    }
    
    private static final int HASH_SIGN_BIT_MASK = ~(1 << 31);
    protected static int hashValue(int h) {
        return h & HASH_SIGN_BIT_MASK;
    }
    
    public Iterator<V> iterator() {
        return new OniHashIterator();
    }
    
    public class OniHashIterator implements Iterator<V> {
        OniHashEntry<V> next, current;
        int index;
        
        public OniHashIterator() {
            if (size > 0) {
                OniHashEntry<V>[]t= table;
                while (index < t.length && (next = t[index++]) == null);
            }
        }
        
        public boolean hasNext() {
            return next != null;
        }

        public V next() {
            OniHashEntry<V> e = next;
            if ((next = e.next) == null) {
                OniHashEntry<V>[]t = table;
                while (index < t.length && (next = t[index++]) == null);
            }
            current = e;
            return e.value;
        }

        public void remove() {
            throw new InternalException("not supported operation exception");
        }
    }
    
    public OniHashIterator entryIterator() {
        return new OniHashIterator();
    }
    
    public class OniHashEntryIterator implements Iterator<OniHashEntry<V>> {
        OniHashEntry<V> next, current;
        int index;
        
        public OniHashEntryIterator() {
            if (size > 0) {
                OniHashEntry<V>[]t= table;
                while (index < t.length && (next = t[index++]) == null);
            }
        }
        
        public boolean hasNext() {
            return next != null;
        }

        public OniHashEntry<V> next() {
            OniHashEntry<V> e = next;
            if ((next = e.next) == null) {
                OniHashEntry<V>[]t = table;
                while (index < t.length && (next = t[index++]) == null);
            }
            current = e;
            return e;
        }

        public void remove() {
            throw new InternalException("not supported operation exception");
        }
    }    
}
