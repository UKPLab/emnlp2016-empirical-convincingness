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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.annotation.MTurkHITContainer;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import org.apache.commons.math.stat.Frequency;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step10LocalGoldLabelClening
{

    private static final boolean DEBUG = true;

    public static void filterNotExplanationReasonUnits(
            Map<String, List<AnnotatedArgumentPair>> data)
    {
        Map<String, MTurkHITContainer.QuestionOption> options = Step7HITCreator
                .listAllOptions(true);

        int filtered = 0;
        int total = 0;

        for (List<AnnotatedArgumentPair> pairs : data.values()) {
            for (AnnotatedArgumentPair pair : pairs) {
                for (MTurkAssignment a : pair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits assignment = (MTurkAssignmentWithReasonUnits) a;
                    for (ReasonUnit unit : assignment.getReasonUnits()) {
                        String label = unit.getEstimatedGoldLabel();

                        if (label != null) {
                            // is that option o1_2 and also topic-specific explanation
                            if ("o1_2".equals(label) || "o8_6".equals(label)) {
                                unit.setFiltered(true);
                                ++filtered;
                            }

                            ++total;
                        }
                    }
                }
            }
        }

        System.out.println("Filtered " + filtered + " labels out of " + total);
    }

    public static void filterWrongPolarityLabels(Map<String, List<AnnotatedArgumentPair>> data)
    {
        int filtered = 0;
        int total = 0;

        for (List<AnnotatedArgumentPair> pairs : data.values()) {
            for (AnnotatedArgumentPair pair : pairs) {
                // a1 or a2
                String moreConvincingArgument = pair.getGoldLabel();

                for (MTurkAssignment a : pair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits assignment = (MTurkAssignmentWithReasonUnits) a;
                    for (ReasonUnit unit : assignment.getReasonUnits()) {
                        String estimatedGoldLabel = unit.getEstimatedGoldLabel();

                        // if has not yet been filtered and have gold label (some might be empty
                        // from MACE etc.)
                        if (!unit.isFiltered() && estimatedGoldLabel != null) {
                            // get unit target
                            String targetArgument = unit.getTarget().toString();

                            boolean positiveProperty = isLabelPositiveProperty(estimatedGoldLabel);

                            if (DEBUG) {
                                System.out.println(
                                        "More convincing argument: " + moreConvincingArgument +
                                                ", reason unit target argument: " + targetArgument
                                                + ", reason unit label: " + estimatedGoldLabel +
                                                ", is positive: " + positiveProperty);
                            }
                            // more convincing arguments cannot have negative property
                            if (moreConvincingArgument.equals(targetArgument)
                                    && !positiveProperty) {
                                unit.setFiltered(true);
                                ++filtered;
                            }

                            // less convincing arguments canno have positive property
                            if (!moreConvincingArgument.equals(targetArgument)
                                    && positiveProperty) {
                                unit.setFiltered(true);
                                ++filtered;
                            }
                            ++total;
                        }
                    }
                }
            }
        }

        System.out.println("Filtered " + filtered + " labels out of " + total);
    }

    public static void filterContradictionsOnArguments(
            Map<String, List<AnnotatedArgumentPair>> data)
    {
        int filtered = 0;
        int total = 0;

        // collect all edges for all labels (map (argID, pairs))
        Map<String, Set<AnnotatedArgumentPair>> mapArgumentPairs = new HashMap<>();
        // keep parent argument pair for all units (it's not in the data)
        Map<ReasonUnit, MTurkAssignmentWithReasonUnits> mapUnitParenPair = new HashMap<>();

        for (List<AnnotatedArgumentPair> pairs : data.values()) {
            for (AnnotatedArgumentPair pair : pairs) {
                String arg1Id = pair.getArg1().getId();
                String arg2Id = pair.getArg2().getId();

                if (!mapArgumentPairs.containsKey(arg1Id)) {
                    mapArgumentPairs.put(arg1Id, new HashSet<AnnotatedArgumentPair>());
                }
                mapArgumentPairs.get(arg1Id).add(pair);

                if (!mapArgumentPairs.containsKey(arg2Id)) {
                    mapArgumentPairs.put(arg2Id, new HashSet<AnnotatedArgumentPair>());
                }
                mapArgumentPairs.get(arg2Id).add(pair);

                for (MTurkAssignment a : pair.getMTurkAssignments()) {
                    for (ReasonUnit ru : ((MTurkAssignmentWithReasonUnits) a).getReasonUnits()) {
                        mapUnitParenPair.put(ru, (MTurkAssignmentWithReasonUnits) a);
                    }
                }
            }
        }

        Frequency frequency = new Frequency();

        // now for each argument
        for (Map.Entry<String, Set<AnnotatedArgumentPair>> entry : mapArgumentPairs.entrySet()) {
            // get units targeting the argument

            //            System.out.println("--------------");
            String currentArgumentID = entry.getKey();

            List<ReasonUnit> unitsTargetingCurrentArgument = getAllReasonUnitsTargetingArgument(
                    entry.getValue(), currentArgumentID);

            //            System.out.println("Reason units for current argument: " + unitsTargetingCurrentArgument.size());

            //            System.out.println(StringUtils.join(unitsTargetingCurrentArgument, "\n"));

            //            // now let's sort them according to the score
            //            Collections.sort(unitsTargetingCurrentArgument, new Comparator<ReasonUnit>()
            //            {
            //                @Override public int compare(ReasonUnit r1, ReasonUnit r2)
            //                {
            //                    return r1.getAverageCompetenceOfOriginalWorkers()
            //                            .compareTo(r2.getAverageCompetenceOfOriginalWorkers());
            //                }
            //            });

            //            System.out.println(unitsTargetingCurrentArgument);

            Set<ReasonUnit> reasonUnitsToBeRemoved = new HashSet<>();

            // assess cartesian product (half of it) and find contradictions
            for (int i = 0; i < unitsTargetingCurrentArgument.size(); i++) {
                for (int j = i + 1; j < unitsTargetingCurrentArgument.size(); j++) {
                    String contradicts = reasonUnitContradicts(unitsTargetingCurrentArgument.get(i),
                            unitsTargetingCurrentArgument.get(j));
                    if (contradicts != null) {
                        // get scores
                        double originalScoreR1 = mapUnitParenPair
                                .get(unitsTargetingCurrentArgument.get(i)).getTurkCompetence();
                        double originalScoreR2 = mapUnitParenPair
                                .get(unitsTargetingCurrentArgument.get(j)).getTurkCompetence();

                        double currentScoreR1 = unitsTargetingCurrentArgument.get(i)
                                .computeAnnotatedWeight(10);
                        double currentScoreR2 = unitsTargetingCurrentArgument.get(j)
                                .computeAnnotatedWeight(10);

                        // update frequency
                        frequency.addValue(contradicts);

                        // and remove with respect to the score combination
                        if ((originalScoreR1 * currentScoreR1) > (originalScoreR2
                                * currentScoreR2)) {
                            reasonUnitsToBeRemoved.add(unitsTargetingCurrentArgument.get(j));
                        }
                        else {
                            reasonUnitsToBeRemoved.add(unitsTargetingCurrentArgument.get(i));
                        }
                    }
                }
            }

            //            System.out.println("Contradicting reason units to be removed: " + reasonUnitsToBeRemoved );
            filtered += reasonUnitsToBeRemoved.size();
            total += unitsTargetingCurrentArgument.size();

            // update status of removed
            for (ReasonUnit unit : reasonUnitsToBeRemoved) {
                unit.setFiltered(true);
            }

        }

        System.out.println("Filtered " + filtered + " labels out of " + total);

        System.out.println(frequency);

    }

    /**
     * Get all reason units (from the set of argument pairs) whose target argument is the given
     * one (id)
     *
     * @param pairs             argument pairs
     * @param currentArgumentID target id
     * @return list of reason units (never null)
     */
    private static List<ReasonUnit> getAllReasonUnitsTargetingArgument(
            Set<AnnotatedArgumentPair> pairs, String currentArgumentID)
    {
        List<ReasonUnit> result = new ArrayList<>();

        for (AnnotatedArgumentPair pair : pairs) {
            for (MTurkAssignment a : pair.getMTurkAssignments()) {
                MTurkAssignmentWithReasonUnits assignment = (MTurkAssignmentWithReasonUnits) a;
                for (ReasonUnit unit : assignment.getReasonUnits()) {
                    String estimatedGoldLabel = unit.getEstimatedGoldLabel();

                    // if has not yet been filtered and have gold label (some might be empty
                    // from MACE etc.)
                    if (!unit.isFiltered() && estimatedGoldLabel != null) {
                        // get unit target
                        String targetArgument = unit.getTargetArgumentId(pair);

                        if (targetArgument.equals(currentArgumentID)) {
                            result.add(unit);
                        }
                    }
                }
            }
        }

        return result;
    }

    // contradicting classes (see table in the article directory)
    final static Map<String, Set<String>> CONTRADICTIONS = new TreeMap<>();

    static {
        CONTRADICTIONS.put("o5_1", new TreeSet<>(Collections.singletonList("o8_4")));
        CONTRADICTIONS.put("o5_2", new TreeSet<>(Collections.singletonList("o9_1")));
        CONTRADICTIONS.put("o5_3", new TreeSet<>(Collections.singletonList("o9_1")));
        CONTRADICTIONS.put("o6_1", new TreeSet<>(Collections.singletonList("o8_6")));
        CONTRADICTIONS.put("o6_2", new TreeSet<>(Collections.singletonList("o8_1")));
        CONTRADICTIONS.put("o6_3", new TreeSet<>(Arrays.asList("o8_5", "o8_6")));
        CONTRADICTIONS
                .put("o7_1", new TreeSet<>(Arrays.asList("o8_4", "o8_5", "o8_6", "o9_1", "o9_4")));
        CONTRADICTIONS.put("o7_2", new TreeSet<>(Arrays.asList("o8_2", "o8_6", "o9_4")));
        CONTRADICTIONS.put("o7_3", new TreeSet<>(
                Arrays.asList("o8_1", "o8_4", "o8_5", "o8_6", "o9_1", "o9_2", "o9_4")));
        CONTRADICTIONS.put("o7_4", new TreeSet<>(Arrays.asList("o8_6", "o9_1", "o9_3", "o9_4")));
    }

    /**
     * Returns null if there is no contradiction, otherwise returns string with contradiction
     * type
     *
     * @param ru1 reason unit 1
     * @param ru2 reason unit 2
     * @return string or null
     */
    private static String reasonUnitContradicts(ReasonUnit ru1, ReasonUnit ru2)
    {
        // ignore the same units
        if (ru1 == ru2) {
            return null;
        }

        // get both labels and sort them
        List<String> labels = Arrays
                .asList(ru1.getEstimatedGoldLabel(), ru2.getEstimatedGoldLabel());
        Collections.sort(labels);

        // if the entry is in the table, return true, false otherwise
        if (CONTRADICTIONS.containsKey(labels.get(0)) && CONTRADICTIONS.get(labels.get(0))
                .contains(labels.get(1))) {
            return labels.toString();
        }

        return null;
    }

    /**
     * Returns true if the reason unit label describes positive properties
     *
     * @param label label
     * @return boolean value
     */
    static boolean isLabelPositiveProperty(String label)
    {
        //"o8" 1 4 5 6 "o9" 1 2 3 4
        return label.startsWith("o8") || label.startsWith("o9");
    }

    public static void main(String[] args)
            throws IOException
    {
        File inputDir = new File(args[0]);
        Map<String, List<AnnotatedArgumentPair>> data = IOHelper.loadAnnotatedPais(inputDir);
        filterNotExplanationReasonUnits(data);
        filterWrongPolarityLabels(data);
        filterContradictionsOnArguments(data);

        // save data
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        IOHelper.saveAnnotatedPairs(data, outputDir);
    }

}
