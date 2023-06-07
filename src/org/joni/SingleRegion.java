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

public class SingleRegion extends Region {
    int beg;
    int end;

    public SingleRegion(int num) {
        if (num != 1) throw new IndexOutOfBoundsException(""+num);
    }

    public SingleRegion(int begin, int end) {
        this.beg = begin;
        this.end = end;
    }

    public int getNumRegs() {
        return 1;
    }

    public SingleRegion clone() {
        SingleRegion region = new SingleRegion(beg, end);
        if (getCaptureTree() != null) region.setCaptureTree(getCaptureTree().cloneTree());
        return region;
    }

    public int getBeg(int index) {
        if (index != 0) throw new IndexOutOfBoundsException(""+index);
        return beg;
    }

    public int setBeg(int index, int value) {
        if (index != 0) throw new IndexOutOfBoundsException(""+index);
        return beg = value;
    }

    public int getEnd(int index) {
        if (index != 0) throw new IndexOutOfBoundsException(""+index);
        return end;
    }

    public int setEnd(int index, int value) {
        if (index != 0) throw new IndexOutOfBoundsException(""+index);
        return end = value;
    }

    void clear() {
        beg = end = REGION_NOTPOS;
    }
}
