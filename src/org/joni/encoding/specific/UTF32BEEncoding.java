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

public final class UTF32BEEncoding extends UnicodeEncoding {

    protected UTF32BEEncoding() {
        super(4, 4, null);
    }

    @Override
    public String toString() {
        return "UTF-32BE";
    }

    @Override
    public int length(byte c) { 
        return 4;       
    }

    @Override
    public int length(byte[]bytes, int p, int end) { 
        return 4;
    }

    @Override
    public int strLength(byte[]bytes, int p, int end) {
        return (end - p) >>> 2;
    }

    @Override
    public boolean isNewLine(byte[]bytes, int p, int end) {
        if (p + 3 < end) {
            if (bytes[p + 3] == (byte)0x0a && bytes[p + 2] == 0 && bytes[p + 1] == 0 && bytes[p] == 0) return true;
            
            if (Config.USE_UNICODE_ALL_LINE_TERMINATORS) {
                if ((Config.USE_CRNL_AS_LINE_TERMINATOR && bytes[p + 3] == (byte)0x0d) ||
                   bytes[p + 3] == (byte)0x85 && bytes[p + 2] == 0 && bytes[p + 1] == 0 && bytes[p] == 0) return true;

                if (bytes[p + 2] == (byte)0x20 &&
                   (bytes[p + 3] == (byte)0x29 || bytes[p + 3] == (byte)0x28) &&
                    bytes[p + 1] == 0 && bytes[p] == 0) return true;
            } // USE_UNICODE_ALL_LINE_TERMINATORS
        }
        return false;
    }
    
    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return (((bytes[p] & 0xff) * 256 + (bytes[p + 1] & 0xff)) * 256 + (bytes[p + 2] & 0xff)) * 256 + (bytes[p + 3] & 0xff);
    }    
    
    @Override
    public int codeToMbcLength(int code) {
        return 4; 
    }
    
    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {    
        int p_ = p;
        bytes[p_++] = (byte)((code & 0xff000000) >>> 24);
        bytes[p_++] = (byte)((code & 0xff0000)   >>> 16);
        bytes[p_++] = (byte)((code & 0xff00)     >>> 8);
        bytes[p_++] = (byte) (code & 0xff);
        return 4;
    }    

    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]fold) {
        int p = pp.value;
        int foldP = 0;
        if (isAscii(bytes[p + 3] & 0xff) && bytes[p + 2] == 0 && bytes[p + 1] == 0 && bytes[p] == 0) {
            fold[foldP++] = 0;
            fold[foldP++] = 0;

            if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
                if ((flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) != 0) {
                    if (bytes[p + 3] == (byte)0x49) {
                        fold[foldP++] = (byte)0x01;
                        fold[foldP] = (byte)0x31;
                        pp.value += 4;
                        return 4;
                    }
                }
            } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI

            fold[foldP++] = 0;
            fold[foldP] = ASCIIEncoding.AsciiToLowerCaseTable[bytes[p + 3] & 0xff];
            pp.value += 4;            
            return 4;
        } else {
            return super.mbcCaseFold(flag, bytes, pp, end, fold);
        }
    }
    
    /** onigenc_utf16_32_get_ctype_code_range
     */
    @Override
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        sbOut.value = 0x00;
        return super.ctypeCodeRange(ctype);
    }
    
    @Override
    public int leftAdjustCharHead(byte[]bytes, int p, int end) {
        if (end <= p) return end;

        return end - ((end - p) % 4); 
    }    
    
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return false;
    }    
    
    public static UTF32BEEncoding INSTANCE = new UTF32BEEncoding();
}
