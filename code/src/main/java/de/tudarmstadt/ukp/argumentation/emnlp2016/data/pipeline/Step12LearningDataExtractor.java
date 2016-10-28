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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.createdebate.Argument;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 */
public class Step12LearningDataExtractor
{
    /**
     * Returns a map (file name, list(gold label pairs))
     *
     * @param data loaded data
     * @return data
     * @throws IOException exception
     */
    public static Map<String, List<GoldLabelPairContainer>> produceLearningData(
            Map<String, List<AnnotatedArgumentPair>> data)
            throws IOException
    {
        int total = 0;
        int withLabels = 0;

        Map<String, List<GoldLabelPairContainer>> result = new HashMap<>();

        for (Map.Entry<String, List<AnnotatedArgumentPair>> entry : data.entrySet()) {
            for (AnnotatedArgumentPair pair : entry.getValue()) {
                total++;

                Argument moreConvincingArgument = pair.getMoreConvincingArgument();
                Argument lessConvincingArgument = pair.getLessConvincingArgument();

                String moreConvincingArgumentID = moreConvincingArgument.getId();
                String lessConvincingArgumentID = lessConvincingArgument.getId();

                Set<String> collectedLabelsMoreConvincing = new HashSet<>();
                Set<String> collectedLabelsLessConvincing = new HashSet<>();

                int negativeReasonsPerPair = 0;
                int positiveReasonsPerPair = 0;

                for (MTurkAssignment a : pair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits assignment = (MTurkAssignmentWithReasonUnits) a;
                    for (ReasonUnit unit : assignment.getReasonUnits()) {
                        String estimatedGoldLabel = unit.getEstimatedGoldLabel();

                        // if has not yet been filtered and have gold label
                        if (!unit.isFiltered() && estimatedGoldLabel != null) {
                            String parentArgId = unit.getTargetArgumentId(pair);

                            if (parentArgId.equals(moreConvincingArgumentID)) {
                                collectedLabelsMoreConvincing.add(unit.getEstimatedGoldLabel());
                                positiveReasonsPerPair++;
                            }
                            else if (parentArgId.equals(lessConvincingArgumentID)) {
                                collectedLabelsLessConvincing.add(unit.getEstimatedGoldLabel());
                                negativeReasonsPerPair++;
                            }
                        }
                    }
                }

                if (collectedLabelsLessConvincing.contains("o8_1")) {
                    throw new IllegalStateException();
                }

                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new ArrayList<GoldLabelPairContainer>());
                }

                // omit gold data
                if (!collectedLabelsLessConvincing.isEmpty() || !collectedLabelsMoreConvincing
                        .isEmpty()) {
                    // create gold label instance and add to the result
                    GoldLabelPairContainer container = new GoldLabelPairContainer();
                    container.id = pair.getId();
                    container.moreConvincingArgumentText = multipleParagraphsToSingleLine(
                            moreConvincingArgument.getText());
                    container.lessConvincingArgumentText = multipleParagraphsToSingleLine(
                            lessConvincingArgument.getText());
                    container.moreConvincingLabels.addAll(collectedLabelsMoreConvincing);
                    container.lessConvincingLabels.addAll(collectedLabelsLessConvincing);
                    container.debateTopic = entry.getKey().split("_")[0].replaceAll("-", " ")
                            .trim();
                    container.debateStance = entry.getKey().split("_")[1].replaceAll("-", " ")
                            .replace(".xml", "").trim();
                    container.lessConvincingArgumentId = lessConvincingArgumentID;
                    container.moreConvincingArgumentId = moreConvincingArgumentID;

                    result.get(entry.getKey()).add(container);
                }
            }
        }

        System.out.println("Total: " + total);
        System.out.println("With label: " + withLabels);

        return result;
    }


    public static String multipleParagraphsToSingleLine(String s)
    {
        return s.replaceAll("\n", " <br/> ");
    }

    public static void main(String[] args)
            throws IOException
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        Map<String, List<AnnotatedArgumentPair>> data = IOHelper.loadAnnotatedPais(inputDir);

        Map<String, List<GoldLabelPairContainer>> goldData = produceLearningData(data);

        XStreamTools.toXML(goldData, outputDir);
//        saveGoldDataCSV(goldData, outputDir);
    }
}
