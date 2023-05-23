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

import java.util.Arrays;

public final class MultiRegion extends Region {
    private final int[] begEnd;

    public MultiRegion(int num) {
        this.begEnd = new int[num * 2];
    }

    public MultiRegion(int begin, int end) {
        this.begEnd = new int[]{begin, end};
    }

    public final int getNumRegs() {
        return begEnd.length / 2;
    }

    public MultiRegion clone() {
        MultiRegion region = new MultiRegion(getNumRegs());
        System.arraycopy(begEnd, 0, region.begEnd, 0, begEnd.length);
        if (getCaptureTree() != null) region.setCaptureTree(getCaptureTree().cloneTree());
        return region;
    }

    public int getBeg(int index) {
        return begEnd[index * 2];
    }

    public int setBeg(int index, int value) {
        return begEnd[index * 2] = value;
    }

    public int getEnd(int index) {
        return begEnd[index * 2 + 1];
    }

    public int setEnd(int index, int value) {
        return begEnd[index * 2 + 1] = value;
    }

    void clear() {
        Arrays.fill(begEnd, REGION_NOTPOS);
    }
}
