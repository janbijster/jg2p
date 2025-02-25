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

import com.github.steveash.jg2p.PhoneticEncoder
import com.github.steveash.jg2p.PhoneticEncoder.Encoding
import com.github.steveash.jg2p.align.InputReader
import com.github.steveash.jg2p.align.InputRecord
import com.github.steveash.jg2p.util.GroovyLogger
import com.github.steveash.jg2p.util.Histogram
import com.github.steveash.jg2p.util.ListEditDistance
import com.github.steveash.jg2p.util.ReadWrite
import com.google.common.base.Stopwatch
import com.google.common.collect.HashMultiset
import groovyx.gpars.GParsConfig
import groovyx.gpars.GParsPool

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * Used to play with the failing examples to try and figure out some areas for improvement
 * @author Steve Ash
 */
out = new GroovyLogger()
def oneEditCounts = HashMultiset.create()

Closure examplePrinter = { String name, Encoding neww, InputRecord input ->
  def edits = ListEditDistance.editDistance(neww.phones, input.yWord.value, 10)
  def more = ""
  if (edits == 1 && neww.phones.size() == input.yWord.unigramCount()) {
    for (int i = 0; i < neww.phones.size(); i++) {
      def newp = neww.phones.get(i)
      def yp = input.yWord.value.get(i)

      if (!newp.equals(yp)) {
        more = " : " + newp + " -> " + yp
        oneEditCounts.add(newp + " -> " + yp)
        break;
      }
    }
  }
  println "  New " + neww
  println "  Exp " + input.yWord.asSpaceString
  println "      " + name + ", edits " + edits + more
  println "-------------------------------------------"
}

//def file = "cmudict.5kA.txt"
//def file = "cmudict.5kB.txt"
def file = "g014b2b.train"
//def file = "g014b2b.test"
//def inps = InputReader.makeDefaultFormatReader().readFromClasspath(file)
def inps = InputReader.makePSaurusReader().readFromClasspath(file).take(100)
//Collections.shuffle(inps, new Random(0xCAFEBABE))
//inps = inps.subList(0, (int)(inps.size() / 4));

//def enc = ReadWrite.readFromClasspath(PhoneticEncoder.class, "cmu_all_jt_2eps_winB.model.dat")
def enc = ReadWrite.readFromFile(PhoneticEncoder.class, new File("../resources/psaur_22_xEps_ww_f3.dat"))
//def alignTag = ReadWrite.readFromClasspath(AlignTagModel, "aligntag.dat")
//def enc2 = enc.withAligner(alignTag)

class Entry {

  AtomicInteger count = new AtomicInteger(0)
  List examples = Collections.synchronizedList([])

  void addExample(Encoding enc, InputRecord input) {
    int thisCount = count.incrementAndGet();


    def size = examples.size()
    if (size < 333) {
      examples << [enc, input]
    } else {
      def next = ThreadLocalRandom.current().nextInt(0, thisCount)
      if (next < size) {
        examples[next] = [enc, input]
      }
    }

  }

}

class Counts {

  AtomicInteger wins = new AtomicInteger(0)
  AtomicInteger total = new AtomicInteger(0)
  // how many wrong cases where the right answer was in rank 2 and a histo of the prob dist between rank1 and rank2
  Histogram good1rankDelta = new Histogram(0.0, 1.0, 50)
  AtomicInteger good2rank = new AtomicInteger(0)
  Histogram good2rankDelta = new Histogram(0.0, 1.0, 50)
  AtomicInteger good3rank = new AtomicInteger(0)
  Histogram good3rankDelta = new Histogram(0.0, 1.0, 50)
  AtomicInteger good4rank = new AtomicInteger(0)
  Histogram good4rankDelta = new Histogram(0.0, 1.0, 50)

  def winExamples = Collections.synchronizedList([])
  def lostRightAlign = new Entry()
  def lostWrongAlign = new Entry()
  def lostWrongAlignGgtP = new Entry()
  def lostWrongAlignGltP = new Entry()
  def lostWrongAlignGeqP = new Entry()

  // this closure should take 3 args: fieldName, Encoding, Input
  void eachExample(Closure c) {
    this.getProperties().each { k, v ->
      if (v instanceof Entry) {
        v.examples.each { c(k, it[0], it[1]) }
      }
    }
  }

