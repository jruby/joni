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
package org.joni.encoding;

import org.joni.ApplyAllCaseFoldFunction;
import org.joni.CaseFoldCodeItem;
import org.joni.IntHolder;
import org.joni.constants.CharacterType;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;
import org.joni.util.BytesHash;

public abstract class Encoding {
    protected byte[]name;
    protected int hashCode;

    @Override
    public abstract String toString();

    @Override
    public final boolean equals(Object other) {
        return this == other;       
    }
    
    @Override
    public final int hashCode() {
        if (name == null) getName();
        return hashCode;
    }

    public final byte[]getName() {
        if (name == null) {
            name = toString().getBytes();
            hashCode = BytesHash.hashCode(name, 0, name.length);
        }
        return name;
    }

    /**
     * Returns character length given the character head
     * returns <code>1</code> for singlebyte encodings or performs direct length table lookup for multibyte ones.   
     * 
     * @param   c
     *          Character head
     * Oniguruma equivalent: <code>mbc_enc_len</code>
     */
    public abstract int length(byte c);

    /**
     * Returns maximum character byte length that can appear in an encoding  
     * 
     * Oniguruma equivalent: <code>max_enc_len</code>
     */
    public abstract int maxLength();
    
    /* ONIGENC_MBC_MAXLEN_DIST */
    public final int maxLengthDistance() {
        return maxLength();
    }

    /**
     * Returns minimum character byte length that can appear in an encoding  
     * 
     * Oniguruma equivalent: <code>min_enc_len</code>
     */    
    public abstract int minLength();

    /**
     * Returns true if <code>bytes[p]</code> is a head of a new line character
     * 
     * Oniguruma equivalent: <code>is_mbc_newline</code>
     */
    public abstract boolean isNewLine(byte[]bytes, int p, int end);

    /**
     * Returns code point for a character
     * 
     * Oniguruma equivalent: <code>mbc_to_code</code>
     */
    public abstract int mbcToCode(byte[]bytes, int p, int end);

    /**
     * Returns character length given a code point
     * 
     * Oniguruma equivalent: <code>code_to_mbclen</code>
     */
    public abstract int codeToMbcLength(int code);

    /**
     * Extracts code point into it's multibyte representation
     * 
     * @return character length for the given code point 
     * 
     * Oniguruma equivalent: <code>code_to_mbc</code>
     */
    public abstract int codeToMbc(int code, byte[]bytes, int p);

