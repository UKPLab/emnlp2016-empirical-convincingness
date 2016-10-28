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
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.mace.MACE;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkOutputReader;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides gold data by estimating them from MTurk annotations using MACE tool
 * <p>
 * @author Ivan Habernal
 */
public class MTurkGoldLabelProvider
        implements GoldLabelProvider {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy",
            Locale.US);
    private final double threshold;

    private MACEOutputContainer maceOutputAll;
    private Map<String, List<String>> feedbacks;

    private boolean ignoreRejected = false;

    /**
     * Map (item ID, map (WorkerID, Assignment))
     */
    private final SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> extractedRawDataItemView;

    /**
     * Map (Worker ID, map (item ID, Assignment))
     */
    private SortedMap<String, SortedMap<Integer, MTurkReasonUnitAssignment>> extractedRawDataWorkerView;

    /**
     * Estimates gold labels from the given MTurk file
     *
     * @param files     CSV file from MTurk
     * @param threshold MACE threshold
     * @throws IOException exception
     */
    public MTurkGoldLabelProvider(List<File> files, double threshold, boolean readOnlyAcceptedOrRejected)
            throws IOException {
        this.threshold = threshold;
        Map<String, MTurkHITContainer.QuestionOption> finalCategories = Step7HITCreator
                .listAllOptions(true);

        // extract raw data
        extractedRawDataItemView = extractAnnotationsFromFile(files, finalCategories.keySet(),
                readOnlyAcceptedOrRejected);
    }

    /**
     * Lazy initialization of gold data predictions
     */
    private void computeGoldData() {
        try {
            maceOutputAll = estimateGoldDataUsingMACE(extractedRawDataItemView, this.threshold);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // make a new view (for each worker)
        extractedRawDataWorkerView = transformItemViewToWorkerView(extractedRawDataItemView);

        // update scores for each worker
        for (Map.Entry<String, SortedMap<Integer, MTurkReasonUnitAssignment>> entry : extractedRawDataWorkerView.entrySet()) {
            // get worker competence
            Double competence = maceOutputAll.getCompetences().get(entry.getKey());

            // and add to all his entries
            for (MTurkReasonUnitAssignment assignment : entry.getValue().values()) {
                assignment.setTurkCompetence(competence);
            }
        }
    }

    /**
     * Creates a new map with main key - worker ID
     *
     * @param extractedRawDataItemView existing raw map - main key is item id
     * @return new map ("view")
     */
    private static SortedMap<String, SortedMap<Integer, MTurkReasonUnitAssignment>> transformItemViewToWorkerView(
            SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> extractedRawDataItemView) {
        SortedMap<String, SortedMap<Integer, MTurkReasonUnitAssignment>> result = new TreeMap<>();

        for (Map.Entry<Integer, Map<String, MTurkReasonUnitAssignment>> entry : extractedRawDataItemView
                .entrySet()) {
            Integer itemId = entry.getKey();
            Map<String, MTurkReasonUnitAssignment> itemAssignments = entry.getValue();

            for (MTurkReasonUnitAssignment assignment : itemAssignments.values()) {
                String turkID = assignment.getTurkID();
                if (!result.containsKey(turkID)) {
                    result.put(turkID, new TreeMap<Integer, MTurkReasonUnitAssignment>());
                }

                result.get(turkID).put(itemId, assignment);
            }
        }

        return result;
    }

    /**
     * Extracts annotations from a single CSV file
     *
     * @param csvFile                    MTurk file
     * @param allowedLabels              one of these labels must be selected
     * @param readOnlyAcceptedOrRejected if false, also assignments in "Submitted" state are allowed
     * @return map (reasonUnit ID; map(worker ID; assignment))
     * @throws IOException exception
     */
    SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> extractAnnotationsFromFile(
            List<File> csvFile, Set<String> allowedLabels, boolean readOnlyAcceptedOrRejected)
            throws IOException {
        MTurkOutputReader reader = new MTurkOutputReader(readOnlyAcceptedOrRejected, csvFile.toArray(new File[csvFile.size()]));

        removeIncostistentRejectedRows(reader, allowedLabels);

        SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> result = new TreeMap<>();

        feedbacks = new TreeMap<>();

        for (Map<String, String> row : reader) {
            String feedback = row.get("Answer.feedback");
            if (feedback != null) {
                String workerID = row.get("workerid");
                if (!feedbacks.containsKey(workerID)) {
                    feedbacks.put(workerID, new ArrayList<String>());
                }

                feedbacks.get(workerID).add(feedback);
                System.out.println(feedback);
            }

            // extract all assignments

            for (String columnKey : row.keySet()) {
                String reasonUnitID = extractAnnotatedReasonUnitID(columnKey);

                if (reasonUnitID != null) {
                    //                    System.out.println("ReasonUnitID: " + reasonUnitID);

                    String cellValue = row.get(columnKey);

                    //                    System.out.println(cellValue);

                    String label = extractLabelromAnswer(cellValue);

                    // if this is the final category, we'll create a new mTurk assignment
                    if (allowedLabels.contains(label)) {
                        MTurkReasonUnitAssignment assignment = createAssignmentFromRow(row, label);

                        int reasonUnitIdInt = Integer.valueOf(reasonUnitID);

                        if (!result.containsKey(reasonUnitIdInt)) {
                            result.put(reasonUnitIdInt,
                                    new TreeMap<String, MTurkReasonUnitAssignment>());
                        }

                        // we cannot have the more results for the same worker
                        Set<String> existingWorkerOnThisItem = result.get(reasonUnitIdInt).keySet();

                        if (existingWorkerOnThisItem.contains(assignment.getTurkID())) {
                            System.out.println(
                                    "Existing assignments: " + result.get(reasonUnitIdInt));
                            System.out.println("cell value: " + cellValue);
                            throw new IllegalStateException(
                                    "We already have assignment from this worker");
                        }

                        // and add to the set
                        result.get(reasonUnitIdInt).put(assignment.getTurkID(), assignment);
                    }

                    //                    System.out.println(category);
                }
            }
        }

        return result;
    }

    /**
     * Removes all rows from the MTurk output that have been rejected. If the row is inconsistent
     * (multiple assignments from the same worker for the same question), an exception is thrown
     *
     * @param reader        reader
     * @param allowedLabels final labels for questions
     */
    private void removeIncostistentRejectedRows(MTurkOutputReader reader, Set<String> allowedLabels) {
        Iterator<Map<String, String>> iterator = reader.iterator();

        List<String> errorRows = new ArrayList<>();

        while (iterator.hasNext()) {
            Map<String, String> row = iterator.next();

            boolean removeRow = false;

            Map<String, MTurkReasonUnitAssignment> singleRowAssignments = new TreeMap<>();

            for (String columnKey : row.keySet()) {
                String reasonUnitID = extractAnnotatedReasonUnitID(columnKey);

                // only if this is an answer to a question with ID
                if (reasonUnitID != null) {
                    String cellValue = row.get(columnKey);

                    String category = extractLabelromAnswer(cellValue);

                    // if this is the final category, we'll create a new mTurk assignment
                    if (allowedLabels.contains(category)) {
                        MTurkReasonUnitAssignment assignment = createAssignmentFromRow(row,
                                category);

                        if (!singleRowAssignments.containsKey(reasonUnitID)) {
                            singleRowAssignments.put(reasonUnitID, assignment);
                        } else {
                            if ("Rejected".equals(row.get("assignmentstatus"))) {
                                // remove this line
                                removeRow = true;
                            } else {
                                errorRows.add(
                                        "More than one answer for " + reasonUnitID + ": "
                                                + singleRowAssignments.get(reasonUnitID).getValue()
                                                + " vs. " + assignment.getValue()
                                                + ", inconsistent row. " + row + "\n" +
                                                "This HIT has should have been rejected");
                            }
                        }
                    }
                }
            }

            // remove it
            if (removeRow) {
                iterator.remove();
            }
        }

        if (!errorRows.isEmpty()) {
            throw new IllegalStateException(StringUtils.join(errorRows, "\n"));
        }

    }

    /**
     * Creates a new assignment and fills required fields
     *
     * @param record row
     * @param value  selected answer (label) by worker
     * @return new instance
     */
    private static MTurkReasonUnitAssignment createAssignmentFromRow(Map<String, String> record,
                                                                     String value) {
        String hitID = record.get("hitid");
        String workerID = record.get("workerid");
        String assignmentId = record.get("assignmentid");

        try {
            Date acceptTime = DATE_FORMAT.parse(record.get("assignmentaccepttime"));
            Date submitTime = DATE_FORMAT.parse(record.get("assignmentsubmittime"));

            return new MTurkReasonUnitAssignment(workerID, hitID, assignmentId, acceptTime,
                    submitTime, value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the label (categoery) from cell value, i.e., "o8_q1_o7_1" is "o7_1"
     *
     * @param answer cell value
     * @return label
     */
    private static String extractLabelromAnswer(String answer) {
        return answer.split("_q\\d+_")[1];
    }

    static MACEOutputContainer estimateGoldDataUsingMACE(
            SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> annotations,
            double threshold)
            throws IOException {
        // get sorted turkers
        SortedSet<String> turkerIDsSet = getAllTurkers(annotations);
        List<String> turkerIDs = new ArrayList<>(turkerIDsSet);
        // save CSV and run MACE
        String preparedCSV = prepareCSV(annotations, turkerIDs);

        Path tmpDir = Files.createTempDirectory("mace");
        File maceInputFile = new File(tmpDir.toFile(), "input.csv");
        FileUtils.writeStringToFile(maceInputFile, preparedCSV, "utf-8");

        File outputPredictions = new File(tmpDir.toFile(), "predictions.txt");
        File outputCompetence = new File(tmpDir.toFile(), "competence.txt");

        // run MACE
        MACE.main(new String[]{"--iterations", "500", "--threshold", String.valueOf(threshold),
                "--restarts", "50", "--outputPredictions", outputPredictions.getAbsolutePath(),
                "--outputCompetence", outputCompetence.getAbsolutePath(),
                maceInputFile.getAbsolutePath()});

        // read back the predictions and competence
        List<String> predictions = FileUtils.readLines(outputPredictions, "utf-8");

        // check the output
        if (predictions.size() != annotations.size()) {
            throw new IllegalStateException(
                    "Wrong size of the predicted file; expected " + annotations.size()
                            + " lines but was " + predictions.size());
        }

        String competenceRaw = FileUtils.readFileToString(outputCompetence, "utf-8");
        String[] competence = competenceRaw.split("\t");
        if (competence.length != turkerIDs.size()) {
            throw new IllegalStateException(
                    "Expected " + turkerIDs.size() + " competence number, got "
                            + competence.length);
        }

        // collect worker competences
        SortedMap<String, Double> competences = new TreeMap<>();
        for (int i = 0; i < competence.length; i++) {
            // get worker ID
            String worker = turkerIDs.get(i);
            // and competence
            Double c = Double.valueOf(competence[i]);

            competences.put(worker, c);
        }

        SortedMap<Integer, String> goldLabels = new TreeMap<>();

        // both lists have the same order
        Iterator<String> predictionsIterator = predictions.iterator();
        for (Integer id : annotations.keySet()) {
            String predictedLabelLine = predictionsIterator.next().trim();

            // if the line is empty, the label was not estimated (see threshold param in MACE)
            if (!predictedLabelLine.isEmpty()) {
                goldLabels.put(id, predictedLabelLine);
            }
        }

        MACEOutputContainer result = new MACEOutputContainer();
        result.setCompetences(competences);
        result.setGoldLabelPredictions(goldLabels);

        // clean up temporary files
        FileUtils.deleteDirectory(tmpDir.toFile());

        return result;
    }

    private static String prepareCSV(
            SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> annotations,
            List<String> turkerIDs) {
        // for each item we need an array of annotations, i.e.
        // label1,,,label1,label2,label1,,,
        // whose size is number of annotators and annotators are identified by position (array index)

        // storing the resulting lines
        List<String> result = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, MTurkReasonUnitAssignment>> entry : annotations
                .entrySet()) {
            // for storing individual assignments
            String[] assignmentsArray = new String[turkerIDs.size()];
            // fill with empty strings
            Arrays.fill(assignmentsArray, "");

            for (MTurkReasonUnitAssignment assignment : entry.getValue().values()) {
                // do we want to include this annotation at all?

                // get the turker index
                int turkerIndex = Collections.binarySearch(turkerIDs, assignment.getTurkID());

                // and set the label on the correct position in the array
                assignmentsArray[turkerIndex] = assignment.getValue();

            }

            // concatenate with comma
            String line = StringUtils.join(assignmentsArray, ",");

            // System.out.println(line);

            result.add(line);
        }

        // add empty line at the end
        result.add("");

        return StringUtils.join(result, "\n");
    }

    /**
     * Extract all worker IDs from the parsed CSV file
     *
     * @param annotations annotations
     * @return sorted set of workers
     */
    private static SortedSet<String> getAllTurkers(
            SortedMap<Integer, Map<String, MTurkReasonUnitAssignment>> annotations) {
        // first, find all workers
        SortedSet<String> turkerIDsSet = new TreeSet<>();
        for (Map<String, MTurkReasonUnitAssignment> values : annotations.values()) {
            // keys are actually turker IDs
            turkerIDsSet.addAll(values.keySet());
        }

        return turkerIDsSet;
    }

    /**
     * Splits "Answer.46566_q4_group" and returns "46566" and "q4_group"
     *
     * @param columnName column name
     * @return array or null, if cannot be split (not proper column name)
     */
    static String[] splitColumnNameToIdAndQuestionId(String columnName) {
        if (!columnName.startsWith("Answer.")) {
            return null;
        }
        //        Answer.46566_q4_group
        String answerRaw = columnName.replaceAll("^Answer\\.", "");

        String[] split = answerRaw.split("_", 2);

        if (split.length != 2) {
            return null;
        }

        return split;
    }

    private static String extractAnnotatedReasonUnitID(String columnName) {
        String[] strings = splitColumnNameToIdAndQuestionId(columnName);

        return strings == null ? null : strings[0];

    }

    public Map<String, List<String>> getFeedbacks() {
        return feedbacks;
    }

    @Override
    public String provideGoldLabel(int reasonUnitId) {
        if (maceOutputAll == null) {
            computeGoldData();
        }


        return maceOutputAll.getGoldLabelPredictions().get(reasonUnitId);
    }

    @Override
    public List<MTurkReasonUnitAssignment> getMTurkReasonUnitAssignments(int reasonUnitId) {
        if (this.extractedRawDataItemView.containsKey(reasonUnitId)) {
            return new ArrayList<>(this.extractedRawDataItemView.get(reasonUnitId).values());
        }

        return new ArrayList<>();
    }

    public void showFirstNWorkers(int n, boolean worst) {
        if (maceOutputAll == null) {
            computeGoldData();
        }

        LinkedHashMap<String, Double> competencesSorted = IOHelper
                .sortByValue(maceOutputAll.getCompetences(), worst);

        Iterator<Map.Entry<String, Double>> iterator = competencesSorted.entrySet().iterator();

        for (int i = 0; i < n; i++) {
            Map.Entry<String, Double> entry = iterator.next();
            String workerID = entry.getKey();
            SortedMap<Integer, MTurkReasonUnitAssignment> assignments = this.extractedRawDataWorkerView
                    .get(workerID);

            System.out.println("---- Worker " + workerID);
            System.out.println("HITs: " + assignments.size() / 8);
            System.out.printf(Locale.ENGLISH, "MACE Score: %.6f%n",
                    maceOutputAll.getCompetences().get(workerID));
            DescriptiveStatistics submitTimeStats = collectSubmitTime(assignments.values());
            System.out.printf(Locale.ENGLISH, "Average submit time: %.0f±%.0f%n",
                    submitTimeStats.getMean(), submitTimeStats.getStandardDeviation());
        }
    }

    /**
     * Collects statistics about submit time (in seconds)
     *
     * @param assignments assignments
     * @return statistics
     */
    public static DescriptiveStatistics collectSubmitTime(
            Collection<MTurkReasonUnitAssignment> assignments) {
        Map<String, Integer> hitSubmitTimes = new HashMap<>();

        for (MTurkReasonUnitAssignment assignment : assignments) {
            hitSubmitTimes.put(assignment.getHitID(), assignment.getSubmitTimeInSeconds());
        }

        DescriptiveStatistics result = new DescriptiveStatistics();

        for (Integer submitTime : hitSubmitTimes.values()) {
            result.addValue(submitTime);
        }

        List<Integer> sorted = new ArrayList<>(hitSubmitTimes.values());
        Collections.sort(sorted);

        System.out.println("Detailed submit times: " + sorted);

        return result;
    }

    public void generateSpammersRejections(File outputFile, String... workerIds)
            throws IOException {
        if (this.extractedRawDataWorkerView == null) {
            extractedRawDataWorkerView = transformItemViewToWorkerView(extractedRawDataItemView);
        }

        Set<String> assignmentsToReject = new HashSet<>();
        for (String workerId : workerIds) {
            if (!this.extractedRawDataWorkerView.containsKey(workerId)) {
                throw new IllegalStateException(
                        "Worker not present in this.extractedRawDataWorkerView");
            }

            for (MTurkReasonUnitAssignment assignment : this.extractedRawDataWorkerView
                    .get(workerId).values()) {
                assignmentsToReject.add(assignment.getAssignmentId());
            }

            System.out.println("./blockWorker.sh -workerid " + workerId
                    + " -reason \"Blocked due to suspiciously short HIT submission times and low quality answers.\"");
        }

        PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
        pw.println("assignmentIdToReject\tassignmentIdToRejectComment");
        for (String a : assignmentsToReject) {
            pw.println(a
                    + "\t\"Dear worker, we appreciate your work, but you provided very low quality answers.\"");
        }

        pw.close();
    }

    public boolean isIgnoreRejected() {
        return ignoreRejected;
    }

    public void setIgnoreRejected(boolean ignoreRejected) {
        this.ignoreRejected = ignoreRejected;
    }
}
