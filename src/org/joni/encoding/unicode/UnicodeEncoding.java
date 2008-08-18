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
package org.joni.encoding.unicode;

import org.joni.ApplyAllCaseFoldFunction;
import org.joni.CaseFoldCodeItem;
import org.joni.CodeRangeBuffer;
import org.joni.Config;
import org.joni.IntHolder;
import org.joni.constants.CharacterType;
import org.joni.encoding.MultiByteEncoding;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;


public abstract class UnicodeEncoding extends MultiByteEncoding {
    private static final int PROPERTY_NAME_MAX_SIZE = 20;

    protected UnicodeEncoding(int minLength, int maxLength, int[]EncLen) {
        // ASCII type tables for all Unicode encodings
        super(minLength, maxLength, EncLen, null, UNICODE_ISO_8859_1_CTypeTable);
    }

    protected UnicodeEncoding(int minLength, int maxLength, int[]EncLen, int[][]Trans) {
        // ASCII type tables for all Unicode encodings        
        super(minLength, maxLength, EncLen, Trans, UNICODE_ISO_8859_1_CTypeTable);
    }

    // onigenc_unicode_is_code_ctype
    @Override
    public final boolean isCodeCType(int code, int ctype) {
        if (Config.USE_UNICODE_PROPERTIES) {
            if (ctype <= CharacterType.MAX_STD_CTYPE && code < 256)
                return isCodeCTypeInternal(code, ctype); 
        } else {
            if (code < 256) return isCodeCTypeInternal(code, ctype);
        }

        if (ctype > UnicodeCodeRanges.CodeRangeTable.length) throw new InternalError(ErrorMessages.ERR_TYPE_BUG);

        return CodeRangeBuffer.isInCodeRange(UnicodeCodeRanges.CodeRangeTable[ctype], code);
        
    }
    
    // onigenc_unicode_ctype_code_range
    protected final int[]ctypeCodeRange(int ctype) {
        if (ctype >= UnicodeCodeRanges.CodeRangeTable.length) throw new InternalError(ErrorMessages.ERR_TYPE_BUG);
        
        return UnicodeCodeRanges.CodeRangeTable[ctype];
    }
    
    // onigenc_unicode_property_name_to_ctype
    @Override
    public int propertyNameToCType(byte[]name, int p, int end) {
        byte[]buf = new byte[PROPERTY_NAME_MAX_SIZE];
        
        int p_ = p;
        int len = 0;
        
        while(p_ < end) {
            int code = mbcToCode(name, p_, end);
            if (code >= 0x80) throw new ValueException(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME);
            buf[len++] = (byte)code;
            if (len >= PROPERTY_NAME_MAX_SIZE) throw new ValueException(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME, name, p, end);
            p_ += length(name, p_, end);
        }

        Integer ctype = UnicodeCTypeNames.CTypeNameHash.get(buf, 0, len);
        if (ctype == null) throw new ValueException(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME, name, p, end);
        return ctype;       
    }
    
