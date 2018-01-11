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
package org.joni.ast;

import org.joni.UnsetAddrList;

public final class CallNode extends StateNode {
    public final byte[]name;
    public final int nameP;
    public final int nameEnd;

    public int groupNum;
    public EncloseNode target;
    public UnsetAddrList unsetAddrList;

    public CallNode(byte[]name, int nameP, int nameEnd, int gnum) {
        super(CALL);
        this.name = name;
        this.nameP = nameP;
        this.nameEnd = nameEnd;
        this.groupNum = gnum; /* call by number if gnum != 0 */
    }

    @Override
    protected void setChild(Node newChild) {
        target = (EncloseNode)newChild;
    }

    @Override
    protected Node getChild() {
        return target;
    }

    public void setTarget(EncloseNode tgt) {
        target = tgt;
        tgt.parent = this;
    }

    @Override
    public String getName() {
        return "Call";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder(super.toString(level));
        value.append("\n  name: " + new String(name, nameP, nameEnd - nameP));
        value.append(", groupNum: " + groupNum);
        value.append("\n  unsetAddrList: " + pad(unsetAddrList, level + 1));
        value.append("\n  target: " + pad(target.getAddressName(), level + 1));
        return value.toString();
    }

}
