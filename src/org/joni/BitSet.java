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
package org.joni;

public final class BitSet {
    static final int BITS_PER_BYTE = 8;
    public static final int SINGLE_BYTE_SIZE = (1 << BITS_PER_BYTE);
    public static final int BITS_IN_ROOM = 4 * BITS_PER_BYTE;
    public static final int BITSET_SIZE = (SINGLE_BYTE_SIZE / BITS_IN_ROOM);
    static final int ROOM_SHIFT = log2(BITS_IN_ROOM);

    public final int[] bits = new int[BITSET_SIZE];

    public boolean at(int pos) {
        return (bits[pos >>> ROOM_SHIFT] & bit(pos)) != 0;
    }

    public void set(ScanEnvironment env, int pos) {
        if (at(pos)) env.ccDuplicateWarn();
        set(pos);
    }

    public void set(int pos) {
        bits[pos >>> ROOM_SHIFT] |= bit(pos);
    }

    public void clear(int pos) {
        bits[pos >>> ROOM_SHIFT] &= ~bit(pos);
    }

    public void invert(int pos) {
        bits[pos >>> ROOM_SHIFT] ^= bit(pos);
    }

    public void clear() {
        for (int i=0; i<BITSET_SIZE; i++) bits[i]=0;
    }

    public boolean isEmpty() {
        for (int i=0; i<BITSET_SIZE; i++) {
            if (bits[i] != 0) return false;
        }
        return true;
    }

    public void setRange(ScanEnvironment env, int from, int to) {
        for (int i=from; i<=to && i < SINGLE_BYTE_SIZE; i++) {
            if (env != null && at(i)) env.ccDuplicateWarn();
            set(i);
        }
    }

    public void setAll() {
        for (int i=0; i<BITSET_SIZE; i++) bits[i] = ~0;
    }

    public void invert() {
        for (int i=0; i<BITSET_SIZE; i++) bits[i] = ~bits[i];
    }

    public void invertTo(BitSet to) {
        for (int i=0; i<BITSET_SIZE; i++) to.bits[i] = ~bits[i];
    }

    public void and(BitSet other) {
        for (int i=0; i<BITSET_SIZE; i++) bits[i] &= other.bits[i];
    }

    public void or(BitSet other) {
        for (int i=0; i<BITSET_SIZE; i++) bits[i] |= other.bits[i];
    }

    public void copy(BitSet other) {
        for (int i=0; i<BITSET_SIZE; i++) bits[i] = other.bits[i];
    }

    int numOn() {
        int num = 0;
        for (int i=0; i<SINGLE_BYTE_SIZE; i++) {
            if (at(i)) num++;
        }
        return num;
    }

    private static int bit(int pos){
        return 1 << (pos % SINGLE_BYTE_SIZE);
    }

    private static int log2(int n){
        int log = 0;
        while ((n >>>= 1) != 0) log++;
        return log;
    }

    private static final int BITS_TO_STRING_WRAP = 4;
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("BitSet");
        for (int i=0; i<SINGLE_BYTE_SIZE; i++) {
            if ((i % (SINGLE_BYTE_SIZE / BITS_TO_STRING_WRAP)) == 0) buffer.append("\n  ");
            buffer.append(at(i) ? "1" : "0");
        }
        return buffer.toString();
    }
}
