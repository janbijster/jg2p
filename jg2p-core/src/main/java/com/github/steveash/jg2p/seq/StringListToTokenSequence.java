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

package com.github.steveash.jg2p.seq;

import com.google.common.base.Preconditions;

import com.github.steveash.jg2p.Word;

import java.io.Serializable;
import java.util.List;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

/**
 * Converts a Word in the data (and target if present) section in to a tokenSequence
 * @author Steve Ash
 */
public class StringListToTokenSequence extends Pipe implements Serializable {
  private static final long serialVersionUID = -774913899461041501L;

  private final boolean updateTarget;

  public StringListToTokenSequence(Alphabet dataDict, Alphabet targetDict) {
    this(dataDict, targetDict, true);
  }

  public StringListToTokenSequence(Alphabet dataDict, Alphabet targetDict,
                                   boolean updateTarget) {
    super(dataDict, targetDict);
    this.updateTarget = updateTarget;
  }

  @Override
  public Instance pipe(Instance inst) {
    List<String> source = (List<String>) inst.getData();

    inst.setData(makeTokenSeq(source));
    if (inst.getTarget() != null && updateTarget) {
      List<String> target = (List<String>) inst.getTarget();
      Preconditions.checkState(target.size() == source.size(), "target %s source %s", target, source);
      inst.setTarget(makeTokenSeq(target));
    }
    return inst;
  }

  private TokenSequence makeTokenSeq(List<String> vals) {
    TokenSequence ts = new TokenSequence(vals.size());
    for (String s : vals) {
      ts.add(s);
    }
    return ts;
  }
}
