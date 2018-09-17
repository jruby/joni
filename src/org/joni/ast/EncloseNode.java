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

import org.joni.Config;
import org.joni.Option;
import org.joni.constants.internal.EncloseType;

public final class EncloseNode extends StateNode implements EncloseType {
    public final int type;          // enclose type
    public int regNum;
    public int option;
    public Node target;             /* EncloseNode : ENCLOSE_MEMORY */
    public int callAddr;            // AbsAddrType
    public int minLength;           // OnigDistance
    public int maxLength;           // OnigDistance
    public int charLength;
    public int optCount;            // referenced count in optimize_node_left()
    public Node containingAnchor;   //

    // node_new_enclose / onig_node_new_enclose
    public EncloseNode(int type) {
        super(ENCLOSE);
        this.type = type;
        callAddr = -1;
    }

    public static EncloseNode newMemory(int option, boolean isNamed) {
        EncloseNode en = new EncloseNode(MEMORY);
        if (Config.USE_SUBEXP_CALL) en.option = option;
        if (isNamed) en.setNamedGroup();
        return en;
    }

    public static EncloseNode newOption(int option) {
        EncloseNode en = new EncloseNode(OPTION);
        en.option = option;
        return en;
    }

    @Override
    protected void setChild(Node child) {
        target = child;
    }

    @Override
    protected Node getChild() {
        return target;
    }

    public void setTarget(Node tgt) {
        target = tgt;
        tgt.parent = this;
    }

    @Override
    public String getName() {
        return "Enclose";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder(super.toString(level));
        value.append("\n  type: " + typeToString());
        value.append("\n  regNum: " + regNum);
        value.append(", option: " + Option.toString(option));
        value.append(", callAddr: " + callAddr);
        value.append(", minLength: " + minLength);
        value.append(", maxLength: " + maxLength);
        value.append(", charLength: " + charLength);
        value.append(", optCount: " + optCount);
        value.append("\n  target: " + pad(target, level + 1));
        return value.toString();
    }

    public String typeToString() {
        StringBuilder types = new StringBuilder();
        if (isStopBacktrack()) types.append("STOP_BACKTRACK ");
        if (isMemory()) types.append("MEMORY ");
        if (isOption()) types.append("OPTION ");
        if (isCondition()) types.append("CONDITION ");
        if (isAbsent()) types.append("ABSENT ");
        return types.toString();
    }

    public void setEncloseStatus(int flag) {
        state |= flag;
    }

    public void clearEncloseStatus(int flag) {
        state &= ~flag;
    }

    public boolean isMemory() {
        return (type & MEMORY) != 0;
    }

    public boolean isOption() {
        return (type & OPTION) != 0;
    }

    public boolean isCondition() {
        return (type & CONDITION) != 0;
    }

    public boolean isStopBacktrack() {
        return (type & STOP_BACKTRACK) != 0;
    }

    public boolean isAbsent() {
        return (type & ABSENT) != 0;
    }
}
