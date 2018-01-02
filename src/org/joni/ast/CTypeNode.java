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

public final class CTypeNode extends Node {
    public final int ctype;
    public final boolean not;
    public final boolean asciiRange;

    public CTypeNode(int type, boolean not, boolean asciiRange) {
        super(CTYPE);
        this.ctype= type;
        this.not = not;
        this.asciiRange = asciiRange;
    }

    @Override
    public String getName() {
        return "Character Type";
    }

    @Override
    public String toString(int level) {
        StringBuilder value = new StringBuilder();
        value.append("\n  ctype: " + ctype);
        value.append(", not: " + not);
        value.append(", ascii: " + asciiRange);
        return value.toString();
    }

}
