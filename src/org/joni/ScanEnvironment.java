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

import org.jcodings.Encoding;
import org.jcodings.util.BytesHash;
import org.joni.ast.EncloseNode;
import org.joni.ast.Node;
import org.joni.constants.SyntaxProperties;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.ValueException;

public final class ScanEnvironment {
    public int option;
    final int caseFoldFlag;
    public final Encoding enc;
    public final Syntax syntax;
    int captureHistory;
    int btMemStart;
    int btMemEnd;
    int backrefedMem;

    public final WarnCallback warnings;

    int numCall;
    UnsetAddrList unsetAddrList; // USE_SUBEXP_CALL
    public int numMem;

    int numNamed; // USE_NAMED_GROUP
    BytesHash<NameEntry> nameTable;

    public EncloseNode memNodes[];

    // USE_COMBINATION_EXPLOSION_CHECK
    int numCombExpCheck;
    int combExpMaxRegNum;
    int currMaxRegNum;
    boolean hasRecursion;
    private int warningsFlag;

    int numPrecReadNotNodes;
    Node precReadNotNodes[];

    ScanEnvironment(Regex regex, Syntax syntax, WarnCallback warnings) {
        this.syntax = syntax;
        this.warnings = warnings;
        option = regex.options;
        caseFoldFlag = regex.caseFoldFlag;
        enc = regex.enc;
    }

    int addMemEntry() {
        if (numMem >= Config.MAX_CAPTURE_GROUP_NUM) throw new InternalException(ErrorMessages.TOO_MANY_CAPTURE_GROUPS);
        if (numMem++ == 0) {
            memNodes = new EncloseNode[Config.SCANENV_MEMNODES_SIZE];
        } else if (numMem >= memNodes.length) {
            EncloseNode[]tmp = new EncloseNode[memNodes.length << 1];
            System.arraycopy(memNodes, 0, tmp, 0, memNodes.length);
            memNodes = tmp;
        }

        return numMem;
    }

    void setMemNode(int num, EncloseNode node) {
        if (numMem >= num) {
            memNodes[num] = node;
        } else {
            throw new InternalException(ErrorMessages.PARSER_BUG);
        }
    }

    NameEntry nameFind(byte[]name, int nameP, int nameEnd) {
        if (nameTable != null) return nameTable.get(name, nameP, nameEnd);
        return null;
    }

    void renumberNameTable(int[]map) {
        if (nameTable != null) {
            for (NameEntry e : nameTable) {
                if (e.backNum > 1) {
                    for (int i=0; i<e.backNum; i++) {
                        e.backRefs[i] = map[e.backRefs[i]];
                    }
                } else if (e.backNum == 1) {
                    e.backRef1 = map[e.backRef1];
                }
            }
        }
    }

    void nameAdd(byte[]name, int nameP, int nameEnd, int backRef, Syntax syntax) {
        if (nameEnd - nameP <= 0) throw new ValueException(ErrorMessages.EMPTY_GROUP_NAME);

        NameEntry e = null;
        if (nameTable == null) {
            nameTable = new BytesHash<NameEntry>(); // 13, oni defaults to 5
        } else {
            e = nameFind(name, nameP, nameEnd);
        }

        if (e == null) {
            // dup the name here as oni does ?, what for ? (it has to manage it, we don't)
            e = new NameEntry(name, nameP, nameEnd);
            nameTable.putDirect(name, nameP, nameEnd, e);
        } else if (e.backNum >= 1 && !syntax.allowMultiplexDefinitionName()) {
            throw new ValueException(ErrorMessages.MULTIPLEX_DEFINED_NAME, new String(name, nameP, nameEnd - nameP));
        }

        e.addBackref(backRef);
    }

    NameEntry nameToGroupNumbers(byte[]name, int nameP, int nameEnd) {
        return nameFind(name, nameP, nameEnd);
    }

    int nameToBackrefNumber(byte[]name, int nameP, int nameEnd, Region region) {
        NameEntry e = nameToGroupNumbers(name, nameP, nameEnd);
        if (e == null) throw new ValueException(ErrorMessages.UNDEFINED_NAME_REFERENCE,
                                                new String(name, nameP, nameEnd - nameP));

        switch(e.backNum) {
        case 0:
            throw new InternalException(ErrorMessages.PARSER_BUG);
        case 1:
            return e.backRef1;
        default:
            if (region != null) {
                for (int i = e.backNum - 1; i >= 0; i--) {
                    if (region.beg[e.backRefs[i]] != Region.REGION_NOTPOS) return e.backRefs[i];
                }
            }
            return e.backRefs[e.backNum - 1];
        }
    }

    void pushPrecReadNotNode(Node node) {
        numPrecReadNotNodes++;
        if (precReadNotNodes == null) {
            precReadNotNodes = new Node[Config.SCANENV_MEMNODES_SIZE];
        } else if (numPrecReadNotNodes >= precReadNotNodes.length) {
            Node[]tmp = new Node[precReadNotNodes.length << 1];
            System.arraycopy(precReadNotNodes, 0, tmp, 0, precReadNotNodes.length);
            precReadNotNodes = tmp;
        }
        precReadNotNodes[numPrecReadNotNodes - 1] = node;
    }

    void popPrecReadNotNode(Node node) {
        if (precReadNotNodes != null && precReadNotNodes[numPrecReadNotNodes - 1] == node) {
            precReadNotNodes[numPrecReadNotNodes - 1] = null;
            numPrecReadNotNodes--;
        }
    }

    Node currentPrecReadNotNode() {
        if (numPrecReadNotNodes > 0) {
            return precReadNotNodes[numPrecReadNotNodes - 1];
        }
        return null;
    }

    int convertBackslashValue(int c) {
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
                if (syntax.op2EscVVtab()) return 11; // '\v'
                break;
            default:
                if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) unknownEscWarn(String.valueOf((char)c));
            }
        }
        return c;
    }

    void ccEscWarn(String s) {
        if (warnings != WarnCallback.NONE) {
            if (syntax.warnCCOpNotEscaped() && syntax.backSlashEscapeInCC()) {
                warnings.warn("character class has '" + s + "' without escape");
            }
        }
    }

    void unknownEscWarn(String s) {
        if (warnings != WarnCallback.NONE) {
            warnings.warn("Unknown escape \\" + s + " is ignored");
        }
    }

    void closeBracketWithoutEscapeWarn(String s) {
        if (warnings != WarnCallback.NONE) {
            if (syntax.warnCCOpNotEscaped()) {
                warnings.warn("regular expression has '" + s + "' without escape");
            }
        }
    }

    void ccDuplicateWarn() {
        if (syntax.warnCCDup() && (warningsFlag & SyntaxProperties.WARN_CC_DUP) == 0) {
            warningsFlag |= SyntaxProperties.WARN_CC_DUP;
            // FIXME: #34 points out problem and what it will take to uncomment this (we were getting erroneous versions of this)
            // warnings.warn("character class has duplicated range");
        }
    }
}
