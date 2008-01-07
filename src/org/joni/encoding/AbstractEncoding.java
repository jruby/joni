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
import org.joni.constants.PosixBracket;
import org.joni.exception.ErrorMessages;
import org.joni.exception.ValueException;

abstract class AbstractEncoding extends Encoding {

    private final short CTypeTable[];

    protected AbstractEncoding(short[]CTypeTable) {
        this.CTypeTable = CTypeTable;
    }

    /** CTYPE_TO_BIT
     */
    private static int CTypeToBit(int ctype) {
        return 1 << ctype;
    }   

    /** ONIGENC_IS_XXXXXX_CODE_CTYPE
     */
    protected final boolean isCodeCTypeInternal(int code, int ctype) {
        return (CTypeTable[code] & CTypeToBit(ctype)) != 0; 
    }    
    
    /** onigenc_is_mbc_newline_0x0a / used also by multibyte encodings
     *
     */
    @Override   
    public boolean isNewLine(byte[]bytes, int p, int end) {
        return p < end ? bytes[p] == (byte)0x0a : false;
    }

    protected final int asciiMbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        lower[0] = AsciiToLowerCaseTable[bytes[pp.value] & 0xff];
        pp.value++;
        return 1;        
    }

    /** onigenc_ascii_mbc_case_fold
     */
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        return asciiMbcCaseFold(flag, bytes, pp, end, lower);
    }

    public static final byte AsciiToLowerCaseTable[] = new byte[]{
        (byte)'\000', (byte)'\001', (byte)'\002', (byte)'\003', (byte)'\004', (byte)'\005', (byte)'\006', (byte)'\007',
        (byte)'\010', (byte)'\011', (byte)'\012', (byte)'\013', (byte)'\014', (byte)'\015', (byte)'\016', (byte)'\017',
        (byte)'\020', (byte)'\021', (byte)'\022', (byte)'\023', (byte)'\024', (byte)'\025', (byte)'\026', (byte)'\027',
        (byte)'\030', (byte)'\031', (byte)'\032', (byte)'\033', (byte)'\034', (byte)'\035', (byte)'\036', (byte)'\037',
        (byte)'\040', (byte)'\041', (byte)'\042', (byte)'\043', (byte)'\044', (byte)'\045', (byte)'\046', (byte)'\047',
        (byte)'\050', (byte)'\051', (byte)'\052', (byte)'\053', (byte)'\054', (byte)'\055', (byte)'\056', (byte)'\057',
        (byte)'\060', (byte)'\061', (byte)'\062', (byte)'\063', (byte)'\064', (byte)'\065', (byte)'\066', (byte)'\067',
        (byte)'\070', (byte)'\071', (byte)'\072', (byte)'\073', (byte)'\074', (byte)'\075', (byte)'\076', (byte)'\077',
        (byte)'\100', (byte)'\141', (byte)'\142', (byte)'\143', (byte)'\144', (byte)'\145', (byte)'\146', (byte)'\147',
        (byte)'\150', (byte)'\151', (byte)'\152', (byte)'\153', (byte)'\154', (byte)'\155', (byte)'\156', (byte)'\157',
        (byte)'\160', (byte)'\161', (byte)'\162', (byte)'\163', (byte)'\164', (byte)'\165', (byte)'\166', (byte)'\167',
        (byte)'\170', (byte)'\171', (byte)'\172', (byte)'\133', (byte)'\134', (byte)'\135', (byte)'\136', (byte)'\137',
        (byte)'\140', (byte)'\141', (byte)'\142', (byte)'\143', (byte)'\144', (byte)'\145', (byte)'\146', (byte)'\147',
        (byte)'\150', (byte)'\151', (byte)'\152', (byte)'\153', (byte)'\154', (byte)'\155', (byte)'\156', (byte)'\157',
        (byte)'\160', (byte)'\161', (byte)'\162', (byte)'\163', (byte)'\164', (byte)'\165', (byte)'\166', (byte)'\167',
        (byte)'\170', (byte)'\171', (byte)'\172', (byte)'\173', (byte)'\174', (byte)'\175', (byte)'\176', (byte)'\177',
        (byte)'\200', (byte)'\201', (byte)'\202', (byte)'\203', (byte)'\204', (byte)'\205', (byte)'\206', (byte)'\207',
        (byte)'\210', (byte)'\211', (byte)'\212', (byte)'\213', (byte)'\214', (byte)'\215', (byte)'\216', (byte)'\217',
        (byte)'\220', (byte)'\221', (byte)'\222', (byte)'\223', (byte)'\224', (byte)'\225', (byte)'\226', (byte)'\227',
        (byte)'\230', (byte)'\231', (byte)'\232', (byte)'\233', (byte)'\234', (byte)'\235', (byte)'\236', (byte)'\237',
        (byte)'\240', (byte)'\241', (byte)'\242', (byte)'\243', (byte)'\244', (byte)'\245', (byte)'\246', (byte)'\247',
        (byte)'\250', (byte)'\251', (byte)'\252', (byte)'\253', (byte)'\254', (byte)'\255', (byte)'\256', (byte)'\257',
        (byte)'\260', (byte)'\261', (byte)'\262', (byte)'\263', (byte)'\264', (byte)'\265', (byte)'\266', (byte)'\267',
        (byte)'\270', (byte)'\271', (byte)'\272', (byte)'\273', (byte)'\274', (byte)'\275', (byte)'\276', (byte)'\277',
        (byte)'\300', (byte)'\301', (byte)'\302', (byte)'\303', (byte)'\304', (byte)'\305', (byte)'\306', (byte)'\307',
        (byte)'\310', (byte)'\311', (byte)'\312', (byte)'\313', (byte)'\314', (byte)'\315', (byte)'\316', (byte)'\317',
        (byte)'\320', (byte)'\321', (byte)'\322', (byte)'\323', (byte)'\324', (byte)'\325', (byte)'\326', (byte)'\327',
        (byte)'\330', (byte)'\331', (byte)'\332', (byte)'\333', (byte)'\334', (byte)'\335', (byte)'\336', (byte)'\337',
        (byte)'\340', (byte)'\341', (byte)'\342', (byte)'\343', (byte)'\344', (byte)'\345', (byte)'\346', (byte)'\347',
        (byte)'\350', (byte)'\351', (byte)'\352', (byte)'\353', (byte)'\354', (byte)'\355', (byte)'\356', (byte)'\357',
        (byte)'\360', (byte)'\361', (byte)'\362', (byte)'\363', (byte)'\364', (byte)'\365', (byte)'\366', (byte)'\367',
        (byte)'\370', (byte)'\371', (byte)'\372', (byte)'\373', (byte)'\374', (byte)'\375', (byte)'\376', (byte)'\377',
    };
    
    public static final byte AsciiToUpperCaseTable[] = new byte[]{
        (byte)'\000', (byte)'\001', (byte)'\002', (byte)'\003', (byte)'\004', (byte)'\005', (byte)'\006', (byte)'\007',
        (byte)'\010', (byte)'\011', (byte)'\012', (byte)'\013', (byte)'\014', (byte)'\015', (byte)'\016', (byte)'\017',
        (byte)'\020', (byte)'\021', (byte)'\022', (byte)'\023', (byte)'\024', (byte)'\025', (byte)'\026', (byte)'\027',
        (byte)'\030', (byte)'\031', (byte)'\032', (byte)'\033', (byte)'\034', (byte)'\035', (byte)'\036', (byte)'\037',
        (byte)'\040', (byte)'\041', (byte)'\042', (byte)'\043', (byte)'\044', (byte)'\045', (byte)'\046', (byte)'\047',
        (byte)'\050', (byte)'\051', (byte)'\052', (byte)'\053', (byte)'\054', (byte)'\055', (byte)'\056', (byte)'\057',
        (byte)'\060', (byte)'\061', (byte)'\062', (byte)'\063', (byte)'\064', (byte)'\065', (byte)'\066', (byte)'\067',
        (byte)'\070', (byte)'\071', (byte)'\072', (byte)'\073', (byte)'\074', (byte)'\075', (byte)'\076', (byte)'\077',
        (byte)'\100', (byte)'\101', (byte)'\102', (byte)'\103', (byte)'\104', (byte)'\105', (byte)'\106', (byte)'\107',
        (byte)'\110', (byte)'\111', (byte)'\112', (byte)'\113', (byte)'\114', (byte)'\115', (byte)'\116', (byte)'\117',
        (byte)'\120', (byte)'\121', (byte)'\122', (byte)'\123', (byte)'\124', (byte)'\125', (byte)'\126', (byte)'\127',
        (byte)'\130', (byte)'\131', (byte)'\132', (byte)'\133', (byte)'\134', (byte)'\135', (byte)'\136', (byte)'\137',
        (byte)'\140', (byte)'\101', (byte)'\102', (byte)'\103', (byte)'\104', (byte)'\105', (byte)'\106', (byte)'\107',
        (byte)'\110', (byte)'\111', (byte)'\112', (byte)'\113', (byte)'\114', (byte)'\115', (byte)'\116', (byte)'\117',
        (byte)'\120', (byte)'\121', (byte)'\122', (byte)'\123', (byte)'\124', (byte)'\125', (byte)'\126', (byte)'\127',
        (byte)'\130', (byte)'\131', (byte)'\132', (byte)'\173', (byte)'\174', (byte)'\175', (byte)'\176', (byte)'\177',
        (byte)'\200', (byte)'\201', (byte)'\202', (byte)'\203', (byte)'\204', (byte)'\205', (byte)'\206', (byte)'\207',
        (byte)'\210', (byte)'\211', (byte)'\212', (byte)'\213', (byte)'\214', (byte)'\215', (byte)'\216', (byte)'\217',
        (byte)'\220', (byte)'\221', (byte)'\222', (byte)'\223', (byte)'\224', (byte)'\225', (byte)'\226', (byte)'\227',
        (byte)'\230', (byte)'\231', (byte)'\232', (byte)'\233', (byte)'\234', (byte)'\235', (byte)'\236', (byte)'\237',
        (byte)'\240', (byte)'\241', (byte)'\242', (byte)'\243', (byte)'\244', (byte)'\245', (byte)'\246', (byte)'\247',
        (byte)'\250', (byte)'\251', (byte)'\252', (byte)'\253', (byte)'\254', (byte)'\255', (byte)'\256', (byte)'\257',
        (byte)'\260', (byte)'\261', (byte)'\262', (byte)'\263', (byte)'\264', (byte)'\265', (byte)'\266', (byte)'\267',
        (byte)'\270', (byte)'\271', (byte)'\272', (byte)'\273', (byte)'\274', (byte)'\275', (byte)'\276', (byte)'\277',
        (byte)'\300', (byte)'\301', (byte)'\302', (byte)'\303', (byte)'\304', (byte)'\305', (byte)'\306', (byte)'\307',
        (byte)'\310', (byte)'\311', (byte)'\312', (byte)'\313', (byte)'\314', (byte)'\315', (byte)'\316', (byte)'\317',
        (byte)'\320', (byte)'\321', (byte)'\322', (byte)'\323', (byte)'\324', (byte)'\325', (byte)'\326', (byte)'\327',
        (byte)'\330', (byte)'\331', (byte)'\332', (byte)'\333', (byte)'\334', (byte)'\335', (byte)'\336', (byte)'\337',
        (byte)'\340', (byte)'\341', (byte)'\342', (byte)'\343', (byte)'\344', (byte)'\345', (byte)'\346', (byte)'\347',
        (byte)'\350', (byte)'\351', (byte)'\352', (byte)'\353', (byte)'\354', (byte)'\355', (byte)'\356', (byte)'\357',
        (byte)'\360', (byte)'\361', (byte)'\362', (byte)'\363', (byte)'\364', (byte)'\365', (byte)'\366', (byte)'\367',
        (byte)'\370', (byte)'\371', (byte)'\372', (byte)'\373', (byte)'\374', (byte)'\375', (byte)'\376', (byte)'\377',
    };

    protected final void asciiApplyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        int[]code = new int[]{0};
        
        for (int i=0; i<AsciiLowerMap.length; i++) {
            code[0] = AsciiLowerMap[i][1];
            fun.apply(AsciiLowerMap[i][0], code, 1, arg);
            
            code[0] = AsciiLowerMap[i][0];
            fun.apply(AsciiLowerMap[i][1], code, 1, arg);
        }        
    }
    
    /** onigenc_ascii_apply_all_case_fold / used also by multibyte encodings
     */
    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        asciiApplyAllCaseFold(flag, fun, arg);
    }
    
    protected static final int AsciiLowerMap[][] = {
        {0x41, 0x61},
        {0x42, 0x62},
        {0x43, 0x63},
        {0x44, 0x64},
        {0x45, 0x65},
        {0x46, 0x66},
        {0x47, 0x67},
        {0x48, 0x68},
        {0x49, 0x69},
        {0x4a, 0x6a},
        {0x4b, 0x6b},
        {0x4c, 0x6c},
        {0x4d, 0x6d},
        {0x4e, 0x6e},
        {0x4f, 0x6f},
        {0x50, 0x70},
        {0x51, 0x71},
        {0x52, 0x72},
        {0x53, 0x73},
        {0x54, 0x74},
        {0x55, 0x75},
        {0x56, 0x76},
        {0x57, 0x77},
        {0x58, 0x78},
        {0x59, 0x79},
        {0x5a, 0x7a}
    };
    
    protected static final CaseFoldCodeItem[] EMPTY_FOLD_CODES = new CaseFoldCodeItem[]{};  
    protected final CaseFoldCodeItem[]asciiCaseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        int b = bytes[p] & 0xff;
        
        if (0x41 <= b && b <= 0x5a) {
            return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b + 0x20})};
        } else if (0x61 <= b && b <= 0x7a) {
            return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b - 0x20})};
        } else {
            return EMPTY_FOLD_CODES;
        }
    }
    
    /** onigenc_ascii_get_case_fold_codes_by_str / used also by multibyte encodings
     */
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        return asciiCaseFoldCodesByString(flag, bytes, p, end);
    }

    /** onigenc_minimum_property_name_to_ctype
     *  notably overridden by unicode encodings
     */
    @Override
    public int propertyNameToCType(byte[]bytes, int p, int end) {
        Integer ctype = PosixBracket.PBSTableUpper.get(bytes, p, end);
        if (ctype != null) return ctype;
        throw new ValueException(ErrorMessages.ERR_INVALID_CHAR_PROPERTY_NAME, new String(bytes, p, end - p));
    }
}
