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

public final class Region {
    static final int REGION_NOTPOS = -1;

    @Deprecated
    public final int numRegs;
    @Deprecated
    public final int[] beg;
    @Deprecated
    public final int[] end;
    @Deprecated
    public CaptureTreeNode historyRoot;

    @SuppressWarnings("deprecation")
    public Region(int num) {
        this.numRegs = num;
        this.beg = new int[num];
        this.end = new int[num];
    }

    @SuppressWarnings("deprecation")
    public Region(int begin, int end) {
        this.numRegs = 1;
        this.beg = new int[]{begin};
        this.end = new int[]{end};
    }

    @SuppressWarnings("deprecation")
    public Region clone() {
        Region region = new Region(numRegs);
        System.arraycopy(beg, 0, region.beg, 0, beg.length);
        System.arraycopy(end, 0, region.end, 0, end.length);
        if (historyRoot != null) region.historyRoot = historyRoot.cloneTree();
        return region;
    }

    @SuppressWarnings("deprecation")
    public int getNumRegs() {
        return numRegs;
    }

    @SuppressWarnings("deprecation")
    public int getBeg(int index) {
        return beg[index];
    }

    @SuppressWarnings("deprecation")
    public int setBeg(int index, int value) {
        return beg[index] = value;
    }

    @SuppressWarnings("deprecation")
    public int getEnd(int index) {
        return end[index];
    }

    @SuppressWarnings("deprecation")
    public int setEnd(int index, int value) {
        return end[index] = value;
    }

    @SuppressWarnings("deprecation")
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Region: \n");
        for (int i=0; i<beg.length; i++) sb.append(" " + i + ": (" + beg[i] + "-" + end[i] + ")");
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
    CaptureTreeNode getCaptureTree() {
        return historyRoot;
    }

    @SuppressWarnings("deprecation")
    CaptureTreeNode setCaptureTree(CaptureTreeNode ctn) {
        return this.historyRoot = ctn;
    }

    @SuppressWarnings("deprecation")
    void clear() {
        for (int i=0; i<beg.length; i++) {
            beg[i] = end[i] = REGION_NOTPOS;
        }
    }
}
