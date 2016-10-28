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
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan Habernal
 */
public class Step9EstimatedLabelsExtractor
{
    public static void addGoldLabels(File inputDir, File outputDir,
            GoldLabelProvider goldLabelProvider)
            throws IOException
    {
        Map<String, List<AnnotatedArgumentPair>> annotatedPairs = IOHelper
                .loadAnnotatedPais(inputDir);

        Map<String, List<MTurkReasonUnitAssignment>> mapUnitTextAssignments = new HashMap<>();
        Map<String, String> mapUnitTextGoldLabel = new HashMap<>();

        int total = 0;

        for (Map.Entry<String, List<AnnotatedArgumentPair>> entry : annotatedPairs.entrySet()) {
            for (AnnotatedArgumentPair argumentPair : entry.getValue()) {
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        if (!reasonUnit.isIgnored() && !reasonUnit.isDuplicate()) {
                            // get gold label
                            Integer reasonUnitId = Integer.valueOf(reasonUnit.getId());
                            String goldLabel = goldLabelProvider.provideGoldLabel(reasonUnitId);

                            // add gold label and all annotations
                            reasonUnit.setEstimatedGoldLabel(goldLabel);
                            List<MTurkReasonUnitAssignment> assignments = reasonUnit.getAssignments();

                            if (goldLabel != null) {
                                total++;
                            }

                            if (assignments == null) {
                                throw new IllegalStateException();
                            }

                            List<MTurkReasonUnitAssignment> mTurkReasonUnitAssignments = goldLabelProvider
                                    .getMTurkReasonUnitAssignments(reasonUnitId);

                            assignments.addAll(mTurkReasonUnitAssignments);

                            // add to global map
                            String unitText = reasonUnit.getTextForAnnotation();
                            mapUnitTextAssignments.put(unitText, mTurkReasonUnitAssignments);
                            mapUnitTextGoldLabel.put(unitText, goldLabel);

                        }
                    }
                }
            }
        }

        System.out.println("Total " + total + " reason units annotated");


        // second pass for duplicates
        for (Map.Entry<String, List<AnnotatedArgumentPair>> entry : annotatedPairs.entrySet()) {
            for (AnnotatedArgumentPair argumentPair : entry.getValue()) {
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        if (!reasonUnit.isIgnored() && reasonUnit.isDuplicate()) {
                            // get gold label
                            String goldLabel = mapUnitTextGoldLabel.get(reasonUnit.getTextForAnnotation());

                            List<MTurkReasonUnitAssignment> mTurkReasonUnitAssignments = mapUnitTextAssignments
                                    .get(reasonUnit.getTextForAnnotation());

                            // add gold label and all annotations
                            reasonUnit.setEstimatedGoldLabel(goldLabel);

                            if (mTurkReasonUnitAssignments != null) {
                                reasonUnit.getAssignments().addAll(mTurkReasonUnitAssignments);
                            }

                            if (goldLabel != null) {
                                total++;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Including duplicates " + total);


        IOHelper.saveAnnotatedPairs(annotatedPairs, outputDir);
    }

    public static void main(String[] args)
            throws IOException
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String csvFilesString = args[2];
        List<File> csvFiles = new ArrayList<>();
        for (String f : csvFilesString.split(",")) {
            csvFiles.add(new File(f));
        }

        //        GoldLabelProvider goldLabelProvider = new MockGoldLabelProvider();
        GoldLabelProvider goldLabelProvider = new MTurkGoldLabelProvider(csvFiles, 0.95, true);

        addGoldLabels(inputDir, outputDir, goldLabelProvider);
    }
}
