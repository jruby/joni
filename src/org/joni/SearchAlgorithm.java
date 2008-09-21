package org.joni;

import org.jcodings.Encoding;
import org.jcodings.IntHolder;

public abstract class SearchAlgorithm {

    public abstract String getName();
    public abstract int search(Regex regex, byte[]text, int textP, int textEnd, int textRange);
    public abstract int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_);
    

    public static final SearchAlgorithm NONE = new SearchAlgorithm() {

        public final String getName() {
            return "NONE";
        }
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            return textP;
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            return textP;
        }
        
    };
    
    public static final SearchAlgorithm SLOW = new SearchAlgorithm() {
        
        public final String getName() {
            return "EXACT";
        }
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            
            int end = textEnd;
            end -= targetEnd - targetP - 1;
            
            if (end > textRange) end = textRange;
            
            int s = textP;
            
            while (s < end) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    
                    if (t == targetEnd) return s;
                }
                s += enc.length(text, s, textEnd);
            }
            
            return -1;            
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;            
            
            int s = textEnd;
            s -= targetEnd - targetP;
            
            if (s > textStart) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            
            while (s >= textP) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;            
        }
    };

    public static final SearchAlgorithm SLOW_SB = new SearchAlgorithm() {
        
        public final String getName() {
            return "EXACT_SB";
        }
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            int end = textEnd;
            end -= targetEnd - targetP - 1;
            
            if (end > textRange) end = textRange;
            
            int s = textP;
            
            while (s < end) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    
                    if (t == targetEnd) return s;
                }
                s++;
            }
            
            return -1;            
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;            
            
            int s = textEnd;
            s -= targetEnd - targetP;
            
            if (s > textStart) s = textStart;
            
            while (s >= textP) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                //s = s <= adjustText ? -1 : s - 1;
                s--;
            }
            return -1;            
        }
    };
    
    
    public static final class SLOW_IC extends SearchAlgorithm {
        private final byte[]buf = new byte[Config.ENC_MBC_CASE_FOLD_MAXLEN];
        private final IntHolder holder = new IntHolder();
        private final int caseFoldFlag;
        private final Encoding enc;

        public SLOW_IC(Regex regex) {
            this.caseFoldFlag = regex.caseFoldFlag;
            this.enc = regex.enc;
        }
        
        public final String getName() {
            return "EXACT_IC";
        }        
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            int end = textEnd;
            end -= targetEnd - targetP - 1;
            
            if (end > textRange) end = textRange;
            int s = textP;
            
            while (s < end) {
                if (lowerCaseMatch(target, targetP, targetEnd, text, s, textEnd)) return s;
                s += enc.length(text, s, textEnd);
            }
            return -1;
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            int s = textEnd;
            s -= targetEnd - targetP;
            
            if (s > textStart) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            
            while (s >= textP) {
                if (lowerCaseMatch(target, targetP, targetEnd, text, s, textEnd)) return s;
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;            
        }
        
        private boolean lowerCaseMatch(byte[]t, int tP, int tEnd,
                                       byte[]bytes, int p, int end) {

            holder.value = p;
            while (tP < tEnd) {
                int lowlen = enc.mbcCaseFold(caseFoldFlag, bytes, holder, end, buf);
                if (lowlen == 1) {
                    if (t[tP++] != buf[0]) return false;
                } else {
                    int q = 0;
                    while (lowlen > 0) {
                        if (t[tP++] != buf[q++]) return false; 
                        lowlen--;
                    }
                }
            }
            return true;
        }
    };

    public static final SearchAlgorithm SLOW_IC_SB = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_IC_SB";
        }        
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {            
            final byte[]toLowerTable = regex.enc.toLowerCaseTable();
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            int end = textEnd;
            end -= targetEnd - targetP - 1;
            
            if (end > textRange) end = textRange;
            int s = textP;
            
            while (s < end) {
                if (target[targetP] == toLowerTable[text[s] & 0xff]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != toLowerTable[text[p++] & 0xff]) break;
                        t++;
                    }
                    
                    if (t == targetEnd) return s;
                }
                s++;                
            }
            return -1;
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            final byte[]toLowerTable = regex.enc.toLowerCaseTable();
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            int s = textEnd;
            s -= targetEnd - targetP;
            
            if (s > textStart) s = textStart;
            
            while (s >= textP) {
                if (target[targetP] == toLowerTable[text[s] & 0xff]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != toLowerTable[text[p++] & 0xff]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                //s = s <= adjustText ? -1 : s - 1;
                s--;
            }
            return -1;            
        }

    };    
    
    public static final SearchAlgorithm BM = new SearchAlgorithm() {
        
        public final String getName() {
            return "EXACT_BM";
        }        
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;            
            
            int end = textRange + (targetEnd - targetP) - 1;
            if (end > textEnd) end = textEnd;
            
            int tail = targetEnd - 1;
            int s = textP + (targetEnd - targetP) - 1;
            
            if (regex.intMap == null) {
                while (s < end) {
                    int p = s;
                    int t = tail;
                    while (t >= targetP && text[p] == target[t]) {
                        p--; t--;
                    }
                    if (t < targetP) return p + 1;
                    s += regex.map[text[s] & 0xff];
                }
            } else { /* see int_map[] */
                while (s < end) {
                    int p = s;
                    int t = tail;
                    while (t >= targetP && text[p] == target[t]) {
                        p--; t--;
                    }
                    if (t < targetP) return p + 1;
                    s += regex.intMap[text[s] & 0xff];
                }
            }
            return -1;            
        }
        
        private static final int BM_BACKWARD_SEARCH_LENGTH_THRESHOLD = 100;
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;
            
            if (regex.intMapBackward == null) {
                if (s_ - range_ < BM_BACKWARD_SEARCH_LENGTH_THRESHOLD) {
                    // goto exact_method;
                    return SLOW.searchBackward(regex, text, textP, adjustText, textEnd, textStart, s_, range_);
                }
                setBmBackwardSkip(regex, target, targetP, targetEnd);                    
            }
            
            int s = textEnd - (targetEnd - targetP);
            
            if (textStart < s) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            
            while (s >= textP) {
                int p = s;
                int t = targetP;
                while (t < targetEnd && text[p] == target[t]) {
                    p++; t++;
                }
                if (t == targetEnd) return s;
                
                s -= regex.intMapBackward[text[s] & 0xff];
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            return -1;            
        }
        
        
        private void setBmBackwardSkip(Regex regex, byte[]bytes, int p, int end) {
            int[] skip;
            if (regex.intMapBackward == null) {
                skip = new int[Config.CHAR_TABLE_SIZE];
                regex.intMapBackward = skip;
            } else {
                skip = regex.intMapBackward;
            }
            
            int len = end - p;
            
            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) skip[i] = len;
            for (int i=len-1; i>0; i--) skip[bytes[i] & 0xff] = i;
        }        
    };
    
    public static final SearchAlgorithm BM_NOT_REV = new SearchAlgorithm() {
        
        public final String getName() {
            return "EXACT_BM_NOT_REV";
        }        
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;              
            
            int tail = targetEnd - 1;
            int tlen1 = tail - targetP;
            int end = textRange;
            
            if (Config.DEBUG_SEARCH) {
                Config.log.println("bm_search_notrev: "+
                                    "text: " + textP +
                                    ", text_end: " + textEnd +
                                    ", text_range: " + textRange);
            }
            
            if (end + tlen1 > textEnd) end = textEnd - tlen1;
            
            int s = textP;
            
            if (regex.intMap == null) {
                while (s < end) {
                    int p, se;
                    p = se = s + tlen1;
                    int t = tail;
                    while (t >= targetP && text[p] == target[t]) {
                        p--; t--;
                    }
                    
                    if (t < targetP) return s;
                    
                    int skip = regex.map[text[se] & 0xff];
                    t = s;
                    do {
                        s += enc.length(text, s, textEnd);
                    } while ((s - t) < skip && s < end);
                }
            } else {
                while (s < end) {
                    int p, se;
                    p = se = s + tlen1;
                    int t = tail;
                    while (t >= targetP && text[p] == target[t]) {
                        p--; t--;
                    }
                    
                    if (t < targetP) return s;
                    
                    int skip = regex.intMap[text[se] & 0xff];
                    t = s;
                    do {
                        s += enc.length(text, s, textEnd);
                    } while ((s - t) < skip && s < end);
                    
                }
            }
            return -1;            
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            return BM.searchBackward(regex, text, textP, adjustText, textEnd, textStart, s_, range_);
        }
    };
    
    
    public static final SearchAlgorithm MAP = new SearchAlgorithm() {

        public final String getName() {
            return "MAP";
        }        
        
        // TODO: check 1.9 inconsistent calls to map_search
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            Encoding enc = regex.enc;
            byte[]map = regex.map;
            int s = textP;

            while (s < textRange) {
                if (map[text[s] & 0xff] != 0) return s;
                s += enc.length(text, s, textEnd);
            }
            return -1;
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Encoding enc = regex.enc;
            byte[]map = regex.map;
            int s = textStart;

            if (s >= textEnd) s = textEnd - 1; // multibyte safe ? 
            while (s >= textP) {
                if (map[text[s] & 0xff] != 0) return s;
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;
        }
    };

    public static final SearchAlgorithm MAP_SB = new SearchAlgorithm() {

        public final String getName() {
            return "MAP_SB";
        }        
        
        public final int search(Regex regex, byte[]text, int textP, int textEnd, int textRange) {
            byte[]map = regex.map;
            int s = textP;
            
            while (s < textRange) {
                if (map[text[s] & 0xff] != 0) return s;
                s++;
            }
            return -1;
        }
        
        public final int searchBackward(Regex regex, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            byte[]map = regex.map;
            int s = textStart;

            if (s >= textEnd) s = textEnd - 1; 
            while (s >= textP) {
                if (map[text[s] & 0xff] != 0) return s;
                s--;
            }
            return -1;
        }
    };   
    
}
