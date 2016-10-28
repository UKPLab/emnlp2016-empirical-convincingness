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

import org.apache.commons.lang.StringUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class Step8HITChecker
{

    public static void printComments(File file)
            throws IOException {
        MTurkGoldLabelProvider labelProvider = new MTurkGoldLabelProvider(Collections.singletonList(file), 1.0, false);
        labelProvider.setIgnoreRejected(true);

        System.out.println(StringUtils.join(labelProvider.getFeedbacks().entrySet(), "\n"));

    }

    public static void showBestWorkers(File file) throws IOException {
        MTurkGoldLabelProvider labelProvider = new MTurkGoldLabelProvider(Collections.singletonList(file), 1.0, false);
        labelProvider.setIgnoreRejected(true);


//        System.out.println("Worst ====================================");
//        labelProvider.showFirstNWorkers(100, true);
//        System.out.println("Best =====================================");
//        labelProvider.showFirstNWorkers(50, false);
    }

    public static SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> keepOnlyFirstNAssignmentsPerItem(
            SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> annotations, int firstN,
            boolean reverse)
    {
        SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> result = new TreeMap<>();

        for (Map.Entry<Integer, Map<String, MTurkReasonUnitAssignment>> entry : annotations
                .entrySet()) {

            // get all annotations
            List<MTurkReasonUnitAssignment> list = new ArrayList<>(entry.getValue().values());
            // sort them
            Collections.sort(list, new MTurkReasonUnitAssignment.SubmissionTimeComparator());

            if (reverse) {
                Collections.reverse(list);
            }

            list = list.subList(0, firstN);

            // convert to map
            SortedMap<String, MTurkReasonUnitAssignment> map = new TreeMap<>();
            for (MTurkReasonUnitAssignment assignment : list) {
                map.put(assignment.getTurkID(), assignment);
            }

            // add to the final map
            result.put(entry.getKey(), map);
        }

        return result;
    }

    public static void main(String[] args)
            throws Exception
    {
        showBestWorkers(new File(args[0]));
    }
}