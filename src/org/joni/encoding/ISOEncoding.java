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

import org.joni.Config;
import org.joni.IntHolder;

public abstract class ISOEncoding extends CaseFoldMapEncoding {
    
    protected ISOEncoding(short[]CTypeTable, byte[]LowerCaseTable, int[][]CaseFoldMap) {
        this(CTypeTable, LowerCaseTable, CaseFoldMap, true);
    }
    
    protected ISOEncoding(short[]CTypeTable, byte[]LowerCaseTable, int[][]CaseFoldMap, boolean foldFlag) {
        super(CTypeTable, LowerCaseTable, CaseFoldMap, foldFlag);
    }

    /** iso_*_mbc_case_fold
     */
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        int p = pp.value;
        int lowerP = 0;
        
        if (bytes[p] == (byte)0xdf && (flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
            lower[lowerP++] = 's';
            lower[lowerP] = 's';
            pp.value++;
            return 2;
        }
        
        lower[lowerP] = LowerCaseTable[bytes[p] & 0xff];
        pp.value++;
        return 1;
    }    

    @Override
    public boolean isCodeCType(int code, int ctype) {
        return code < 256 ? isCodeCTypeInternal(code, ctype) : false;
    }   
}
