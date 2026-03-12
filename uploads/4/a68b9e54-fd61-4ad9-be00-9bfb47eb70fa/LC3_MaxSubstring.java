package com.codetop.hot100;

import java.util.HashSet;
import java.util.Set;

public class LC3_MaxSubstring {

    public int lengthOfLongestSubstring(String s) {
        Set<Character> set = new HashSet<Character>();
        int stringLength = s.length();
        int maxLength = 0, end = -1;
        for (int i = 0; i < stringLength; i++) {
            if (i != 0) {
                set.remove(s.charAt(i - 1));
            }
            while (end + 1 < stringLength && !set.contains(s.charAt(end + 1))) {
                set.add(s.charAt(end + 1));
                end++;
            }
            if (set.size() > maxLength) {
                maxLength = set.size();
            }
        }


        return maxLength;
    }

    public static void main(String[] args) {
        String s = "ab";
        LC3_MaxSubstring solution = new LC3_MaxSubstring();
        System.out.println(solution.lengthOfLongestSubstring(s));
    }
}
