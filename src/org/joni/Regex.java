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
import static org.joni.Option.isCaptureGroup;
import static org.joni.Option.isDontCaptureGroup;

import java.util.IllegalFormatConversionException;
import java.util.Iterator;

import org.jcodings.Encoding;
import org.jcodings.util.BytesHash;
import org.joni.constants.AnchorType;
import org.joni.constants.RegexState;
import org.joni.exception.ErrorMessages;
import org.joni.exception.InternalException;
import org.joni.exception.ValueException;

public final class Regex implements RegexState {
    
    int[] code;             /* compiled pattern */
    int codeLength;
    boolean stackNeeded;
    Object[]operands;       /* e.g. shared CClassNode */
    int operandLength;

    int state;              /* normal, searching, compiling */ // remove
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

    public WarnCallback warnings;
    public MatcherFactory factory;

    final Encoding enc;
    int options;
    int userOptions;
    Object userObject;
    //final Syntax syntax;
    final int caseFoldFlag;
    
    BytesHash<NameEntry> nameTable;        // named entries
    
    /* optimization info (string search, char-map and anchors) */
    SearchAlgorithm searchAlgorithm;        /* optimize flag */
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
            throw new ValueException(ErrorMessages.ERR_INVALID_COMBINATION_OF_OPTIONS);
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
        this.warnings = warnings;

        new Analyser(new ScanEnvironment(this, syntax), bytes, p, end).compile();

