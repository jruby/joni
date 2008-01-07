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

import org.joni.IntHolder;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;

public abstract class SingleByteEncoding extends AbstractEncoding {

    protected final byte[]LowerCaseTable; 
    
    protected SingleByteEncoding(short[]CTypeTable, byte[]LowerCaseTable) {
        super(CTypeTable);
        this.LowerCaseTable = LowerCaseTable;
    }
    
    /** onigenc_single_byte_mbc_enc_len
     */
    @Override
    public final int length(byte c) {
        return 1;
    }

    @Override
    public final int strLength(byte[]bytes, int p, int end) {
        return end - p;
    }    

    @Override
    public final int maxLength() {
        return 1;
    }

    @Override   
    public final int minLength() {
        return 1;
    }

    // onigenc_is_mbc_newline_0x0a here
    
    /** onigenc_single_byte_mbc_to_code
     */
    @Override
    public int mbcToCode(byte[]bytes, int p, int end){
        return bytes[p] & 0xff;
    }

    /** onigenc_single_byte_code_to_mbclen
     */
    @Override
    public final int codeToMbcLength(int code) {
        if (code < 0x100) return 1;
        throw new ValueException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
    }

    /** onigenc_single_byte_code_to_mbc
     */
    @Override
    public final int codeToMbc(int code, byte[]bytes, int p) {
        bytes[p] = (byte)(code & 0xff); // c implementation also uses mask here
        return 1;
    }

    /** onigenc_not_support_get_ctype_code_range
     */
    @Override
    public final int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        return null; 
    }
    
    /** onigenc_single_byte_left_adjust_char_head
     */
    @Override
    public final int leftAdjustCharHead(byte[]bytes, int start, int p) {
        return p;
    }
    
    /** onigenc_always_true_is_allowed_reverse_match
     */
    @Override
    public final boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        return true;        
    }
    
    @Override
    public final boolean isSingleByte() {
        return true;
    }
    
    @Override
    public final boolean isFixedWidth() {
        return true;
    }
}
