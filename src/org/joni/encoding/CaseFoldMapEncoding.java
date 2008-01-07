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

public abstract class CaseFoldMapEncoding extends SingleByteEncoding {

    protected final int[][]CaseFoldMap;
    protected final boolean foldFlag;    
    
    protected CaseFoldMapEncoding(short[]CTypeTable, byte[]LowerCaseTable, int[][]CaseFoldMap) {
        this(CTypeTable, LowerCaseTable, CaseFoldMap, true);
    }
    
    protected CaseFoldMapEncoding(short[]CTypeTable, byte[]LowerCaseTable, int[][]CaseFoldMap, boolean foldFlag) {
        super(CTypeTable, LowerCaseTable);
        this.CaseFoldMap = CaseFoldMap;
        this.foldFlag = foldFlag;
    }
    
    /** onigenc_apply_all_case_fold_with_map
     */
    protected final int applyAllCaseFoldWithMap(int mapSize, int[][]map, boolean essTsettFlag, int flag,
                                          ApplyAllCaseFoldFunction fun, Object arg) {

        asciiApplyAllCaseFold(flag, fun, arg);
        int[]code = new int[]{0};
        
        for (int i=0; i<mapSize; i++) {
            code[0] = map[i][1];
            
            fun.apply(map[i][0], code, 1, arg);
            
            code[0] = map[i][0];
            fun.apply(map[i][1], code, 1, arg);
        }
        
        if (essTsettFlag) ssApplyAllCaseFold(flag, fun, arg);
        return 0;
    }

    static final int[] SS = new int []{0x73, 0x73};
    /** ss_apply_all_case_fold
     */
    private void ssApplyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        fun.apply(0xdf, SS, 2, arg);
    }
    
    /** onigenc_get_case_fold_codes_by_str_with_map
     */
    protected final CaseFoldCodeItem[]getCaseFoldCodesByStringWithMap(int mapSize, int[][]map, 
                                                  boolean essTsettFlag, int flag,
                                                  byte[]bytes, int p, int end) {
        int b = bytes[p] & 0xff;
        
        if (0x41 <= b && b <= 0x5a) {
            CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 1, new int[]{b + 0x20});

            if (b == 0x53 && essTsettFlag && end > p + 1 &&
               (bytes[p+1] == (byte)0x53 || bytes[p+1] == (byte)0x73)) { /* SS */
                CaseFoldCodeItem item1 = new CaseFoldCodeItem(2, 1, new int[]{0xdf});
                return new CaseFoldCodeItem[]{item0, item1};
            } else {
                return new CaseFoldCodeItem[]{item0};
            }
        } else if (0x61 <= b && b <= 0x7a) {
            CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 1, new int[]{b - 0x20});
            
            if (b == 0x73 && essTsettFlag && end >p + 1 &&
               (bytes[p+1] == (byte)0x73 || bytes[p+1] == (byte)0x53)) { /* ss */
                CaseFoldCodeItem item1 = new CaseFoldCodeItem(2, 1, new int[]{0xdf});

                return new CaseFoldCodeItem[]{item0, item1};
            } else {
                return new CaseFoldCodeItem[]{item0};
            }
        } else if (b == 0xdf && essTsettFlag) {
            CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 2, new int[]{'s', 's'});
            CaseFoldCodeItem item1 = new CaseFoldCodeItem(1, 2, new int[]{'S', 'S'});
            CaseFoldCodeItem item2 = new CaseFoldCodeItem(1, 2, new int[]{'s', 'S'});
            CaseFoldCodeItem item3 = new CaseFoldCodeItem(1, 2, new int[]{'S', 's'});

            return new CaseFoldCodeItem[]{item0, item1, item2, item3};
        } else {
            for (int i=0; i<mapSize; i++) {
                if (b == map[i][0]) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{map[i][1]})};
                } else if (b == map[i][1]) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{map[i][0]})};
                }
            }
        }
        return EMPTY_FOLD_CODES;
    }

    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        applyAllCaseFoldWithMap(CaseFoldMap.length, CaseFoldMap, foldFlag, flag, fun, arg);      
    }
    
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        return getCaseFoldCodesByStringWithMap(CaseFoldMap.length, CaseFoldMap, foldFlag, flag, bytes, p, end); 
    }
    
    @Override
    public boolean isCodeCType(int code, int ctype) {
        return code < 256 ? isCodeCTypeInternal(code, ctype) : false;
    }    
}
