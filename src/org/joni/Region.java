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

public abstract class Region {
    static final int REGION_NOTPOS = -1;

    protected CaptureTreeNode historyRoot;

    public static Region newRegion(int num) {
        if (num == 1) return new SingleRegion(num);
        return new MultiRegion(num);
    }

    public static Region newRegion(int begin, int end) {
        return new SingleRegion(begin, end);
    }

    @Override
    public abstract Region clone();

    public abstract int getNumRegs();

    public abstract int getBeg(int index);

    public abstract int setBeg(int index, int value);

    public abstract int getEnd(int index);

    public abstract int setEnd(int index, int value);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Region: \n");
        for (int i=0; i<getNumRegs(); i++) sb.append(" " + i + ": (" + getBeg(i) + "-" + getEnd(i) + ")");
        return sb.toString();
    }

    CaptureTreeNode getCaptureTree() {
        return historyRoot;
    }

    CaptureTreeNode setCaptureTree(CaptureTreeNode ctn) {
        return this.historyRoot = ctn;
    }

    abstract void clear();
}
