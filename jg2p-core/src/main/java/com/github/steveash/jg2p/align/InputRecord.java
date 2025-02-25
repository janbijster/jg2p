/*
 * Copyright 2014 Steve Ash
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

package com.github.steveash.jg2p.align;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import com.github.steveash.jg2p.Word;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * One training exemplar for the aligner
 *
 * @author Steve Ash
 */
public class InputRecord extends Pair<Word, Word> {

  public final Word xWord;
  public final Word yWord;
  public final String memo;
  public final List<Integer> stresses;

  public InputRecord(Word xWord, Word yWord) {
    this(xWord, yWord, null);
  }

  public InputRecord(Word xWord, Word yWord, List<Integer> stresses) {
    this.xWord = xWord;
    this.yWord = yWord;
    this.memo = null;
    this.stresses = stresses;
  }

  public Pair<Word, Word> xyWordPair() {
    return Pair.of(xWord, yWord);
  }

  @Override
  public Word getLeft() {
    return xWord;
  }

  @Override
  public Word getRight() {
    return yWord;
  }

  @Override
  public Word setValue(Word value) {
    throw new IllegalStateException("Word pairs are immutable");
  }

  public static final Ordering<InputRecord> OrderByX = Ordering.natural().onResultOf(
      new Function<InputRecord, Word>() {
        @Override
        public Word apply(InputRecord input) {
          return input.getLeft();
        }
      });

  public static final Equivalence<InputRecord> EqualByX = new Equivalence<InputRecord>() {
    @Override
    protected boolean doEquivalent(InputRecord a, InputRecord b) {
      return a.getLeft().equals(b.getLeft());
    }

    @Override
    protected int doHash(InputRecord inputRecord) {
      return 0;
    }
  };

  @Override
  public String toString() {
    return "InputRecord{" +
           "xWord=" + xWord +
           ", yWord=" + yWord +
           ", stresses=" + stresses +
           '}';
  }
}
