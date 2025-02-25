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

import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Options for the training procedure; also defines the training command line arguments
 *
 * @author Steve Ash
 */
public class TrainOptions implements Cloneable {

  public enum InputFormat {TAB, CMU}

  @Option(name = "--minX")
  public int minXGram = 1;  // these have to be 1 right now

  @Option(name = "--maxX")
  public int maxXGram = 2;

  @Option(name = "--minY")
  public int minYGram = 1;  // these have to be 1 right now

  @Option(name = "--maxY")
  public int maxYGram = 1;

  @Option(name = "--includeXEps")
  public boolean includeXEpsilons = true;

  @Option(name = "--includeEpsY")
  public boolean includeEpsilonYs = false;

  @Option(name = "--onlyOneGrams")
  public boolean onlyOneGrams = true; // only 1-1, 2-1, 1-2, etc. no 2-2 or 3-2

  @Option(name = "--useCityBlockPenalty")
  public boolean useCityBlockPenalty = false; // enables the penalty described in Kubo 2014

  /**
   * training aligner options
   **/
  @Option(name = "--trainingAlignerMaximizer")
  public transient Maximizer trainingAlignerMaximizer = Maximizer.JOINT;

  @Option(name = "--trainingAlignerMaxIteartions")
  public int trainingAlignerMaxIterations = 100;

  @Option(name = "--trainingConvergence")
  public double probDeltaConvergenceThreshold = 1.0e-5;

  @Option(name = "--allowedAligns")
  public File alignAllowedFile;

  @Option(name = "--semiSupervisedFactor")
  public double semiSupervisedFactor = 0.6;

  @Option(name = "--useWindowWalker")
  public boolean useWindowWalker = true;

  @Option(name = "--windowWalkerPadding")
  public int windowPadding = 0;

  /**
   * Syllable aligner options; syllable aligner is used to train the alignment model, then a
   * special syll-aligner-tagger is trained for runtime alignemtn + tagging of syllable structure
   */

  @Option(name = "--syllableTagger")
  public boolean useSyllableTagger = false;

  /**
   * pronouncer options
   **/
  @Option(name = "--maxCrfTrainingIterations")
  public int maxPronouncerTrainingIterations = 175;

  @Option(name = "--trimFeaturesUnderPercentile")
  public int trimFeaturesUnderPercentile = 0;

  @Option(name = "--trimFeaturesByGradient")
  public double trimFeaturesByGradientGain = 0;

  /**
   * Graphone language model options
   */
  @Option(name = "--graphoneNGram")
  public int graphoneLanguageModelOrder = 8;

  @Option(name = "--graphoneNGramForTraining")
  public int graphoneLanguageModelOrderForTraining = 8;

  @Option(name = "--modelLangFromGraphones")
  public boolean graphoneLangModel = true; // if true then model is of graphones, if false then its of phonemes only

  /**
   * Reranker training options
   */
  @Option(name = "--useInputRerankExampleCsv")
  public String useInputRerankExampleCsv = null;

  @Option(name = "--writeOutputRerankExampleCsv")
  public String writeOutputRerankExampleCsv = null;

  /**
   * where to get the serialized model files to use or at least start from
   **/

  @Option(name = "--initCrfFrom")
  public String initCrfFromModelFile = null;

  @Option(name = "--initSyllTagFrom")
  public String initSyllTagFromFile = null;

  @Option(name = "--initTrainingAlignerFrom")
  public String initTrainingAlignerFromFile = null;

  @Option(name = "--initTestingAlignerFrom")
  public String initTestingAlignerFromFile = null;

  @Option(name = "--initRerankerFrom")
  public String initRerankerFromFile = null;

  @Option(name = "--initGraphoneFrom")
  public String initGraphoneModelFromFile = null;

  // if you want to load up the phone syllable tagger to use in the reranking model then
  // you need to set this to the path to the
  @Option(name = "--usePhoneSyllModel")
  public String initPhoneSyllModelFromFile = null;

