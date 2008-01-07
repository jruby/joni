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
package org.joni.encoding.specific;

import org.joni.Config;
import org.joni.IntHolder;
import org.joni.encoding.unicode.UnicodeEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;

public final class UTF8Encoding extends UnicodeEncoding {
    static final boolean USE_INVALID_CODE_SCHEME = true; 

    protected UTF8Encoding() {
        super(UTF8EncLen);
    }
    
    @Override
    public String toString() {
        return "UTF-8";
    }
    
    @Override
    public int maxLength() {
        return 6;
    }
    
    @Override
    public int minLength() {
        return 1;
    }
    
    @Override
    public boolean isFixedWidth() {
        return false;
    }   
    
    @Override
    public boolean isNewLine(byte[]bytes, int p, int end) {
        if (p < end) {
            if (bytes[p] == (byte)0x0a) return true;
            
            if (Config.USE_UNICODE_ALL_LINE_TERMINATORS) {
                if (!Config.USE_CRNL_AS_LINE_TERMINATOR) {
                    if (bytes[p] == (byte)0x0d) return true;
                }
                
                if (p + 1 < end) { // & 0xff...
                    if (bytes[p+1] == (byte)0x85 && bytes[p] == (byte)0xc2) return true; /* U+0085 */
                    if (p + 2 < end) {
                        if ((bytes[p+2] == (byte)0xa8 || bytes[p+2] == (byte)0xa9) &&
                            bytes[p+1] == (byte)0x80 && bytes[p] == (byte)0xe2) return true; /* U+2028, U+2029 */
                    }
                }
            } // USE_UNICODE_ALL_LINE_TERMINATORS
        }
        return false;
    }

    private static final int INVALID_CODE_FE = 0xfffffffe;
    private static final int INVALID_CODE_FF = 0xffffffff;
    // private static final int VALID_CODE_LIMIT = 0x7fffffff;
    @Override
    public int codeToMbcLength(int code) {
        if ((code & 0xffffff80) == 0) {
            return 1;
        } else if ((code & 0xfffff800) == 0) {
            return 2;
        } else if ((code & 0xffff0000) == 0) {
            return 3;
        } else if ((code & 0xffe00000) == 0) {
            return 4;
        } else if ((code & 0xfc000000) == 0) {
            return 5;
        } else if ((code & 0x80000000) == 0) {
            return 6;
        } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FE) {
            return 1;
        } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FF) {
            return 1;
        } else {
            throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
        }
    }
    
    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        int len = length(bytes[p]);
        
        int c = bytes[p++] & 0xff;
        
        if (len > 1) {
            len--;
            int n = c & ((1 << (6 - len)) - 1);
            while (len-- != 0) {
                c = bytes[p++] & 0xff;
                n = (n << 6) | (c & ((1 << 6) - 1));
            }
            return n;
        } else {
            if (USE_INVALID_CODE_SCHEME) {
                if (c > 0xfd) return c == 0xfe ? INVALID_CODE_FE : INVALID_CODE_FF;
            }
            return c;
        }
    }
    
    static byte trailS(int code, int shift) {
        return (byte)((((code) >>> (shift)) & 0x3f) | 0x80);
    }
    
    static byte trail0(int code) {
        return (byte)(((code) & 0x3f) | 0x80);
    }
    
    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xffffff80) == 0) {
            bytes[p_] = (byte)code;
            return 1;
        } else {
            if ((code & 0xfffff800) == 0) {
                bytes[p_++] = (byte)(((code >>> 6) & 0x1f) | 0xc0);
            } else if ((code & 0xffff0000) == 0) {
                bytes[p_++] = (byte)(((code >>> 12) & 0x0f) | 0xe0);
                bytes[p_++] = trailS(code, 6);
            } else if ((code & 0xffe00000) == 0) {
                bytes[p_++] = (byte)(((code >>> 18) & 0x07) | 0xf0);
                bytes[p_++] = trailS(code, 12);
                bytes[p_++] = trailS(code, 6);
            } else if ((code & 0xfc000000) == 0) {
                bytes[p_++] = (byte)(((code >>> 24) & 0x03) | 0xf8);
                bytes[p_++] = trailS(code, 18);
                bytes[p_++] = trailS(code, 12);
                bytes[p_++] = trailS(code, 6);
            } else if ((code & 0x80000000) == 0) {
                bytes[p_++] = (byte)(((code >>> 30) & 0x01) | 0xfc);
                bytes[p_++] = trailS(code, 24);
                bytes[p_++] = trailS(code, 18);
                bytes[p_++] = trailS(code, 12);
                bytes[p_++] = trailS(code, 6);
            } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FE) {
                bytes[p_] = (byte)0xfe;
                return 1;
            } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FF) {
                bytes[p_] = (byte)0xff;
                return 1;
            } else {
                throw new ValueException(ErrorMessages.ERR_TOO_BIG_WIDE_CHAR_VALUE);
            }
            bytes[p_++] = trail0(code);
            return p_ - p; 
          }
    }

    // utf8_mbc_case_fold
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]fold) {
        int p = pp.value;
        int foldP = 0;
        
        if (isMbcAscii(bytes[p])) {
            
            if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
                if ((flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) != 0) {
                    if (bytes[p] == (byte)0x49) {
                        fold[foldP++] = (byte)0xc4l;
                        fold[foldP] = (byte)0xb1;
                        pp.value++;
                        return 2;
                    }
                }
            } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI
            
            fold[foldP] = ASCIIEncoding.AsciiToLowerCaseTable[bytes[p] & 0xff];
            pp.value++;
            return 1; /* return byte length of converted char to lower */
        } else {
            return super.mbcCaseFold(flag, bytes, pp, end, fold);
        }
    }
    
    /** utf8_get_ctype_code_range
     */
    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        sbOut.value = 0x80;
        return super.ctypeCodeRange(ctype); // onigenc_unicode_ctype_code_range
    }
    
    private static boolean utf8IsLead(int c) {
        return ((c & 0xc0) & 0xff) != 0x80;
    }
    
    /** utf8_left_adjust_char_head
     */
    @Override
    public int leftAdjustCharHead(byte[]bytes, int p, int end) {
        if (end <= p) return end;
        int p_ = end;
        while (!utf8IsLead(bytes[p_] & 0xff) && p_ > p) p_--;
        return p_;
    }
    
    /** onigenc_always_true_is_allowed_reverse_match
     */
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return true;
    }
    
    static final int UTF8EncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 1, 1
    };  

    public static final UTF8Encoding INSTANCE = new UTF8Encoding();    
}
