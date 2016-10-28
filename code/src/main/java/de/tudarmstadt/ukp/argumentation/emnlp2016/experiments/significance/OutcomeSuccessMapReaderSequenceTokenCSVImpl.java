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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Reads CVS file from sequence labeling (in SVM HMM). Key is tokenPosition_token to ensure
 * that two sequences match later.
 *
 * @author Ivan Habernal
 */
public class OutcomeSuccessMapReaderSequenceTokenCSVImpl
        implements OutcomeSuccessMapReader
{
    @Override
    public SortedMap<String, Boolean> readOutcomeSuccessMap(File file)
    {
        SortedMap<String, Boolean> result = new TreeMap<String, Boolean>();

        List<String> lines;
        try {
            lines = FileUtils.readLines(file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        // first line is comment
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            String[] split = line.split(",");

            String token = split[2];

            String key = i + "_" + token;
            boolean res = split[0].equals(split[1]);

            result.put(key, res);
        }

        return result;
    }
}
