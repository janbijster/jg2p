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

package com.github.steveash.jg2p.seqvow;

import java.io.Serializable;

/**
 * @author Steve Ash
 */
public interface RetaggerPipe extends Serializable {

  /**
   * Emit features for this gram index (which you can assume is a valid one containing at least one partialPhone)
   * @param gramIndex
   * @param tagging
   */
  void pipe(int gramIndex, PartialTagging tagging);
}
