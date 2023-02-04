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

public final class Region {
    static final int REGION_NOTPOS = -1;

    private final int numRegs;
    private final int[] begEnd;
    private CaptureTreeNode historyRoot;

    @SuppressWarnings("deprecation")
    public static Region newRegion(int num) {
        return new Region(num);
    }

    @SuppressWarnings("deprecation")
    public static Region newRegion(int begin, int end) {
        return new Region(begin, end);
    }

    @Deprecated
    public Region(int num) {
        this.numRegs = num;
        this.begEnd = new int[num * 2];
    }

    @Deprecated
    public Region(int begin, int end) {
        this.numRegs = 1;
        this.begEnd = new int[]{begin, end};
    }

    public Region clone() {
        Region region = new Region(numRegs);
        System.arraycopy(begEnd, 0, region.begEnd, 0, begEnd.length);
        if (historyRoot != null) region.historyRoot = historyRoot.cloneTree();
        return region;
    }

    public int getNumRegs() {
        return numRegs;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Region: \n");
        for (int i=0; i<numRegs; i++) sb.append(" " + i + ": (" + begEnd[i*2] + "-" + begEnd[i*2+1] + ")");
        return sb.toString();
    }

    CaptureTreeNode getCaptureTree() {
        return historyRoot;
    }

    CaptureTreeNode setCaptureTree(CaptureTreeNode ctn) {
        return this.historyRoot = ctn;
    }

    void clear() {
        Arrays.fill(begEnd, REGION_NOTPOS);
    }
}