    // onigenc_unicode_mbc_case_fold
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]fold) {
        int p = pp.value;
        int foldP = 0;
        
        int code = mbcToCode(bytes, p, end);
        int len = length(bytes, p, end);
        pp.value += len;
        
        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
            if ((flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) != 0) {
                if (code == 0x0049) {
                    return codeToMbc(0x0131, fold, foldP);
                } else if (code == 0x0130) {
                    return codeToMbc(0x0069, fold, foldP);
                }
            }
        }
        
        int to[] = UnicodeCaseFolds.FoldHash.get(code);
        if (to != null) {
            if (to.length == 1) {
                return codeToMbc(to[0], fold, foldP);
            } else {
                int rlen = 0;
                for (int i=0; i<to.length; i++) {
                    len = codeToMbc(to[i], fold, foldP); 
                    foldP += len;
                    rlen += len;
                }
                return rlen;
            }
        }

        for (int i=0; i<len; i++) {
            fold[foldP++] = bytes[p++];
        }
        return len;
    }
    
    // onigenc_unicode_apply_all_case_fold
    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        /* if (CaseFoldInited == 0) init_case_fold_table(); */      

        int[]code = new int[]{0};
        for (int i=0; i<UnicodeCaseFolds.CaseUnfold_11_From.length; i++) {
            int from = UnicodeCaseFolds.CaseUnfold_11_From[i];
            int[]to = UnicodeCaseFolds.CaseUnfold_11_To[i];
            
            for (int j=0; j<to.length; j++) {
                code[0] = from;
                fun.apply(to[j], code, 1, arg);
                
                code[0] = to[j];
                fun.apply(from, code, 1, arg);
                
                for (int k=0; k<j; k++) {
                    code[0] = to[k];
                    fun.apply(to[j], code, 1, arg);
                    
                    code[0] = to[j];
                    fun.apply(to[k], code, 1, arg);
                }
                
            }
        }
        
        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI && (flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) != 0) {
            code[0] = 0x0131;
            fun.apply(0x0049, code, 1, arg);
            code[0] = 0x0049;
            fun.apply(0x0131, code, 1, arg);
            code[0] = 0x0130;
            fun.apply(0x0069, code, 1, arg);
            code[0] = 0x0069;
            fun.apply(0x0130, code, 1, arg);
        } else {
            for (int i=0; i<UnicodeCaseFolds.CaseUnfold_11_Locale_From.length; i++) {
                int from = UnicodeCaseFolds.CaseUnfold_11_Locale_From[i];
                int[]to = UnicodeCaseFolds.CaseUnfold_11_Locale_To[i];
            
                for (int j=0; j<to.length; j++) {
                    code[0] = from;
                    fun.apply(to[j], code, 1, arg);
                
                    code[0] = to[j];
                    fun.apply(from, code, 1, arg);
                
                    for (int k = 0; k<j; k++) {
                        code[0] = to[k];
                        fun.apply(to[j], code, 1, arg);
                    
                        code[0] = to[j];
                        fun.apply(to[k], code, 1, arg);
                    }
                }
            }
        } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI
        
        if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
            for (int i=0; i<UnicodeCaseFolds.CaseUnfold_12.length; i+=2) {
                int[]from = UnicodeCaseFolds.CaseUnfold_12[i];
                int[]to = UnicodeCaseFolds.CaseUnfold_12[i + 1];
                for (int j=0; j<to.length; j++) {
                    fun.apply(to[j], from, 2, arg);
                    
                    for (int k=0; k<to.length; k++) {
                        if (k == j) continue;
                        code[0] = to[k];
                        fun.apply(to[j], code, 1, arg);
                    }
                }
            }

            if (!Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI || (flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) == 0) {
                for (int i=0; i<UnicodeCaseFolds.CaseUnfold_12_Locale.length; i+=2) {
                    int[]from = UnicodeCaseFolds.CaseUnfold_12_Locale[i];
                    int[]to = UnicodeCaseFolds.CaseUnfold_12_Locale[i + 1];
                    for (int j=0; j<to.length; j++) {
                        fun.apply(to[j], from, 2, arg);

                        for (int k=0; k<to.length; k++) {
                            if (k == j) continue;
                            code[0] = to[k];
                            fun.apply(to[j], code, 1, arg);
                        }
                    }
                }
            } // !USE_UNICODE_CASE_FOLD_TURKISH_AZERI
            
            for (int i=0; i<UnicodeCaseFolds.CaseUnfold_13.length; i+=2) {
                int[]from = UnicodeCaseFolds.CaseUnfold_13[i];
                int[]to = UnicodeCaseFolds.CaseUnfold_13[i + 1];
                
                for (int j=0; j<to.length; j++) {
                    fun.apply(to[j], from, 3, arg); //// ????
                    
                    for (int k=0; k<to.length; k++) {
                        if (k == j) continue;
                        code[0] = to[k];
                        fun.apply(to[j], code, 1, arg);
                    }
                }
            }
            
        } // INTERNAL_ENC_CASE_FOLD_MULTI_CHAR
    }
    
    // onigenc_unicode_get_case_fold_codes_by_str
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        int code = mbcToCode(bytes, p, end);
        int len = length(bytes, p, end);

        if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
            if ((flag & Config.ENC_CASE_FOLD_TURKISH_AZERI) != 0) {
                if (code == 0x0049) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(len, 1, new int[]{0x0131})};
                } else if(code == 0x0130) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(len, 1, new int[]{0x0069})};                    
                } else if(code == 0x0131) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(len, 1, new int[]{0x0049})};
                } else if(code == 0x0069) {
                    return new CaseFoldCodeItem[]{new CaseFoldCodeItem(len, 1, new int[]{0x0130})};
                }
            }
        } // USE_UNICODE_CASE_FOLD_TURKISH_AZERI
        
        int n = 0;
        int fn = 0;
        
        int[]to = UnicodeCaseFolds.FoldHash.get(code);
        CaseFoldCodeItem[]items = null;
        if (to != null) {
            items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];
            
            if (to.length == 1) {
                int origCode = code;
                
                items[0] = new CaseFoldCodeItem(len, 1, new int[]{to[0]});
                n++;
                
                code = to[0];
                to = UnicodeCaseFolds.Unfold1Hash.get(code);
                if (to != null) {
                    for (int i=0; i<to.length; i++) {
                        if (to[i] != origCode) {
                            items[n] = new CaseFoldCodeItem(len, 1, new int[]{to[i]});
                            n++;
                        }
                    }
                }
            } else if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
                int[][]cs = new int[3][4];
                int[]ncs = new int[3];
                
                for (fn=0; fn<to.length; fn++) {
                    cs[fn][0] = to[fn];
                    int[]z3 = UnicodeCaseFolds.Unfold1Hash.get(cs[fn][0]);
                    if (z3 != null) {
                        for (int i=0; i<z3.length; i++) {
                            cs[fn][i+1] = z3[i];
                        }
                        ncs[fn] = z3.length + 1;
                    } else {
                        ncs[fn] = 1;
                    }
                }

                if (fn == 2) {
                    for (int i=0; i<ncs[0]; i++) {
                        for (int j=0; j<ncs[1]; j++) {
                            items[n] = new CaseFoldCodeItem(len, 2, new int[]{cs[0][i], cs[1][j]});
                            n++;
                        }
                    }
                        
                    int[]z2 = UnicodeCaseFolds.Unfold2Hash.get(to);
                    if (z2 != null) {
                        for (int i=0; i<z2.length; i++) {
                            if (z2[i] == code) continue;
                            items[n] = new CaseFoldCodeItem(len, 1, new int[]{z2[i]});
                            n++;                                
                        }
                    }
                } else {
                    for (int i=0; i<ncs[0]; i++) {
                        for (int j=0; j<ncs[1]; j++) {
                            for (int k=0; k<ncs[2]; k++) {
                                items[n] = new CaseFoldCodeItem(len, 3, new int[]{cs[0][i], cs[1][j], cs[2][k]});
                                n++;
                            }
                        }
                    }
                    int[]z2 = UnicodeCaseFolds.Unfold3Hash.get(to);
                    if (z2 != null) {
                        for (int i=0; i<z2.length; i++) {
                            if (z2[i] == code) continue;
                            items[n] = new CaseFoldCodeItem(len, 1, new int[]{z2[i]});    
                            n++;
                        }
                    }
                }
                /* multi char folded code is not head of another folded multi char */
                flag = 0; /* DISABLE_CASE_FOLD_MULTI_CHAR(flag); */
            }
        } else {
            to = UnicodeCaseFolds.Unfold1Hash.get(code);
            if (to != null) {
                items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];
                for (int i=0; i<to.length; i++) {
                    items[n] = new CaseFoldCodeItem(len, 1, new int[]{to[i]});
                    n++;
                }
            }
        }
        
        if ((flag & Config.INTERNAL_ENC_CASE_FOLD_MULTI_CHAR) != 0) {
            if (items == null) items = new CaseFoldCodeItem[Config.ENC_GET_CASE_FOLD_CODES_MAX_NUM];
            
            p += len;
            if (p < end) {                
                int[]codes = new int[3];
                codes[0] = code;
                code = mbcToCode(bytes, p, end);
                to = UnicodeCaseFolds.FoldHash.get(code);
                if (to != null && to.length == 1) {
                    codes[1] = to[0]; 
                } else {
                    codes[1] = code;
                }
                    
                int clen = length(bytes, p, end);
                len += clen;
                int[]z2 = UnicodeCaseFolds.Unfold2Hash.get(codes);
                if (z2 != null) {
                    for (int i=0; i<z2.length; i++) {
                        items[n] = new CaseFoldCodeItem(len, 1, new int[]{z2[i]});
                        n++;
                    }
                }
                p += clen;
                if (p < end) {
                    code = mbcToCode(bytes, p, end);
                    to = UnicodeCaseFolds.FoldHash.get(code);
                    if (to != null && to.length == 1) {
                        codes[2] = to[0];
                    } else {
                        codes[2] = code;
                    }
                    clen = length(bytes, p, end);
                    len += clen;
                    z2 = UnicodeCaseFolds.Unfold3Hash.get(codes);
                    if (z2 != null) {
                        for (int i=0; i<z2.length; i++) {
                            items[n] = new CaseFoldCodeItem(len, 1, new int[]{z2[i]});
                            n++;
                        }
                    }
                }
            }
        }
        
        if (items == null || n == 0) return EMPTY_FOLD_CODES;
        if (n < items.length) {
            CaseFoldCodeItem [] tmp = new CaseFoldCodeItem[n];
            System.arraycopy(items, 0, tmp, 0, n);
            return tmp;
        } else {
            return items;
        }
    }
    
    static final short UNICODE_ISO_8859_1_CTypeTable[] = {
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4008, 0x428c, 0x4289, 0x4288, 0x4288, 0x4288, 0x4008, 0x4008,
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
          0x4284, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0, 0x78b0,
          0x78b0, 0x78b0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x41a0,
          0x41a0, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x7ca2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2, 0x74a2,
          0x74a2, 0x74a2, 0x74a2, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x51a0,
          0x41a0, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x78e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2, 0x70e2,
          0x70e2, 0x70e2, 0x70e2, 0x41a0, 0x41a0, 0x41a0, 0x41a0, 0x4008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0288, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
          0x0284, 0x01a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0,
          0x00a0, 0x00a0, 0x30e2, 0x01a0, 0x00a0, 0x00a8, 0x00a0, 0x00a0,
          0x00a0, 0x00a0, 0x10a0, 0x10a0, 0x00a0, 0x30e2, 0x00a0, 0x01a0,
          0x00a0, 0x10a0, 0x30e2, 0x01a0, 0x10a0, 0x10a0, 0x10a0, 0x01a0,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x00a0,
          0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x34a2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x00a0,
          0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2, 0x30e2
    };      
}