  boolean isDone() {
    return false
    //winExamples.size() >= 666 &&
    //        lostRightAlign.examples.size() >= 333 &&
    //        lostWrongAlignGgtP.examples.size() >= 333 &&
    //        lostWrongAlignGeqP.examples.size() >= 333 &&
    //        lostWrongAlignGltP.examples.size() >= 333
  }

  @Override
  String toString() {
    StringBuilder s = new StringBuilder()
    s.append("wins = $wins\ntotal = $total")
    s.append("\nGood2Rank=${good2rank.get()}")
    s.append("\nGood3Rank=${good3rank.get()}")
    s.append("\nGood4Rank=${good4rank.get()}")
    s.append("\nGood1Histo=" + good1rankDelta.nonEmptyBinsAsString())
    s.append("\nGood2Histo=" + good2rankDelta.nonEmptyBinsAsString())
    s.append("\nGood3Histo=" + good3rankDelta.nonEmptyBinsAsString())
    s.append("\nGood4Histo=" + good4rankDelta.nonEmptyBinsAsString())
    this.getProperties().each { k, v ->
      if (v instanceof Entry) {
        s.append("\n").append(k).append(" = ").append(v.count)
      }
    }
    return s.toString()
  }
}

def c = new Counts()
println "Running with bestTaggings = " + enc.bestTaggings + " and bestAligns = " + enc.bestAlignments
Stopwatch watch = Stopwatch.createStarted()
GParsPool.withPool {
  inps.everyParallel { InputRecord input ->
    if (c.isDone()) {
      return false
    }

    List<PhoneticEncoder.Encoding> ans = enc.encode(input.xWord);
    def newTotal = c.total.incrementAndGet()

    if (newTotal % 5000 == 0) {
      println "Completed " + newTotal + " of " + inps.size()
    }

    def exp = input.yWord.value
    def neww = ans.get(0)

    if (neww.phones == exp) {
      int newWins = c.wins.incrementAndGet()
      c.good1rankDelta.add(neww.tagProbability())
      if (newWins < 333) {
        c.winExamples << [neww, input]
      }
      return true;
    }

    if (ans.size() >= 2 && ans.get(1).phones == exp) {
      c.good2rank.incrementAndGet()
      double delta = neww.tagProbability() - ans.get(1).tagProbability()
      synchronized (c.good2rankDelta) {
        c.good2rankDelta.add(delta);
      }
    } else if (ans.size() >= 3 && ans.get(2).phones == exp) {
      c.good3rank.incrementAndGet()
      double delta = neww.tagProbability() - ans.get(2).tagProbability()
      synchronized (c.good3rankDelta) {
        c.good3rankDelta.add(delta);
      }
    } else if (ans.size() >= 4 && ans.get(3).phones == exp) {
      c.good4rank.incrementAndGet()
      double delta = neww.tagProbability() - ans.get(3).tagProbability()
      synchronized (c.good4rankDelta) {
        c.good4rankDelta.add(delta);
      }
    }

    def expc = exp.size()
    def algc = neww.alignment.size()
    def gc = input.xWord.unigramCount()

    if (expc == algc) {
      c.lostRightAlign.addExample(neww, input)
    } else {
      c.lostWrongAlign.addExample(neww, input)
      if (gc > expc) {
        c.lostWrongAlignGgtP.addExample(neww, input)
      } else if (gc < expc) {
        c.lostWrongAlignGltP.addExample(neww, input)
      } else {
        c.lostWrongAlignGeqP.addExample(neww, input)
      }

    }
    return true;
  }
}
GParsConfig.shutdown()
watch.stop()

c.eachExample examplePrinter
//println "Counts of errors with 1 phoneme error"
//oneEditCounts.entrySet().each { println it.element + " = " + it.count}
println "Done! Counts=\n" + c.toString()
println "Eval took " + watch

/*
def ex = []
ex.addAll(c.winExamples.subList(0, 333))
ex.addAll(c.lostRightAlign.examples.subList(0, 333))
ex.addAll(c.lostWrongAlignGeqP.examples.subList(0, 333))
ex.addAll(c.lostWrongAlignGgtP.examples.subList(0, 333))
ex.addAll(c.lostWrongAlignGltP.examples.subList(0, 333))

Collections.shuffle(ex)
new File("cmubad.2kA.txt").withPrintWriter { pw ->
  ex.each {
    InputRecord inp = it[1]
    pw.println(inp.left.asSpaceString + "\t" + inp.right.asSpaceString)
  }
}
*/
println "done!"