/*
 * Copyright 2016 Steve Ash
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
import com.github.steveash.jg2p.PipelineModel
import com.github.steveash.jg2p.util.GroovyLogger
import com.github.steveash.jg2p.util.ReadWrite
import org.slf4j.LoggerFactory

def modelFile = "../resources/pipe_22_F9_1.dat"


def log = LoggerFactory.getLogger("psaurus")
out = new GroovyLogger(log)
out.println("Starting the test...")

def model = ReadWrite.readFromFile(PipelineModel, new File(modelFile))
ReadWrite.writeTo(model.trainingAlignerModel, new File("../resources/pip_align.dat"))
ReadWrite.writeTo(model.testingAlignerModel, new File("../resources/pip_testAlign.dat"))
ReadWrite.writeTo(model.pronouncerModel, new File("../resources/pip_pron.dat"))
ReadWrite.writeTo(model.rerankerModel, new File("../resources/pip_rr.dat"))

println "done"