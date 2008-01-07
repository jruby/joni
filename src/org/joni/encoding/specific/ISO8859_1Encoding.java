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

import org.joni.ApplyAllCaseFoldFunction;
import org.joni.CaseFoldCodeItem;
import org.joni.encoding.ISOEncoding;

public final class ISO8859_1Encoding extends ISOEncoding {

    protected ISO8859_1Encoding() {
        super(ISO8859_1CtypeTable, ISO8859_1ToLowerCaseTable, ISO8859_1CaseFoldMap);
    }
    
    @Override
    public String toString() {
        return "ISO-8859-1";
    }
    
    @Override
    public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
        applyAllCaseFoldWithMap(CaseFoldMap.length, CaseFoldMap, true, flag, fun, arg);      
    }

    /** get_case_fold_codes_by_str
     */
    @Override
    public CaseFoldCodeItem[]caseFoldCodesByString(int flag, byte[]bytes, int p, int end) {
        int b = bytes[p] & 0xff;
        
        if (0x41 <= b && b <= 0x5a) {
            CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 1, new int[]{b + 0x20});
            
            if (b == 0x53 && end > p + 1 &&
               (bytes[p+1] == (byte)0x53 || bytes[p+1] == (byte)0x73)) { /* ss */
                CaseFoldCodeItem item1 = new CaseFoldCodeItem(2, 1, new int[]{0xdf});

                return new CaseFoldCodeItem[]{item0, item1};  
            } else {
                return new CaseFoldCodeItem[]{item0};
            }
        } else if (0x61 <= b && b <= 0x7a) {
            CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 1, new int[]{b - 0x20});
            
            if (b == 0x73 && end > p + 1 &&
               (bytes[p+1] == (byte)0x73 || bytes[p+1] == (byte)0x53)) { /* ss */
                CaseFoldCodeItem item1 = new CaseFoldCodeItem(2, 1, new int[]{0xdf});
                return new CaseFoldCodeItem[]{item0, item1};
            } else {
                return new CaseFoldCodeItem[]{item0};
            }
            
        } else if (0xc0 <= b && b <= 0xcf) {
            return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b + 0x20})};
        } else if (0xd0 <= b && b <= 0xdf) {
            if (b == 0xdf) {
                CaseFoldCodeItem item0 = new CaseFoldCodeItem(1, 2, new int[]{'s', 's'});
                CaseFoldCodeItem item1 = new CaseFoldCodeItem(1, 2, new int[]{'S', 'S'});
                CaseFoldCodeItem item2 = new CaseFoldCodeItem(1, 2, new int[]{'s', 'S'});
                CaseFoldCodeItem item3 = new CaseFoldCodeItem(1, 2, new int[]{'S', 's'});
                
                return new CaseFoldCodeItem[]{item0, item1, item2, item3};               
            } else if (b != 0xd7) {
                return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b + 0x20})};
            }
        } else if (0xe0 <= b && b <= 0xef) {
            return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b - 0x20})};
        } else if (0xf0 <= b && b <= 0xfe) {
            if (b != 0xf7) {
                return new CaseFoldCodeItem[]{new CaseFoldCodeItem(1, 1, new int[]{b - 0x20})};
            }
        }       
        return EMPTY_FOLD_CODES;
    }
    
    static final short ISO8859_1CtypeTable[] = {
        0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008, 0x4008,
        0x4008, 0x420c, 0x4209, 0x4208, 0x4208, 0x4208, 0x4008, 0x4008,
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
        0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
        0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
        0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
        0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008, 0x0008,
        0x0284, 0x01a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0, 0x00a0,
        0x00a0, 0x00a0, 0x30e2, 0x01a0, 0x00a0, 0x01a0, 0x00a0, 0x00a0,
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
    
    static final byte ISO8859_1ToLowerCaseTable[] = new byte[]{
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
        (byte)'\340', (byte)'\341', (byte)'\342', (byte)'\343', (byte)'\344', (byte)'\345', (byte)'\346', (byte)'\347',
        (byte)'\350', (byte)'\351', (byte)'\352', (byte)'\353', (byte)'\354', (byte)'\355', (byte)'\356', (byte)'\357',
        (byte)'\360', (byte)'\361', (byte)'\362', (byte)'\363', (byte)'\364', (byte)'\365', (byte)'\366', (byte)'\327',
        (byte)'\370', (byte)'\371', (byte)'\372', (byte)'\373', (byte)'\374', (byte)'\375', (byte)'\376', (byte)'\337',
        (byte)'\340', (byte)'\341', (byte)'\342', (byte)'\343', (byte)'\344', (byte)'\345', (byte)'\346', (byte)'\347',
        (byte)'\350', (byte)'\351', (byte)'\352', (byte)'\353', (byte)'\354', (byte)'\355', (byte)'\356', (byte)'\357',
        (byte)'\360', (byte)'\361', (byte)'\362', (byte)'\363', (byte)'\364', (byte)'\365', (byte)'\366', (byte)'\367',
        (byte)'\370', (byte)'\371', (byte)'\372', (byte)'\373', (byte)'\374', (byte)'\375', (byte)'\376', (byte)'\377'
    }; 

    static final byte ISO8859_1ToUpperCaseTable[] = new byte[]{
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
        (byte)'\300', (byte)'\301', (byte)'\302', (byte)'\303', (byte)'\304', (byte)'\305', (byte)'\306', (byte)'\307',
        (byte)'\310', (byte)'\311', (byte)'\312', (byte)'\313', (byte)'\314', (byte)'\315', (byte)'\316', (byte)'\317',
        (byte)'\320', (byte)'\321', (byte)'\322', (byte)'\323', (byte)'\324', (byte)'\325', (byte)'\326', (byte)'\367',
        (byte)'\330', (byte)'\331', (byte)'\332', (byte)'\333', (byte)'\334', (byte)'\335', (byte)'\336', (byte)'\377',
    };
    
    static final int ISO8859_1CaseFoldMap[][] = {
        { 0xc0, 0xe0 },
        { 0xc1, 0xe1 },
        { 0xc2, 0xe2 },
        { 0xc3, 0xe3 },
        { 0xc4, 0xe4 },
        { 0xc5, 0xe5 },
        { 0xc6, 0xe6 },
        { 0xc7, 0xe7 },
        { 0xc8, 0xe8 },
        { 0xc9, 0xe9 },
        { 0xca, 0xea },
        { 0xcb, 0xeb },
        { 0xcc, 0xec },
        { 0xcd, 0xed },
        { 0xce, 0xee },
        { 0xcf, 0xef },

        { 0xd0, 0xf0 },
        { 0xd1, 0xf1 },
        { 0xd2, 0xf2 },
        { 0xd3, 0xf3 },
        { 0xd4, 0xf4 },
        { 0xd5, 0xf5 },
        { 0xd6, 0xf6 },
        { 0xd8, 0xf8 },
        { 0xd9, 0xf9 },
        { 0xda, 0xfa },
        { 0xdb, 0xfb },
        { 0xdc, 0xfc },
        { 0xdd, 0xfd },
        { 0xde, 0xfe }
    };  

    public static final ISO8859_1Encoding INSTANCE = new ISO8859_1Encoding();
}
