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

import org.joni.ast.EncloseNode;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;

public final class UnsetAddrList {
    EncloseNode[]targets;
    int[]offsets;
    int num;

    public UnsetAddrList(int size) {
        targets = new EncloseNode[size];
        offsets = new int[size];
    }

    public void add(int offset, EncloseNode node) {
        if (num >= offsets.length) {
            EncloseNode []ttmp = new EncloseNode[targets.length << 1];
            System.arraycopy(targets, 0, ttmp, 0, num);
            targets = ttmp;
            int[]otmp = new int[offsets.length << 1];
            System.arraycopy(offsets, 0, otmp, 0, num);
            offsets = otmp;
        }
        targets[num] = node;
        offsets[num] = offset;
        num++;
    }

    public void fix(Regex regex) {
        for (int i=0; i<num; i++) {
            EncloseNode en = targets[i];
            if (!en.isAddrFixed()) throw new InternalException(ErrorMessages.PARSER_BUG);
            regex.code[offsets[i]] = en.callAddr; // is this safe ?
        }
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                value.append("offset + ").append(offsets[i]).append(" target: ").append(targets[i].getAddressName());
            }
        }
        return value.toString();
    }
}
