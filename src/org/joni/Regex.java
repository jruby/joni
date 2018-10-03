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

import static org.joni.BitStatus.bsAt;
import static org.joni.Config.USE_SUNDAY_QUICK_SEARCH;
import static org.joni.Option.isCaptureGroup;
import static org.joni.Option.isDontCaptureGroup;

import java.util.Collections;
import java.util.Iterator;

import org.jcodings.CaseFoldCodeItem;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.util.BytesHash;
import org.joni.constants.internal.AnchorType;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.ValueException;

public final class Regex {
    int[] code;             /* compiled pattern */
    int codeLength;
    boolean requireStack;

    int numMem;             /* used memory(...) num counted from 1 */
    int numRepeat;          /* OP_REPEAT/OP_REPEAT_NG id-counter */
    int numNullCheck;       /* OP_NULL_CHECK_START/END id counter */
    int numCombExpCheck;    /* combination explosion check */
    int numCall;            /* number of subexp call */
    int captureHistory;     /* (?@...) flag (1-31) */
    int btMemStart;         /* need backtrack flag */
    int btMemEnd;           /* need backtrack flag */

    int stackPopLevel;

    int[]repeatRangeLo;
    int[]repeatRangeHi;

    MatcherFactory factory;

    final Encoding enc;
    int options;
    int userOptions;
    Object userObject;
    final int caseFoldFlag;

    private BytesHash<NameEntry> nameTable; // named entries

    /* optimization info (string search, char-map and anchors) */
    Search.Forward forward;                 /* optimize flag */
    Search.Backward backward;
    int thresholdLength;                    /* search str-length for apply optimize */
    int anchor;                             /* BEGIN_BUF, BEGIN_POS, (SEMI_)END_BUF */
    int anchorDmin;                         /* (SEMI_)END_BUF anchor distance */
    int anchorDmax;                         /* (SEMI_)END_BUF anchor distance */
    int subAnchor;                          /* start-anchor for exact or map */

    byte[]exact;
    int exactP;
    int exactEnd;

    byte[]map;                              /* used as BM skip or char-map */
    int[]intMap;                            /* BM skip for exact_len > 255 */
    int[]intMapBackward;                    /* BM skip for backward search */
    int dMin;                               /* min-distance of exact or map */
    int dMax;                               /* max-distance of exact or map */

    byte[][]templates;                      /* fixed pattern strings not embedded in bytecode */
    int templateNum;

    public Regex(CharSequence cs) {
        this(cs.toString());
    }

    public Regex(CharSequence cs, Encoding enc) {
        this(cs.toString(), enc);
    }

    public Regex(String str) {
        this(str.getBytes(), 0, str.length(), 0, UTF8Encoding.INSTANCE);
    }

    public Regex(String str, Encoding enc) {
        this(str.getBytes(), 0, str.length(), 0, enc);
    }

    public Regex(byte[] bytes) {
        this(bytes, 0, bytes.length, 0, ASCIIEncoding.INSTANCE);
    }

    public Regex(byte[] bytes, int p, int end) {
        this(bytes, p, end, 0, ASCIIEncoding.INSTANCE);
    }

    public Regex(byte[] bytes, int p, int end, int option) {
        this(bytes, p, end, option, ASCIIEncoding.INSTANCE);
    }

    public Regex(byte[]bytes, int p, int end, int option, Encoding enc) {
        this(bytes, p, end, option, enc, Syntax.RUBY, WarnCallback.DEFAULT);
    }

    // onig_new
    public Regex(byte[]bytes, int p, int end, int option, Encoding enc, Syntax syntax) {
        this(bytes, p, end, option, Config.ENC_CASE_FOLD_DEFAULT, enc, syntax, WarnCallback.DEFAULT);
    }

    public Regex(byte[]bytes, int p, int end, int option, Encoding enc, WarnCallback warnings) {
        this(bytes, p, end, option, enc, Syntax.RUBY, warnings);
    }

    // onig_new
    public Regex(byte[]bytes, int p, int end, int option, Encoding enc, Syntax syntax, WarnCallback warnings) {
        this(bytes, p, end, option, Config.ENC_CASE_FOLD_DEFAULT, enc, syntax, warnings);
    }

    // onig_alloc_init
    public Regex(byte[]bytes, int p, int end, int option, int caseFoldFlag, Encoding enc, Syntax syntax, WarnCallback warnings) {

        if ((option & (Option.DONT_CAPTURE_GROUP | Option.CAPTURE_GROUP)) ==
            (Option.DONT_CAPTURE_GROUP | Option.CAPTURE_GROUP)) {
            throw new ValueException(ErrorMessages.INVALID_COMBINATION_OF_OPTIONS);
        }

        if ((option & Option.NEGATE_SINGLELINE) != 0) {
            option |= syntax.options;
            option &= ~Option.SINGLELINE;
        } else {
            option |= syntax.options;
        }

        this.enc = enc;
        this.options = option;
        this.caseFoldFlag = caseFoldFlag;
        new Analyser(this, syntax, bytes, p, end, warnings).compile();
    }

