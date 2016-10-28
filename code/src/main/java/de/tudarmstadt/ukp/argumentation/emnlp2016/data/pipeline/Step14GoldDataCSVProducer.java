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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 */
public class Step14GoldDataCSVProducer
{

    public static void saveGoldDataCSV(Map<String, List<GoldLabelPairContainer>> data,
            File outputDir)
            throws IOException
    {
        // first merge two debates into one
        Map<String, List<GoldLabelPairContainer>> merged = new HashMap<>();
        for (Map.Entry<String, List<GoldLabelPairContainer>> entry : data.entrySet()) {
            String fileName = entry.getKey();
            // split by "_"
            String debate = fileName.split("_")[0];

            if (!merged.containsKey(debate)) {
                merged.put(debate, new ArrayList<GoldLabelPairContainer>());
            }

            merged.get(debate).addAll(entry.getValue());
        }


        for (Map.Entry<String, List<GoldLabelPairContainer>> entry : merged.entrySet()) {

            File outputFile = new File(outputDir, entry.getKey() + ".csv");
            PrintWriter pw = new PrintWriter(outputFile);

            for (GoldLabelPairContainer c : entry.getValue()) {
                // write more convincing argument
                List<String> allLabels = new ArrayList<>();
                allLabels.addAll(c.moreConvincingLabels);
                allLabels.addAll(c.lessConvincingLabels);
                pw.printf(Locale.ENGLISH, "%s\t%s\t%s\t%s%n", c.id,
                        StringUtils.join(allLabels, ","), c.moreConvincingArgumentText,
                        c.lessConvincingArgumentText);
            }

            pw.close();
            System.out.println("Written to " + outputFile);

        }

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws IOException
    {
        File goldDataFile = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Map<String, List<GoldLabelPairContainer>> data = (Map<String, List<GoldLabelPairContainer>>) XStreamTools
                .fromXML(FileUtils.readFileToString(goldDataFile));

        saveGoldDataCSV(data, outputDir);
    }
}
