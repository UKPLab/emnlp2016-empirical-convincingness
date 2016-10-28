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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class MTurkResultTools
{

    private final List<AnnotatedArgumentPair> allArgumentPairs;

    @SuppressWarnings("unchecked")
    public MTurkResultTools(File inputDir)
            throws IOException
    {
        // we will process only a subset first
        allArgumentPairs = new ArrayList<>();

        Collection<File> files = IOHelper.listXmlFiles(inputDir);

        for (File file : files) {
            allArgumentPairs
                    .addAll((List<AnnotatedArgumentPair>) XStreamTools.getXStream().fromXML(file));
        }
    }

    public List<MTurkAssignment> listAllTurkerAssignments(String mTurkID)
    {
        List<MTurkAssignment> result = new ArrayList<>();

        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            for (MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                if (mTurkID.equals(assignment.getTurkID())) {
                    result.add(assignment);
                }
            }
        }

        return result;
    }

    public List<String> listAssignmentIDsForRejection(String mTurkID)
    {
        List<String> result = new ArrayList<>();
        for (MTurkAssignment assignment : listAllTurkerAssignments(mTurkID)) {
            result.add(assignment.getAssignmentId());
        }

        return result;
    }

    public List<String> listAllReasons(String mTurkID)
    {
        List<String> result = new ArrayList<>();
        for (MTurkAssignment assignment : listAllTurkerAssignments(mTurkID)) {
            result.add(assignment.getReason());
        }

        Collections.sort(result);

        return result;
    }

    public List<MTurkAssignment> listAllAssignmentsInLastNHours(int hours)
    {
        List<MTurkAssignment> result = new ArrayList<>();
        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            for (MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                Calendar c1 = Calendar.getInstance();
                c1.setTime(assignment.getAssignmentSubmitTime());

                long hoursDiff = ((new Date()).getTime() - c1.getTimeInMillis()) / (1000 * 60 * 60);

                if (hoursDiff < hours) {
                    result.add(assignment);
                }
            }
        }

        return result;
    }

    public void showTop10PerformingWorkers()
    {

        for (Map.Entry<String, Double> entry : listRankedTurkers().entrySet()) {
            List<String> allReasons = listAllReasons(entry.getKey());

        }

    }

    public Map<String, SortedSet<String>> listAllFeedbackComments()
    {
        Map<String, SortedSet<String>> result = new HashMap<>();

        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            for (MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                String hitComment = assignment.getHitComment();

                if (hitComment != null) {
                    String turkID = assignment.getTurkID();

                    if (!result.containsKey(turkID)) {
                        result.put(turkID, new TreeSet<String>());
                    }

                    result.get(turkID).add(hitComment);

                }
            }
        }

        return result;
    }

    public LinkedHashMap<String, Double> listRankedTurkers()
    {
        Map<String, Double> competence = new HashMap<>();

        for (AnnotatedArgumentPair argumentPair : allArgumentPairs) {
            for (MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                String turkID = assignment.getTurkID();
                Double turkCompetence = assignment.getTurkCompetence();

                if (turkCompetence == null) {
                    throw new IllegalStateException("competence is null for " + turkID
                            + "; make sure you run on gold standard data with competence estimates!");
                }

                competence.put(turkID, turkCompetence);
            }
        }

        return IOHelper.sortByValue(competence, false);
    }

    public List<String> listAssignmentsFromWorkerContainingTextForRejection(String workerID,
            String text)
    {
        List<String> result = new ArrayList<>();
        for (MTurkAssignment assignment : listAllTurkerAssignments(
                workerID)) {
            if (text.equals(assignment.getReason())) {
                result.add(assignment.getAssignmentId());
            }
        }

        return result;
    }

    public void showReasonsFromLastNHours(int hours)
    {
        Map<String, List<MTurkAssignment>> assignmentsPerWorker = new HashMap<>();

        for (MTurkAssignment assignment : listAllAssignmentsInLastNHours(
                hours)) {
            if (!assignmentsPerWorker.containsKey(assignment.getTurkID())) {
                assignmentsPerWorker.put(assignment.getTurkID(),
                        new ArrayList<MTurkAssignment>());
            }
            assignmentsPerWorker.get(assignment.getTurkID()).add(assignment);
        }

        for (Map.Entry<String, List<MTurkAssignment>> entry : assignmentsPerWorker
                .entrySet()) {
            System.out.println(
                    "--" + entry.getKey() + " (competence: " + entry.getValue().iterator().next()
                            .getTurkCompetence() + ")");
            List<String> list = listAllReasons(entry.getKey());
            Collections.sort(list);
            System.out.println("   " + StringUtils.join(list, "\n   "));
        }
    }

    /**
     * print all reasons and HIT comments by rank
     */
    public void printAllReasonsRanked()
    {
        for (Map.Entry<String, Double> entry : listRankedTurkers().entrySet()) {
            System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
            // get reasons
            System.out.println("   " + StringUtils.join(listAllReasons(entry.getKey()), "\n   "));
        }
    }

    public void printAllFeedbackRanked()
    {
        LinkedHashMap<String, Double> rankedTurkers = listRankedTurkers();
        for (Map.Entry<String, SortedSet<String>> entry : listAllFeedbackComments().entrySet()) {
            System.out.println(
                    "--" + entry.getKey() + "(rank: " + rankedTurkers.get(entry.getKey()) + ")");
            System.out.println(StringUtils.join(entry.getValue(), "\n"));
        }
    }

}
