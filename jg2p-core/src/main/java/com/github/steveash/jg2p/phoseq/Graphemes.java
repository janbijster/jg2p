/*
 * Copyright 2015 Steve Ash
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.steveash.jg2p.phoseq;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

import com.github.steveash.jg2p.Word;

import javax.annotation.Nullable;

import static com.google.common.base.CharMatcher.anyOf;
import static com.google.common.base.CharMatcher.inRange;

/**
 * @author Steve Ash
 */
public class Graphemes {

  public static final CharMatcher vowels = anyOf("AEIOUYaeiouy").precomputed();
  public static final CharMatcher
      consonants =
      (inRange('A', 'Z').or(inRange('a', 'z'))).and(vowels.negate()).precomputed();
  private static final CharMatcher other = CharMatcher.ANY.and(vowels.or(consonants).negate()).precomputed();

  public static boolean isVowelOrConsonant(String graph) {
    Preconditions.checkArgument(graph.length() == 1);
    return !other.matches(graph.toUpperCase().charAt(0));
  }

  public static boolean isVowel(String graph) {
    Preconditions.checkArgument(graph.length() == 1);
    return vowels.matches(graph.toUpperCase().charAt(0));
  }

  public static boolean isConsonant(String graph) {
    Preconditions.checkArgument(graph.length() == 1);
    return consonants.matches(graph.toUpperCase().charAt(0));
  }

  public static boolean isAllVowelsOrConsonants(Word word) {
    for (int i = 0; i < word.unigramCount(); i++) {
      String gram = word.gramAt(i);
      if (other.matchesAllOf(gram)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isAllVowels(Word word) {
    for (int i = 0; i < word.unigramCount(); i++) {
      String gram = word.gramAt(i);
      if (!vowels.matchesAllOf(gram)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isAllConsonants(Word word) {
    for (int i = 0; i < word.unigramCount(); i++) {
      String gram = word.gramAt(i);
      if (!consonants.matchesAllOf(gram)) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  public static Word trimTrailingAposS(Word input) {
    int size = input.unigramCount();
    if (size >= 3) {
      if (input.gramAt(size - 2).equalsIgnoreCase("'")) {
        if (input.gramAt(size - 1).equalsIgnoreCase("S")) {
          return Word.fromGrams(input.getValue().subList(0, size - 2));
        }
      }
    }
    return null;
  }

  public static Word xformForEval(Word input) {
    input.throwIfNotUnigram();
//    int originalSize = input.unigramCount();
//    if (originalSize <= 0) return input;
//
//    String lastChar = input.gramAt(originalSize - 1);
//    if (lastChar.equalsIgnoreCase("'")) {
//      return Word.fromGrams(input.getValue().subList(0, originalSize - 1));
//    }
    return input;
  }
}
