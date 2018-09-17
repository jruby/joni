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

import org.joni.constants.internal.AnchorType;

public final class AnchorNode extends Node {
    public final int type;
    public final boolean asciiRange;
    public Node target;
    public int charLength;

    public AnchorNode(int type, boolean asciiRange) {
        super(ANCHOR);
        this.type = type;
        charLength = -1;
        this.asciiRange = asciiRange;
    }

    public AnchorNode(int type) {
        this(type, false);
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
        return "Anchor";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder();
        value.append("\n  type: " + typeToString());
        value.append(", ascii: " + asciiRange);
        value.append("\n  target: " + pad(target, level + 1));
        return value.toString();
    }

    public String typeToString() {
        StringBuilder type = new StringBuilder();
        if (isType(AnchorType.BEGIN_BUF)) type.append("BEGIN_BUF ");
        if (isType(AnchorType.BEGIN_LINE)) type.append("BEGIN_LINE ");
        if (isType(AnchorType.BEGIN_POSITION)) type.append("BEGIN_POSITION ");
        if (isType(AnchorType.END_BUF)) type.append("END_BUF ");
        if (isType(AnchorType.SEMI_END_BUF)) type.append("SEMI_END_BUF ");
        if (isType(AnchorType.END_LINE)) type.append("END_LINE ");
        if (isType(AnchorType.WORD_BOUND)) type.append("WORD_BOUND ");
        if (isType(AnchorType.NOT_WORD_BOUND)) type.append("NOT_WORD_BOUND ");
        if (isType(AnchorType.WORD_BEGIN)) type.append("WORD_BEGIN ");
        if (isType(AnchorType.WORD_END)) type.append("WORD_END ");
        if (isType(AnchorType.PREC_READ)) type.append("PREC_READ ");
        if (isType(AnchorType.PREC_READ_NOT)) type.append("PREC_READ_NOT ");
        if (isType(AnchorType.LOOK_BEHIND)) type.append("LOOK_BEHIND ");
        if (isType(AnchorType.LOOK_BEHIND_NOT)) type.append("LOOK_BEHIND_NOT ");
        if (isType(AnchorType.ANYCHAR_STAR)) type.append("ANYCHAR_STAR ");
        if (isType(AnchorType.ANYCHAR_STAR_ML)) type.append("ANYCHAR_STAR_ML ");
        return type.toString();
    }

    private boolean isType(int type) {
        return (this.type & type) != 0;
    }

}
