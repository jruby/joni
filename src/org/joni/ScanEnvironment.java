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

import static org.joni.BitStatus.bsClear;

import org.jcodings.Encoding;
import org.joni.ast.Node;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;

public final class ScanEnvironment {

    private static final int SCANENV_MEMNODES_SIZE = 8;

    int option;
    final int caseFoldFlag;
    final public Encoding enc;
    final public Syntax syntax;
    int captureHistory;
    int btMemStart;
    int btMemEnd;
    int backrefedMem;

    final public Regex reg;

    int numCall;
    UnsetAddrList unsetAddrList; // USE_SUBEXP_CALL
    public int numMem;

    int numNamed; // USE_NAMED_GROUP

    public Node memNodes[];

    // USE_COMBINATION_EXPLOSION_CHECK
    int numCombExpCheck;
    int combExpMaxRegNum;
    int currMaxRegNum;
    boolean hasRecursion;

    int numPrecReadNotNodes;
    Node precReadNotNodes[];

    public ScanEnvironment(Regex regex, Syntax syntax) {
        this.reg = regex;
        option = regex.options;
        caseFoldFlag = regex.caseFoldFlag;
        enc = regex.enc;
        this.syntax = syntax;
    }

    public void clear() {
        captureHistory = bsClear();
        btMemStart = bsClear();
        btMemEnd = bsClear();
        backrefedMem = bsClear();

        numCall = 0;
        numMem = 0;

        numNamed = 0;

        memNodes = null;

        numCombExpCheck = 0;
        combExpMaxRegNum = 0;
        currMaxRegNum = 0;
        hasRecursion = false;

        numPrecReadNotNodes = 0;
        precReadNotNodes = null;
    }

    public int addMemEntry() {
        if (numMem++ == 0) {
            memNodes = new Node[SCANENV_MEMNODES_SIZE];
        } else if (numMem >= memNodes.length) {
            Node[]tmp = new Node[memNodes.length << 1];
            System.arraycopy(memNodes, 0, tmp, 0, memNodes.length);
            memNodes = tmp;
        }

        return numMem;
    }

    public void setMemNode(int num, Node node) {
        if (numMem >= num) {
            memNodes[num] = node;
        } else {
            throw new InternalException(ErrorMessages.ERR_PARSER_BUG);
        }
    }

    public void pushPrecReadNotNode(Node node) {
        numPrecReadNotNodes++;
        if (precReadNotNodes == null) {
            precReadNotNodes = new Node[SCANENV_MEMNODES_SIZE];
        } else if (numPrecReadNotNodes >= precReadNotNodes.length) {
            Node[]tmp = new Node[precReadNotNodes.length << 1];
            System.arraycopy(precReadNotNodes, 0, tmp, 0, precReadNotNodes.length);
            precReadNotNodes = tmp;
        }
        precReadNotNodes[numPrecReadNotNodes - 1] = node;
    }

    public void popPrecReadNotNode(Node node) {
        if (precReadNotNodes != null && precReadNotNodes[numPrecReadNotNodes - 1] == node) {
            precReadNotNodes[numPrecReadNotNodes - 1] = null;
            numPrecReadNotNodes--;
        }
    }

    public Node currentPrecReadNotNode() {
        if (numPrecReadNotNodes > 0) {
            return precReadNotNodes[numPrecReadNotNodes - 1];
        }
        return null;
    }

    public int convertBackslashValue(int c) {
        if (syntax.opEscControlChars()) {
            switch (c) {
            case 'n': return '\n';
            case 't': return '\t';
            case 'r': return '\r';
            case 'f': return '\f';
            case 'a': return '\007';
            case 'b': return '\010';
            case 'e': return '\033';
            case 'v':
                if (syntax.op2EscVVtab()) return 11; // ???
                break;
            default:
                break;
            }
        }
        return c;
    }

    void ccEscWarn(String s) {
        if (Config.USE_WARN) {
            if (syntax.warnCCOpNotEscaped() && syntax.backSlashEscapeInCC()) {
                reg.warnings.warn("character class has '" + s + "' without escape");
            }
        }
    }

    void closeBracketWithoutEscapeWarn(String s) {
        if (Config.USE_WARN) {
            if (syntax.warnCCOpNotEscaped()) {
                reg.warnings.warn("regular expression has '" + s + "' without escape");
            }
        }
    }
}
