/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.argumentation.emnlp2016.experiments.significance;

import java.io.File;
import java.util.SortedMap;

/**
 * Reads the outcome (id2outcome.txt, csv, etc.) and for each unique outcome (id, position, etc.)
 * returns whether the classifier was successful (true) or not (false).
 *
 * @author Ivan Habernal
 */
public interface OutcomeSuccessMapReader
{
    /**
     * Returns a map where key is the tested instance ID and value is whether the classifier was
     * correct (true) or wrong (false)
     *
     * @param file file (id2outcome.txt, or similar)
     * @return map (never null)
     */
    public SortedMap<String, Boolean> readOutcomeSuccessMap(File file);
}
