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
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ivan Habernal
 */
public class Step6ReasonUnitForAnnotationProducer
{
    private static void prepareData(File inputDir, File outputDir)
            throws IOException
    {
        // reason unit text (modified); all scores of the original workers
        Map<String, List<Double>> allReasonUnitTextsAndCompetences = new TreeMap<>();

        List<File> files = new ArrayList<>(IOHelper.listXmlFiles(inputDir));

        for (File file : files) {
            List argumentPairs = (List) XStreamTools.getXStream().fromXML(file);
            for (Object o : argumentPairs) {
                AnnotatedArgumentPair argumentPair = (AnnotatedArgumentPair) o;
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        String text = modifyReasonUnitText(reasonUnit.getReasonUnitText());

                        if (text == null) {
                            // too short!
                            reasonUnit.setIgnored(true);
                        }
                        else {
                            // check if seen already
                            if (!allReasonUnitTextsAndCompetences.containsKey(text)) {
                                allReasonUnitTextsAndCompetences.put(text, new ArrayList<Double>());
                                // unseen reason unit, annotation candidate

                                reasonUnit.setDuplicate(false);
                            }
                            else {
                                reasonUnit.setDuplicate(true);
                            }

                            // and add the score to get the average
                            double workerCompetence = assignment.getTurkCompetence();
                            allReasonUnitTextsAndCompetences.get(text).add(workerCompetence);

                            // set text
                            reasonUnit.setTextForAnnotation(text);
                        }

                        if (reasonUnit.getTarget() == null) {
                            throw new IllegalStateException(reasonUnit.getId());
                        }
                    }
                }
            }

            // save the file
            File outputFile = new File(outputDir, file.getName());
            XStreamTools.toXML(argumentPairs, outputFile);
        }

        // now the second pass over the data
        List<File> filesSecondPass = new ArrayList<>(IOHelper.listXmlFiles(outputDir));

        for (File file : filesSecondPass) {
            List argumentPairs = (List) XStreamTools.getXStream().fromXML(file);
            for (Object o : argumentPairs) {
                AnnotatedArgumentPair argumentPair = (AnnotatedArgumentPair) o;
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        if (!reasonUnit.isIgnored() && !reasonUnit.isDuplicate()) {
                            String text = reasonUnit.getTextForAnnotation();

                            // compute average
                            List<Double> competences = allReasonUnitTextsAndCompetences.get(text);

                            if (competences.isEmpty()) {
                                throw new IllegalStateException();
                            }

                            // make average
                            double avg = 0.0;
                            for (Double d : competences) {
                                avg += d;
                            }
                            avg = avg / (double) competences.size();

                            reasonUnit.setAverageCompetenceOfOriginalWorkers(avg);
                        }
                    }
                }
            }

            File outputFile = new File(outputDir, file.getName());
            XStreamTools.toXML(argumentPairs, outputFile);
        }
    }

    /**
     * Return modified text or null, if the reason unit is too short
     *
     * @param reasonUnitText text
     * @return modified text or null
     */
    private static String modifyReasonUnitText(String reasonUnitText)
    {
        String result = reasonUnitText.replaceAll("a[12]", "Argument X");
        int words = result.split(" ").length;

        // only 5+ words
        if (words > 4) {
            return result;
        }

        return null;
    }

    public static void main(String[] args)
            throws IOException
    {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        prepareData(inputDir, outputDir);
    }

}
