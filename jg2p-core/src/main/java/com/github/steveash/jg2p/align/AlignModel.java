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

import com.github.steveash.jg2p.Word;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * Uses the EM-based graphone model + viterbi inference
 * @author Steve Ash
 */
public class AlignModel implements Externalizable, Aligner {

  private /*final*/ GramOptions opts;
  private /*final*/ ProbTable t;
  private /*final*/ AlignerViterbi viterbi;
  private /*final*/ AlignerInferencer inferencer;

  public AlignModel(GramOptions opts, ProbTable t) {
    this.opts = opts;
    this.t = t;
    viterbi = new AlignerViterbi(opts, t);
    inferencer = new AlignerInferencer(opts, t);
  }

  // for serialization
  public AlignModel() {
  }

  public List<Alignment> align(Word x, Word y, int nBest) {
    return viterbi.align(x, y, nBest);
  }

  @Override
  public List<Alignment> inferAlignments(Word x, int nBest) {
    return inferencer.bestGraphemes(x, nBest);
  }

  public ProbTable getTransitions() {
    return t;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(opts);
    out.writeObject(t);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.opts = (GramOptions) in.readObject();
    this.t = (ProbTable) in.readObject();
    this.viterbi = new AlignerViterbi(opts, t);
    this.inferencer = new AlignerInferencer(opts, t);
  }
}