    /**
     * Performs case folding for a character at <code>bytes[pp.value]</code>
     * 
     * @param   flag    case fold flag
     * @param   pp      an <code>IntHolder</code> that points at character head
     * @param   to      a buffer where to extract case folded character
     *
     * Oniguruma equivalent: <code>mbc_case_fold</code>
     */
    public abstract int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]to);
    
    /**
     * Returns lower case table if it's safe to use it directly, otherwise <code>null</code>
     * Used for fast case insensitive matching for some singlebyte encodings
     * 
     * @return lower case table 
     */
    public byte[] toLowerCaseTable() {return null;}

    /**
     * Expand case folds given a character class (used for case insensitive matching)
     * 
     * @param   flag    case fold flag
     * @param   fun     case folding functor (look at: <code>ApplyCaseFold</code>)
     * @param   arg     case folding functor argument (look at: <code>ApplyCaseFoldArg</code>)
     * 
     * Oniguruma equivalent: <code>apply_all_case_fold</code>
     */
    public abstract void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg);
    
    /**
     * Expand AST string nodes into their folded alternatives (look at: <code>Analyser.expandCaseFoldString</code>)
     * 
     * Oniguruma equivalent: <code>get_case_fold_codes_by_str</code>
     */
    public abstract CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end);
    
    /**
     * Returns character type given character type name (used when e.g. \p{Alpha})
     * 
     * Oniguruma equivalent: <code>property_name_to_ctype</code>
     */
    public abstract int propertyNameToCType(byte[]bytes, int p, int end);

    /**
     * Perform a check whether given code is of given character type (e.g. used by isWord(someByte) and similar methods)
     * 
     * @param   code    a code point of a character
     * @param   ctype   a character type to check against
     * 
     * Oniguruma equivalent: <code>is_code_ctype</code>
     */
    public abstract boolean isCodeCType(int code, int ctype);

    /**
     * Returns code range for a given character type
     * 
     * Oniguruma equivalent: <code>get_ctype_code_range</code>
     */
    public abstract int[]ctypeCodeRange(int ctype, IntHolder sbOut);

    /**
     * Seeks the previous character head in a stream
     * 
     * Oniguruma equivalent: <code>left_adjust_char_head</code>
     */
    public abstract int leftAdjustCharHead(byte[]bytes, int p, int end);

    /**
     * Returns true if it's safe to use reversal Boyer-Moore search fail fast algorithm
     * 
     * Oniguruma equivalent: <code>is_allowed_reverse_match</code>
     */
    public abstract boolean isReverseMatchAllowed(byte[]bytes, int p, int end);
    
    /* onigenc_get_right_adjust_char_head / ONIGENC_LEFT_ADJUST_CHAR_HEAD */
    public final int rightAdjustCharHead(byte[]bytes, int p, int end) {
        int p_ = leftAdjustCharHead(bytes, p, end);
        if (p_ < end) p_ += length(bytes[p_]);
        return p_;
    }

    /* onigenc_get_right_adjust_char_head_with_prev */
    public final int rightAdjustCharHeadWithPrev(byte[]bytes, int p, int end, IntHolder prev) {
        int p_ = leftAdjustCharHead(bytes, p, end);
        if (p_ < end) {
            if (prev != null) prev.value = p_;
            p_ += length(bytes[p_]);
        } else {
            if (prev != null) prev.value = -1; /* Sorry */
        }
        return p_;
    }

    /* onigenc_get_prev_char_head */
    public final int prevCharHead(byte[]bytes, int p, int end) {
        if (end <= p) return -1; // ??      
        return leftAdjustCharHead(bytes, p, end - 1);     
    }

    /* onigenc_step_back */
    public final int stepBack(byte[]bytes, int p, int end, int n) {
        while (end != -1 && n-- > 0) {
            if (end <= p) return -1;
            end = leftAdjustCharHead(bytes, p, end - 1);
        }
        return end;
    }
    
    /* onigenc_step */
    public final int step(byte[]bytes, int p, int end, int n) {
        int q = p;
        while (n-- > 0) {
            q += length(bytes[q]);
        }
        return q <= end ? q : -1;
    }
    
    /* onigenc_strlen */
    public int strLength(byte[]bytes, int p, int end) {
        int n = 0;
        int q = p;
        while (q < end) {
            q += length(bytes[q]);
            n++;
        }
        return n;
    }
    
    /* onigenc_strlen_null */
    public final int strLengthNull(byte[]bytes, int p) {
        int n = 0;
        
        while(true) {
            if (bytes[p] == 0) {
                int len = minLength();
                
                if (len == 1) return n;
                int q = p + 1;
                
                while (len > 1) {
                    if (bytes[q] != 0) break;
                    q++;
                    len--;
                }
                if (len == 1) return n;
            }
            p += length(bytes[p]);
            n++;
        }        
    }
    
    /* onigenc_str_bytelen_null */
    public final int strByteLengthNull(byte[]bytes, int p) {
        int p_, start;
        p_ = start = 0;
        
        while(true) {
            if (bytes[p_] == 0) {
                int len = minLength();
                if (len == 1) return p_ - start;
                int q = p_ + 1;
                while (len > 1) {
                    if (q >= bytes.length) return p_ - start;
                    if (bytes[q] != 0) break;
                    q++;
                    len--;
                }
                if (len == 1) return p_ - start;
            }
            p_ += length(bytes[p_]);
        }   
    }
    
    /* onigenc_with_ascii_strncmp */
    public final int strNCmp(byte[]bytes, int p, int end, byte[]ascii, int asciiP, int n) {
        while (n-- > 0) {
            if (p >= end) return ascii[asciiP];
            int c = mbcToCode(bytes, p, end);
            int x = ascii[asciiP] - c;
            if (x != 0) return x;
            
            asciiP++;
            p += length(bytes[p]);
        }
        return 0;
    }   

    public final boolean isNewLine(int code) {
        return isCodeCType(code, CharacterType.NEWLINE);
    }
    
    public final boolean isGraph(int code) {
        return isCodeCType(code, CharacterType.GRAPH);
    }
    
    public final boolean isPrint(int code) {
        return isCodeCType(code, CharacterType.PRINT);
    }
    
    public final boolean isAlnum(int code) {
        return isCodeCType(code, CharacterType.ALNUM);
    }
    
    public final boolean isAlpha(int code) {
        return isCodeCType(code, CharacterType.ALPHA);
    }
    
    public final boolean isLower(int code) {
        return isCodeCType(code, CharacterType.LOWER);
    }
    
    public final boolean isUpper(int code) {
        return isCodeCType(code, CharacterType.UPPER);
    }
    
    public final boolean isCntrl(int code) {
        return isCodeCType(code, CharacterType.CNTRL);
    }
    
    public final boolean isPunct(int code) {
        return isCodeCType(code, CharacterType.PUNCT);
    }
    
    public final boolean isSpace(int code) {
        return isCodeCType(code, CharacterType.SPACE);
    }
    
    public final boolean isBlank(int code) {
        return isCodeCType(code, CharacterType.BLANK);
    }
    
    public final boolean isDigit(int code) {
        return isCodeCType(code, CharacterType.DIGIT);
    }
    
    public final boolean isXDigit(int code) {
        return isCodeCType(code, CharacterType.XDIGIT);
    }
    
    public final boolean isWord(int code) {
        return isCodeCType(code, CharacterType.WORD);
    }

    // ONIGENC_IS_MBC_WORD
    public final boolean isMbcWord(byte[]bytes, int p, int end) {
        return isWord(mbcToCode(bytes, p, end));
    }

    // IS_CODE_SB_WORD
    public final boolean isSbWord(int code) {
        return isAscii(code) && isWord(code);
    }

    // ONIGENC_IS_MBC_HEAD
    public final boolean isMbcHead(byte b) {
        return length(b) != 1;
    }
    
    public boolean isMbcCrnl(byte[]bytes, int p, int end) {
        return mbcToCode(bytes, p, end) == 13 && isNewLine(bytes, p + length(bytes[p]), end);
    }    

    // ============================================================
    // helpers
    // ============================================================
    public static int digitVal(int code) {
        return code - '0';
    }
    
    public static int odigitVal(int code) {
        return digitVal(code);
    }
    
    public final int xdigitVal(int code) {
        if (isDigit(code)) {
            return digitVal(code);
        } else {
            return isUpper(code) ? code - 'A' + 10 : code - 'a' + 10;
        }
    }

    // ONIGENC_IS_MBC_ASCII
    public static boolean isMbcAscii(byte b) {
        return (b & 0xff) < 128; // b > 0 ? 
    }
    
    // ONIGENC_IS_CODE_ASCII
    public static boolean isAscii(int code) {
        return code < 128; 
    }
    
    public static int asciiToLower(int c) {
        return AbstractEncoding.AsciiToLowerCaseTable[c];
    }
    
    public static int asciiToUpper(int c) {
        return AbstractEncoding.AsciiToUpperCaseTable[c];
    }
    
    public static boolean isWordGraphPrint(int ctype) {
        return ctype == CharacterType.WORD ||
               ctype == CharacterType.GRAPH ||
               ctype == CharacterType.PRINT;
    } 
    
    public final int mbcodeStartPosition() {
        return minLength() > 1 ? 0 : 0x80;      
    }

    public abstract boolean isSingleByte();
    public abstract boolean isFixedWidth();
    
    public static final byte NEW_LINE = (byte)0x0a;

    public static Encoding load(String name) { 
        String encClassName = "org.joni.encoding.specific." + name + "Encoding";

        Class<?> encClass;
        try {
            encClass = Class.forName(encClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new ValueException(ErrorMessages.ERR_ENCODING_CLASS_DEF_NOT_FOUND, encClassName);
        }

        try {
            return (Encoding)encClass.getField("INSTANCE").get(encClass);
        } catch (Exception e) {
            throw new ValueException(ErrorMessages.ERR_ENCODING_LOAD_ERROR, encClassName);
        }
    }
}