  /**
   * recipe flags that indicate which part of the training that is being completed
   **/
  @Option(name = "--trainTrainingAligner")
  public boolean trainTrainingAligner = true;
  @Option(name = "--trainTestingAligner")
  public boolean trainTestingAligner = true;
  @Option(name = "--trainSyllTag")
  public boolean trainSyllTag = true;
  @Option(name = "--trainPronouncer")
  public boolean trainPronouncer = true;
  @Option(name = "--trainGraphone")
  public boolean trainGraphoneModel = true;
  @Option(name = "--trainReranker")
  public boolean trainReranker = true;

  /**
   * misc training options
   **/

  @Option(name = "--topKAlignCandidates")
  public int topKAlignCandidates = 1; // the number of aligned training examples, to use to train the aligner and pronouncer

  @Option(name = "--minAlignScore")
  public int minAlignScore = Integer.MIN_VALUE; // the min aligner score to consider for training

  @Option(name = "--infile", required = true)
  public File trainingFile; // the input csv/tsv/etc

  @Option(name = "--outfile")
  public File outputFile; // the output model file

  @Option(name = "--format")
  public InputFormat format = InputFormat.TAB;

  public void trainAll() {
    this.trainTrainingAligner = true;
    this.trainTestingAligner = true;
    this.trainPronouncer = true;
    this.trainGraphoneModel = true;
    this.trainReranker = true;
  }

  public void afterParametersSet() {
    if (outputFile == null) {
      this.outputFile = new File(trainingFile.getAbsolutePath() + ".model.dat");
    }
  }

  public GramOptions makeGramOptions() {
    GramOptions gopts = new GramOptions(minXGram, maxXGram, minYGram, maxYGram, includeXEpsilons, includeEpsilonYs,
                                        onlyOneGrams, windowPadding);
    if (useCityBlockPenalty) gopts.setCityBlockPenalty(true);
    return gopts;
  }

  public InputReader makeReader() {
    if (format == InputFormat.CMU) {
      return InputReader.makeCmuReader();
    }

    return InputReader.makeDefaultFormatReader();
  }

  @Override
  public TrainOptions clone() {
    try {
      return (TrainOptions) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException();
    }
  }

  @Override
  public String toString() {
    return "TrainOptions{" +
           "\n\tminXGram=" + minXGram +
           ",\n\tmaxXGram=" + maxXGram +
           ",\n\tminYGram=" + minYGram +
           ",\n\tmaxYGram=" + maxYGram +
           ",\n\tincludeXEpsilons=" + includeXEpsilons +
           ",\n\tincludeEpsilonYs=" + includeEpsilonYs +
           ",\n\tonlyOneGrams=" + onlyOneGrams +
           ",\n\ttrainingAlignerMaximizer=" + trainingAlignerMaximizer +
           ",\n\ttrainingAlignerMaxIterations=" + trainingAlignerMaxIterations +
           ",\n\tprobDeltaConvergenceThreshold=" + probDeltaConvergenceThreshold +
           ",\n\tmaxPronouncerTrainingIterations=" + maxPronouncerTrainingIterations +
           ",\n\ttrimFeaturesUnderPercentile=" + trimFeaturesUnderPercentile +
           ",\n\tinitCrfFromModelFile='" + initCrfFromModelFile + '\'' +
           ",\n\tsemiSupervisedFactor=" + semiSupervisedFactor +
           ",\n\tuseWindowWalker=" + useWindowWalker +
           ",\n\twindowPadding=" + windowPadding +
           ",\n\ttopKAlignCandidates=" + topKAlignCandidates +
           ",\n\tminAlignScore=" + minAlignScore +
           ",\n\ttrainingFile=" + trainingFile +
           ",\n\toutputFile=" + outputFile +
           ",\n\tformat=" + format +
           '}';
  }
}
