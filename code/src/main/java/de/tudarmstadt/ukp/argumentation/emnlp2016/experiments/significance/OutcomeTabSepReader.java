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
 * (c) 2016 Ivan Habernal
 */
public class OutcomeTabSepReader
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
        for (String line : lines) {

            String[] split = line.split("\t");

            String id = split[0];
            String gold = split[1];
            String pred = split[2];

            boolean res = gold.equals(pred);

            result.put(id, res);
        }

        return result;
    }
}
