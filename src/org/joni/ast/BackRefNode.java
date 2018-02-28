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
import org.joni.ScanEnvironment;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;

public final class BackRefNode extends StateNode {
    public final int back[];
    public int backNum;
    public int nestLevel;

    private BackRefNode(int backNum, int[]backRefs, boolean byName, ScanEnvironment env) {
        super(BREF);
        this.backNum = backNum;
        if (byName) setNameRef();

        for (int i=0; i<backNum; i++) {
            if (backRefs[i] <= env.numMem && env.memNodes[backRefs[i]] == null) {
                setRecursion(); /* /...(\1).../ */
                break;
            }
        }
        back = backRefs;
    }

    public BackRefNode(int backNum, int[]backRefs, boolean byName, boolean existLevel, int nestLevel, ScanEnvironment env) {
        this(backNum, backRefs, byName, env);

        if (Config.USE_BACKREF_WITH_LEVEL && existLevel) {
            setNestLevel();
            this.nestLevel = nestLevel;
        }
    }

    public void renumber(int[]map) {
        if (!isNameRef()) throw new ValueException(ErrorMessages.NUMBERED_BACKREF_OR_CALL_NOT_ALLOWED);

        int oldNum = backNum;

        int pos = 0;
        for (int i=0; i<oldNum; i++) {
            int n = map[back[i]];
            if (n > 0) {
                back[pos] = n;
                pos++;
            }
        }
        backNum = pos;
    }

    @Override
    public String getName() {
        return "Back Ref";
    }

    @Override
    public String toString(int level) {
        StringBuilder sb = new StringBuilder(super.toString(level));
        sb.append("\n  backNum: " + backNum);
        String backs = "";
        for (int i=0; i<back.length; i++) backs += back[i] + ", ";
        sb.append("\n  back: " + backs);
        sb.append("\n  nextLevel: " + nestLevel);
        return sb.toString();
    }
}