    public Matcher matcher(byte[]bytes) {
        return matcher(bytes, 0, bytes.length);
    }

    public Matcher matcherNoRegion(byte[]bytes) {
        return matcherNoRegion(bytes, 0, bytes.length);
    }

    public Matcher matcher(byte[]bytes, int p, int end) {
        return factory.create(this, numMem == 0 ? null : new Region(numMem + 1), bytes, p, end);
    }

    public Matcher matcherNoRegion(byte[]bytes, int p, int end) {
        return factory.create(this, null, bytes, p, end);
    }

    public int numberOfCaptures() {
        return numMem;
    }

    public int numberOfCaptureHistories() {
        if (Config.USE_CAPTURE_HISTORY) {
            int n = 0;
            for (int i=0; i<=Config.MAX_CAPTURE_HISTORY_GROUP; i++) {
                if (bsAt(captureHistory, i)) n++;
            }
            return n;
        } else {
            return 0;
        }
    }

    private NameEntry nameFind(byte[]name, int nameP, int nameEnd) {
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

    public int nameToBackrefNumber(byte[]name, int nameP, int nameEnd, Region region) {
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

    String nameTableToString() {
        StringBuilder sb = new StringBuilder();

        if (nameTable != null) {
            sb.append("name table\n");
            for (NameEntry ne : nameTable) {
                sb.append("  " + ne + "\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public Iterator<NameEntry> namedBackrefIterator() {
        return nameTable == null ? Collections.<NameEntry>emptyIterator() : nameTable.iterator();
    }

    public int numberOfNames() {
        return nameTable == null ? 0 : nameTable.size();
    }

    public boolean noNameGroupIsActive(Syntax syntax) {
        if (isDontCaptureGroup(options)) return false;

        if (Config.USE_NAMED_GROUP) {
            if (numberOfNames() > 0 && syntax.captureOnlyNamedGroup() && !isCaptureGroup(options)) return false;
        }
        return true;
    }

    /* set skip map for Boyer-Moor search */
    boolean setupBMSkipMap(boolean ignoreCase) {
        byte[]bytes = exact;
        int s = exactP;
        int end = exactEnd;
        int len = end - s;
        int clen;
        CaseFoldCodeItem[]items = CaseFoldCodeItem.EMPTY_FOLD_CODES;
        byte[]buf = new byte[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM * Config.ENC_MBC_CASE_FOLD_MAXLEN];

        final int ilen = USE_SUNDAY_QUICK_SEARCH ? len : len - 1;
        if (Config.USE_BYTE_MAP || len < Config.CHAR_TABLE_SIZE) {
            if (map == null) map = new byte[Config.CHAR_TABLE_SIZE]; // map/skip
            for (int i = 0; i < Config.CHAR_TABLE_SIZE; i++) map[i] = (byte)(USE_SUNDAY_QUICK_SEARCH ? len + 1 : len);

            for (int i = 0; i < ilen; i += clen) {
                if (ignoreCase) items = enc.caseFoldCodesByString(caseFoldFlag, bytes, s + i, end);
                clen = setupBMSkipMapCheck(bytes, s + i, end, items, buf);
                if (clen == 0) return true;

                for (int j = 0; j < clen; j++) {
                    map[bytes[s + i + j] & 0xff] = (byte)(ilen - i - j);
                    for (int k = 0; k < items.length; k++) {
                        map[buf[k * Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM + j] & 0xff] = (byte)(ilen - i - j);
                    }
                }
            }
        } else {
            if (intMap == null) intMap = new int[Config.CHAR_TABLE_SIZE];
            for (int i = 0; i < Config.CHAR_TABLE_SIZE; i++) intMap[i] = (USE_SUNDAY_QUICK_SEARCH ? len + 1 : len);

            for (int i = 0; i < ilen; i += clen) {
                if (ignoreCase) items = enc.caseFoldCodesByString(caseFoldFlag, bytes, s + i, end);
                clen = setupBMSkipMapCheck(bytes, s + i, end, items, buf);
                if (clen == 0) return true;

                for (int j = 0; j < clen; j++) {
                    intMap[bytes[s + i + j] & 0xff] = ilen - i - j;
                    for (int k = 0; k < items.length; k++) {
                        intMap[buf[k * Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM + j] & 0xff] = ilen - i - j;
                    }
                }
            }
        }
        return false;
    }

    private int setupBMSkipMapCheck(byte[]bytes, int p, int end, CaseFoldCodeItem[]items, byte[]buf) {
        int clen = enc.length(bytes, p, end);
        if (p + clen > end) clen = end - p;
        for (int j = 0; j < items.length; j++) {
            if (items[j].code.length != 1 || items[j].byteLen != clen) return 0;
            int flen = enc.codeToMbc(items[j].code[0], buf, j * Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM);
            if (flen != clen) return 0;
        }
        return clen;
    }

    void setOptimizeExactInfo(OptExactInfo e) {
        if (e.length == 0) return;

        // shall we copy that ?
        exact = e.bytes;
        exactP = 0;
        exactEnd = e.length;
        boolean allowReverse = enc.isReverseMatchAllowed(exact, exactP, exactEnd);

        if (e.ignoreCase > 0) {
            if (e.length >= 3 || (e.length >= 2 && allowReverse)) {
                forward = enc.toLowerCaseTable() != null ? Search.SLOW_IC_SB_FORWARD : Search.SLOW_IC_FORWARD;
//                if (!setupBMSkipMap(true)) {
//                    forward = allowReverse ? Search.BM_IC_FORWARD : Search.BM_NOT_REV_IC_FORWARD;
//                } else {
//                    forward = enc.toLowerCaseTable() != null ? Search.SLOW_IC_SB_FORWARD : Search.SLOW_IC_FORWARD;
//                }
            } else {
                forward = enc.toLowerCaseTable() != null ? Search.SLOW_IC_SB_FORWARD : Search.SLOW_IC_FORWARD;
            }
            backward = enc.toLowerCaseTable() != null ? Search.SLOW_IC_SB_BACKWARD : Search.SLOW_IC_BACKWARD;
        } else {
            if (e.length >= 3 || (e.length >= 2 && allowReverse)) {
                if (!setupBMSkipMap(false)) {
                    forward = allowReverse ? Search.BM_FORWARD : Search.BM_NOT_REV_FORWARD;
                } else {
                    forward = enc.isSingleByte() ? Search.SLOW_SB_FORWARD : Search.SLOW_FORWARD;
                }
            } else {
                forward = enc.isSingleByte() ? Search.SLOW_SB_FORWARD : Search.SLOW_FORWARD;
            }
            backward = enc.isSingleByte() ? Search.SLOW_SB_BACKWARD : Search.SLOW_BACKWARD;
        }

        dMin = e.mmd.min;
        dMax = e.mmd.max;

        if (dMin != MinMaxLen.INFINITE_DISTANCE) {
            thresholdLength = dMin + (exactEnd - exactP);
        }
    }

    void setOptimizeMapInfo(OptMapInfo m) {
        map = m.map;

        if (enc.isSingleByte()) {
            forward = Search.MAP_SB_FORWARD;
            backward = Search.MAP_SB_BACKWARD;
        } else {
            forward = Search.MAP_FORWARD;
            backward = Search.MAP_BACKWARD;
        }

        dMin = m.mmd.min;
        dMax = m.mmd.max;

        if (dMin != MinMaxLen.INFINITE_DISTANCE) {
            thresholdLength = dMin + 1;
        }
    }

    void setSubAnchor(OptAnchorInfo anc) {
        subAnchor |= anc.leftAnchor & AnchorType.BEGIN_LINE;
        subAnchor |= anc.rightAnchor & AnchorType.END_LINE;
    }

    void clearOptimizeInfo() {
        forward = null;
        backward = null;
        anchor = 0;
        anchorDmax = 0;
        anchorDmin = 0;
        subAnchor = 0;

        exact = null;
        exactP = exactEnd = 0;
    }

    public String optimizeInfoToString() {
        String s = "";
        s += "optimize: " + (forward != null ? forward.getName() : "NONE") + "\n";
        s += "  anchor:     " + OptAnchorInfo.anchorToString(anchor);

        if ((anchor & AnchorType.END_BUF_MASK) != 0) {
            s += MinMaxLen.distanceRangeToString(anchorDmin, anchorDmax);
        }

        s += "\n";

        if (forward != null) {
            s += "  sub anchor: " + OptAnchorInfo.anchorToString(subAnchor) + "\n";
        }

        s += "dmin: " + dMin + " dmax: " + dMax + "\n";
        s += "threshold length: " + thresholdLength + "\n";

        if (exact != null) {
            s += "exact: [" + new String(exact, exactP, exactEnd - exactP) + "]: length: " + (exactEnd - exactP) + "\n";
        } else if (forward == Search.MAP_FORWARD || forward == Search.MAP_SB_FORWARD) {
            int n=0;
            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) if (map[i] != 0) n++;

            s += "map: n = " + n + "\n";
            if (n > 0) {
                int c=0;
                s += "[";
                for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) {
                    if (map[i] != 0) {
                        if (c > 0) s += ", ";
                        c++;
                        if (enc.maxLength() == 1 && enc.isPrint(i)) s += ((char)i);
                        else s += i;
                    }
                }
                s += "]\n";
            }
        }

        return s;
    }

    public Encoding getEncoding() {
        return enc;
    }

    public int getOptions() {
        return options;
    }

    public void setUserOptions(int options) {
        this.userOptions = options;
    }

    public int getUserOptions() {
        return userOptions;
    }

    public void setUserObject(Object object) {
        this.userObject = object;
    }

    public Object getUserObject() {
        return userObject;
    }
}