        this.warnings = null;
    }
    
    public Matcher matcher(byte[]bytes) {
        return matcher(bytes, 0, bytes.length);
    }
    
    public Matcher matcher(byte[]bytes, int p, int end) {
        return factory.create(this, bytes, p, end);
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

    public int numberOfNames() {
        return nameTable == null ? 0 : nameTable.size();
    }

    void nameAdd(byte[]name, int nameP, int nameEnd, int backRef, Syntax syntax) {
        if (nameEnd - nameP <= 0) throw new ValueException(ErrorMessages.ERR_EMPTY_GROUP_NAME);

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
            throw new ValueException(ErrorMessages.ERR_MULTIPLEX_DEFINED_NAME, new String(name, nameP, nameEnd - nameP));
        }

        e.addBackref(backRef);
    }

    NameEntry nameToGroupNumbers(byte[]name, int nameP, int nameEnd) {
        return nameFind(name, nameP, nameEnd);
    }

    public int nameToBackrefNumber(byte[]name, int nameP, int nameEnd, Region region) {
        NameEntry e = nameToGroupNumbers(name, nameP, nameEnd);
        if (e == null) throw new ValueException(ErrorMessages.ERR_UNDEFINED_NAME_REFERENCE,
                                                new String(name, nameP, nameEnd - nameP));

        switch(e.backNum) {
        case 0:
            throw new InternalException(ErrorMessages.ERR_PARSER_BUG);
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

    public Iterator<NameEntry> namedBackrefIterator() {
        return nameTable.iterator();
    }

    public boolean noNameGroupIsActive(Syntax syntax) {
        if (isDontCaptureGroup(options)) return false;
        
        if (Config.USE_NAMED_GROUP) {
            if (numberOfNames() > 0 && syntax.captureOnlyNamedGroup() && !isCaptureGroup(options)) return false;
        }
        return true;
    }

    /* set skip map for Boyer-Moor search */
    void setupBMSkipMap() {
        byte[]bytes = exact;
        int p = exactP;
        int end = exactEnd;
        int len = end - p;

        if (len < Config.CHAR_TABLE_SIZE) {
            // map/skip
            if (map == null) map = new byte[Config.CHAR_TABLE_SIZE];

            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) map[i] = (byte)len;
            for (int i=0; i<len-1; i++) map[bytes[p + i] & 0xff] = (byte)(len - 1 -i); // oxff ??
        } else {
            if (intMap == null) intMap = new int[Config.CHAR_TABLE_SIZE];
            
            for (int i=0; i<len-1; i++) intMap[bytes[p + i] & 0xff] = len - 1 - i; // oxff ??
        }
    }

    void setExactInfo(OptExactInfo e) {
        if (e.length == 0) return;

        // shall we copy that ?
        exact = e.s;
        exactP = 0;
        exactEnd = e.length;

        if (e.ignoreCase) {
            // encodings won't return toLowerTable for case insensitive search if it's not safe to use it directly
            searchAlgorithm = enc.toLowerCaseTable() != null ? SearchAlgorithm.SLOW_IC_SB : new SearchAlgorithm.SLOW_IC(this);
        } else {
            boolean allowReverse = enc.isReverseMatchAllowed(exact, exactP, exactEnd);
            
            if (e.length >= 3 || (e.length >= 2 && allowReverse)) {
                setupBMSkipMap();
                if (allowReverse) {
                    searchAlgorithm = SearchAlgorithm.BM;
                } else {
                    searchAlgorithm = SearchAlgorithm.BM_NOT_REV;                    
                }
            } else {
                searchAlgorithm = enc.isSingleByte() ? SearchAlgorithm.SLOW_SB : SearchAlgorithm.SLOW;
            }
        }
        
        dMin = e.mmd.min;
        dMax = e.mmd.max;
        
        if (dMin != MinMaxLen.INFINITE_DISTANCE) {
            thresholdLength = dMin + (exactEnd - exactP);
        }
    }

    void setOptimizeMapInfo(OptMapInfo m) {
        /*
        for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) {
            map[i] = m.map[i]; // do we really have to copy that ???
        }
        */
        map = m.map;
        
        searchAlgorithm = enc.isSingleByte() ? SearchAlgorithm.MAP_SB : SearchAlgorithm.MAP;
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
        searchAlgorithm = SearchAlgorithm.NONE;
        anchor = 0;
        anchorDmax = 0;
        anchorDmin = 0;
        subAnchor = 0;
        
        exact = null;
        exactP = exactEnd = 0;
    }
    
    public String encStringToString(byte[]bytes, int p, int end) {
        StringBuilder sb = new StringBuilder("\nPATTERN: /");
        
        if (enc.minLength() > 1) {
            int p_ = p;
            while (p_ < end) {
                int code = enc.mbcToCode(bytes, p_, end);
                if (code >= 0x80) {
                    try {
                        sb.append(String.format(" 0x%04x ", code));
                    } catch (IllegalFormatConversionException ifce) {
                        sb.append(code);
                    }
                } else {
                    sb.append((char)code);
                }
                p_ += enc.length(bytes, p_, end);
            }
        } else {
            while (p < end) {
                sb.append(new String(new byte[]{bytes[p]}));
                p++;
            }
        }
        return sb.append("/").toString();
    }
    
    public String optimizeInfoToString() {
        String s = "";
        s += "optimize: " + searchAlgorithm.getName() + "\n";
        s += "  anchor:     " + OptAnchorInfo.anchorToString(anchor);
        
        if ((anchor & AnchorType.END_BUF_MASK) != 0) {
            s += MinMaxLen.distanceRangeToString(anchorDmin, anchorDmax);
        }

        s += "\n";

        if (searchAlgorithm != SearchAlgorithm.NONE) {
            s += "  sub anchor: " + OptAnchorInfo.anchorToString(subAnchor) + "\n";
        }

        s += "dmin: " + dMin + " dmax: " + dMax + "\n";
        s += "threshold length: " + thresholdLength + "\n";

        if (exact != null) {
            s += "exact: [" + new String(exact, exactP, exactEnd - exactP) + "]: length: " + (exactEnd - exactP) + "\n"; 
        } else if (searchAlgorithm == SearchAlgorithm.MAP || searchAlgorithm == SearchAlgorithm.MAP_SB) {
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
