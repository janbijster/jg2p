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

package com.github.steveash.jg2p.rerank

import com.github.steveash.jg2p.PhoneticEncoder
import com.github.steveash.jg2p.util.CsvFactory
import com.google.common.collect.ImmutableList

/**
 * @author Steve Ash
 */
class RerankExampleTest extends GroovyTestCase {

  void testCsvInOut() {
    def abc = ImmutableList.of("A", "B", "C")
    def encA = PhoneticEncoder.Encoding.createEncoding(abc, abc, abc, 2.123, 3.456, 10.0, 3)

    def ex = new RerankExample()
    ex.dupCount = 1
    ex.encoding = encA
    ex.languageModelScore = 0.001
    ex.uniqueMatchingMode = true
    ex.wordGraphs = abc
    ex.sequence = 1
    ex.relevant = true

    def factory = CsvFactory.make();
    def serializer = factory.createSerializer()
    def deserializer = factory.createDeserializer()

    def sw = new StringWriter()
    serializer.open(sw)
    serializer.write(ex)
    serializer.write(ex)
    serializer.write(ex)
    serializer.close(true)

    def result = sw.toString()
    println "Got:\n" + result

    def sr = new StringReader(result)
    deserializer.open(sr)
    while (deserializer.hasNext()) {
      def o = deserializer.next()
      println "Re-read: " + o
    }
    deserializer.close(true)
    println "Done!"
  }
}
