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

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.mace.MACE;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkOutputReader;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 *
 */
public class Step13LearningDataValidator
{

    private static final String MTURK_SANDBOX_URL = "https://workersandbox.mturk.com/mturk/externalSubmit";

    private static final String MTURK_ACUTAL_URL = "https://www.mturk.com/mturk/externalSubmit";

    /**
     * Parameter for output HIT files
     */
    private final static String fileNamePattern = "mthit-%05d.html";

    /**
     * How many arguments are shown in one HIT
     */
    private static final int ARGUMENTS_PER_HIT = 4;

    /**
     * Where the mustache template is stored (can also be classpath:xxx if packed in resources folder)
     */
    private final static String SOURCE_MUSTACHE_TEMPLATE = "mturk-template-validation.mustache";

    public static final Random RANDOM = new Random(0);

    public static void statistics(Map<String, List<GoldLabelPairContainer>> data)
    {
        // sanity check - how many items: 9111
        int total = 0;
        for (List<GoldLabelPairContainer> values : data.values()) {
            total += values.size();
        }
        System.out.println("Total pairs: " + total);

        // get all labels together
        Map<String, Integer> concatenatedLabelsCount = new HashMap<>();
        Map<String, Integer> concatenatedLabelsCountMore = new HashMap<>();
        Map<String, Integer> concatenatedLabelsCountLess = new HashMap<>();
        for (List<GoldLabelPairContainer> values : data.values()) {
            for (GoldLabelPairContainer c : values) {
                String valueMore = StringUtils.join(c.moreConvincingLabels, ",");
                String valueLess = StringUtils.join(c.lessConvincingLabels, ",");
                String value = "m:" + valueMore + "-L:" + valueLess;

                if (!concatenatedLabelsCount.containsKey(value)) {
                    concatenatedLabelsCount.put(value, 0);
                }
                if (!concatenatedLabelsCountMore.containsKey(valueMore)) {
                    concatenatedLabelsCountMore.put(valueMore, 0);
                }
                if (!concatenatedLabelsCountLess.containsKey(valueLess)) {
                    concatenatedLabelsCountLess.put(valueLess, 0);
                }

                concatenatedLabelsCount.put(value, concatenatedLabelsCount.get(value) + 1);
                concatenatedLabelsCountMore
                        .put(valueMore, concatenatedLabelsCountMore.get(valueMore) + 1);
                concatenatedLabelsCountLess
                        .put(valueLess, concatenatedLabelsCountLess.get(valueLess) + 1);

                prepareFakedLabels(c.moreConvincingLabels, true);
                prepareFakedLabels(c.lessConvincingLabels, false);
            }
        }

        concatenatedLabelsCount = IOHelper.sortByValue(concatenatedLabelsCount, false);
        System.out.println(
                "All labels concatenated (aka Label Combination): " + concatenatedLabelsCount
                        .size());
        System.out.println(concatenatedLabelsCount);

        concatenatedLabelsCountMore = IOHelper.sortByValue(concatenatedLabelsCountMore, false);
        System.out.println("All labels concatenated (aka Label Combination): MORE "
                + concatenatedLabelsCountMore.size());
        System.out.println(concatenatedLabelsCountMore);

        concatenatedLabelsCountLess = IOHelper.sortByValue(concatenatedLabelsCountLess, false);
        System.out.println("All labels concatenated (aka Label Combination): LESS "
                + concatenatedLabelsCountLess.size());
        System.out.println(concatenatedLabelsCountLess);

    }

    /**
     * Randomly shuffles on side of labels in the data (original data are kept untouched)
     *
     * @param data map (file, list (gold label pair))
     * @return the same but randomly sampled
     */
    public static List<Map<String, GoldLabelPairContainer>> sampleFakedData(
            Map<String, List<GoldLabelPairContainer>> data, int n)
    {
        List<Map<String, GoldLabelPairContainer>> result = new ArrayList<>();

        // collect all pairs first
        List<GoldLabelPairContainer> allPairContainers = new ArrayList<>();

        for (Map.Entry<String, List<GoldLabelPairContainer>> entry : data.entrySet()) {
            allPairContainers.addAll(entry.getValue());
        }

        // shuffle and sample random N
        Collections.shuffle(allPairContainers, RANDOM);
        allPairContainers = allPairContainers.subList(0, n);

        for (GoldLabelPairContainer container : allPairContainers) {
            GoldLabelPairContainer faked = fakeData(container);

            Map<String, GoldLabelPairContainer> map = new HashMap<>();
            map.put("gold", container);
            map.put("faked", faked);
            result.add(map);
        }

        return result;
    }

    /**
     * Randomly shuffles on side of labels in the data (original data are kept untouched)
     *
     * @param goldLabelPairContainer container
     * @return faked container
     */
    public static GoldLabelPairContainer fakeData(GoldLabelPairContainer goldLabelPairContainer)
    {
        // create a new label
        GoldLabelPairContainer result = new GoldLabelPairContainer(goldLabelPairContainer);
        result.isFaked = 1;

        SortedSet<String> lessConvincingAfterRandomSampling = new TreeSet<>(
                goldLabelPairContainer.lessConvincingLabels);
        SortedSet<String> moreConvincingAfterRandomSampling = new TreeSet<>(
                goldLabelPairContainer.moreConvincingLabels);

        // both are non-empty - we pick the side randomly
        if (!goldLabelPairContainer.moreConvincingLabels.isEmpty()
                && !goldLabelPairContainer.lessConvincingLabels.isEmpty()) {
            if (RANDOM.nextBoolean()) {
                moreConvincingAfterRandomSampling = prepareFakedLabels(
                        goldLabelPairContainer.moreConvincingLabels, true);
            }
            else {
                lessConvincingAfterRandomSampling = prepareFakedLabels(
                        goldLabelPairContainer.lessConvincingLabels, false);
            }
        }
        else if (!goldLabelPairContainer.lessConvincingLabels.isEmpty()) {
            lessConvincingAfterRandomSampling = prepareFakedLabels(
                    goldLabelPairContainer.lessConvincingLabels, false);
        }
        else {
            moreConvincingAfterRandomSampling = prepareFakedLabels(
                    goldLabelPairContainer.moreConvincingLabels, true);
        }

        // update the fake pair and add to result
        result.moreConvincingLabels = moreConvincingAfterRandomSampling;
        result.lessConvincingLabels = lessConvincingAfterRandomSampling;

        result.moreConvincingLabelsString = labelsToString(moreConvincingAfterRandomSampling,
                "Argument A1");
        result.lessConvincingLabelsString = labelsToString(lessConvincingAfterRandomSampling,
                "Argument A2");

        return result;
    }

    private static String labelsToString(SortedSet<String> labels, String whichArgument)
    {
        List<String> collected = new ArrayList<>();

        for (String label : labels) {
            collected.add(Step7HITCreator.listAllOptions(true).get(label).answer
                    .replace("Argument X", whichArgument));
        }

        return StringUtils.join(collected, " and ");
    }

    public static SortedSet<String> prepareFakedLabels(Set<String> correctGoldLabels,
            boolean positive)
    {
        SortedSet<String> result = new TreeSet<>();

        Set<String> positiveOptions = new HashSet<>(
                Arrays.asList("o8_1", "o8_4", "o8_5", "o9_1", "o9_2", "o9_3", "o9_4"));
        Set<String> negativeOptions = new HashSet<>(
                Arrays.asList("o5_1", "o5_2", "o5_3", "o6_1", "o6_2", "o6_3", "o7_1", "o7_2",
                        "o7_3", "o7_4"));

        // remove the correct gold labels
        if (positive) {
            positiveOptions.removeAll(correctGoldLabels);
            result.addAll(positiveOptions);
        }
        else {
            negativeOptions.removeAll(correctGoldLabels);
            result.addAll(negativeOptions);
        }

        // can occasionally happen = correct gold labels were all of them
        if (result.isEmpty()) {
            return null;
        }

        // now randomly sample
        List<String> list = new ArrayList<>(result);
        Collections.shuffle(list, RANDOM);
        list = list.subList(0, Math.min(correctGoldLabels.size(), list.size()));
        result = new TreeSet<>(list);

        return result;
    }

    public static void generateHITs(File outputDir, boolean sandbox,
            List<Map<String, GoldLabelPairContainer>> fakedData)
            throws IOException
    {
        Step13LearningDataValidator validator = new Step13LearningDataValidator(sandbox);
        validator.outputPath = outputDir;

        validator.initialize();
        for (Map<String, GoldLabelPairContainer> pair : fakedData) {
            validator.process(pair);
        }
        validator.collectionProcessComplete();
    }

    /**
     * Use sandbox or real MTurk?
     */
    private final boolean sandbox;

    private Mustache mustache;

    File outputPath;

    private List<MTurkHITContainerValidation.ValidationPair> reasonBuffer = new ArrayList<>();

    private int outputFileCounter = 0;

    public Step13LearningDataValidator(boolean sandbox)
    {
        this.sandbox = sandbox;
    }

    public void initialize()
            throws IOException
    {
        InputStream stream = this.getClass().getClassLoader()
                .getResourceAsStream(SOURCE_MUSTACHE_TEMPLATE);
        if (stream == null) {
            throw new FileNotFoundException("Resource not found: " + SOURCE_MUSTACHE_TEMPLATE);
        }

        // compile template
        MustacheFactory mf = new DefaultMustacheFactory();
        Reader reader = new InputStreamReader(stream, "utf-8");
        mustache = mf.compile(reader, "template");

        // output path
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }
    }

    public void process(Map<String, GoldLabelPairContainer> goldLabelPairContainerMap)
            throws IOException
    {
        MTurkHITContainerValidation.ValidationPair validationPair = new MTurkHITContainerValidation.ValidationPair();
        validationPair.goldLabelPairContainers.add(goldLabelPairContainerMap.get("faked"));
        GoldLabelPairContainer goldLabelPairContainer = goldLabelPairContainerMap.get("gold");
        validationPair.goldLabelPairContainers.add(goldLabelPairContainer);
        // and now shuffle
        Collections.shuffle(validationPair.goldLabelPairContainers, RANDOM);

        // set other metadata
        validationPair.pairId = goldLabelPairContainer.id;
        if (goldLabelPairContainer.id == null) {
            throw new NullPointerException("no ID for gold label pair container");
        }
        validationPair.debateTitle = goldLabelPairContainer.debateTopic;
        validationPair.stance = goldLabelPairContainer.debateStance;
        validationPair.arg1text = Arrays
                .asList(goldLabelPairContainer.moreConvincingArgumentText.split("<br/>"));
        validationPair.arg2text = Arrays
                .asList(goldLabelPairContainer.lessConvincingArgumentText.split("<br/>"));

        goldLabelPairContainer.moreConvincingLabelsString = labelsToString(
                goldLabelPairContainer.moreConvincingLabels, "Argument A1");

        goldLabelPairContainer.lessConvincingLabelsString = labelsToString(
                goldLabelPairContainer.lessConvincingLabels, "Argument A2");

        reasonBuffer.add(validationPair);

        if (reasonBuffer.size() >= ARGUMENTS_PER_HIT) {
            flushArgumentBufferToHIT();
        }
    }

    private void flushArgumentBufferToHIT()
            throws IOException
    {
        // fill some data first
        MTurkHITContainerValidation tf = new MTurkHITContainerValidation();
        tf.validationPairs.addAll(this.reasonBuffer);
        tf.numberOfValidationPairs = tf.validationPairs.size();

        // make sure you use the proper type
        if (sandbox) {
            tf.mturkURL = MTURK_SANDBOX_URL;
        }
        else {
            tf.mturkURL = MTURK_ACUTAL_URL;
        }

        // get the correct output file
        File outputHITFile = new File(outputPath,
                String.format(Locale.ENGLISH, fileNamePattern, this.outputFileCounter));

        System.out.println("Generating " + outputHITFile);

        PrintWriter pw = new PrintWriter(outputHITFile);
        this.mustache.execute(pw, tf);
        IOUtils.closeQuietly(pw);

        // increase counter
        this.outputFileCounter++;

        // empty the current buffer
        this.reasonBuffer.clear();
    }

    public void collectionProcessComplete()
            throws IOException
    {
        // fill the rest of the buffer
        if (!reasonBuffer.isEmpty()) {
            flushArgumentBufferToHIT();
        }
    }

    public static void checkValidationResults(File csvFile)
            throws IOException
    {
        MTurkOutputReader reader = new MTurkOutputReader(false, csvFile);

        // map (item, map(worker, value))
        SortedMap<String, SortedMap<String, String>> mapItemWorkerResult = new TreeMap<>();

        for (Map<String, String> row : reader) {
            // worker id
            String workerid = row.get("workerid");

            for (Map.Entry<String, String> entry : row.entrySet()) {
                System.out.println(entry);

                String key = entry.getKey();
                if (key.startsWith("Answer.") && !"Answer.feedback".equals(key)) {
                    // get item id
                    String id = key.split("\\.")[1];
                    String value = entry.getValue();

                    // update results
                    if (!mapItemWorkerResult.containsKey(id)) {
                        mapItemWorkerResult.put(id, new TreeMap<String, String>());
                    }

                    mapItemWorkerResult.get(id).put(workerid, value);
                }
            }

        }

        MACEOutputContainerValidation maceOutput = estimateGoldLabels(mapItemWorkerResult, 0.95);

        System.out.println(maceOutput.goldLabelPredictions.values());

        ConfusionMatrix cm = new ConfusionMatrix();
        int total = 0;
        int nonControlVectorCount = 0;
        Iterator<Boolean> iterator = maceOutput.controlVector.iterator();
        for (String s : maceOutput.goldLabelPredictions.values()) {
            Boolean next = iterator.next();
            if (!next) {
                cm.increaseValue(s, "0");

                total += Integer.valueOf(s);
                nonControlVectorCount++;
            }
        }
        System.out.println((1.0 - ((double) total / nonControlVectorCount)));
        System.out.println("Non-control labeled items: " + nonControlVectorCount);
        System.out.println(cm);
        System.out.println(cm.printNiceResults());


    }

    private static String prepareCSV(SortedMap<String, SortedMap<String, String>> annotations,
            List<String> workerIDs)
    {
        // for each item we need an array of annotations, i.e.
        // label1,,,label1,label2,label1,,,
        // whose size is number of annotators and annotators are identified by position (array index)

        // storing the resulting lines
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, SortedMap<String, String>> entry : annotations.entrySet()) {
            // for storing individual assignments
            String[] assignmentsArray = new String[workerIDs.size()];
            // fill with empty strings
            Arrays.fill(assignmentsArray, "");

            for (Map.Entry<String, String> assignment : entry.getValue().entrySet()) {
                // do we want to include this annotation at all?

                // get the worker index
                int workerIndex = Collections.binarySearch(workerIDs, assignment.getKey());

                // and set the label on the correct position in the array
                assignmentsArray[workerIndex] = assignment.getValue();
            }

            // concatenate with comma
            String line = StringUtils.join(assignmentsArray, ",");

            System.out.println(line);

            result.add(line);
        }

        // add empty line at the end
        result.add("");

        return StringUtils.join(result, "\n");
    }

    public static MACEOutputContainerValidation estimateGoldLabels(
            SortedMap<String, SortedMap<String, String>> mapItemWorkerResult, double threshold)
            throws IOException
    {

        // get sorted turkers
        SortedSet<String> turkerIDsSet = new TreeSet<>();
        for (SortedMap<String, String> values : mapItemWorkerResult.values()) {
            turkerIDsSet.addAll(values.keySet());
        }

        List<String> turkerIDs = new ArrayList<>(turkerIDsSet);
        System.out.println(turkerIDs);

        // save CSV and run MACE
        String preparedCSV = prepareCSV(mapItemWorkerResult, turkerIDs);

        Path tmpDir = Files.createTempDirectory("mace");
        File maceInputFile = new File(tmpDir.toFile(), "input.csv");
        FileUtils.writeStringToFile(maceInputFile, preparedCSV, "utf-8");

        File outputPredictions = new File(tmpDir.toFile(), "predictions.txt");
        File outputCompetence = new File(tmpDir.toFile(), "competence.txt");

        // generate control gold label with probability 0.5
        List<Boolean> controlVector = new ArrayList<>();
        File controls = new File(tmpDir.toFile(), "controls.csv");
        PrintWriter pw = new PrintWriter(controls);
        for (int i = 0; i < mapItemWorkerResult.size(); i++) {
            if (RANDOM.nextDouble() < 0.5) {
                pw.println("0");
                controlVector.add(true);
            }
            else {
                pw.println("");
                controlVector.add(false);
            }
        }
        pw.close();

        // run MACE
        MACE.main(new String[] { "--iterations", "500", "--threshold", String.valueOf(threshold),
                "--restarts", "50", "--outputPredictions", outputPredictions.getAbsolutePath(),
                "--outputCompetence", outputCompetence.getAbsolutePath(), "--controls", controls.getAbsolutePath(),
                maceInputFile.getAbsolutePath() });

        // read back the predictions and competence
        List<String> predictions = FileUtils.readLines(outputPredictions, "utf-8");

        // check the output
        if (predictions.size() != mapItemWorkerResult.size()) {
            throw new IllegalStateException(
                    "Wrong size of the predicted file; expected " + mapItemWorkerResult.size()
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

        SortedMap<String, String> goldLabels = new TreeMap<>();

        // both lists have the same order
        Iterator<String> predictionsIterator = predictions.iterator();
        for (String id : mapItemWorkerResult.keySet()) {
            String predictedLabelLine = predictionsIterator.next().trim();

            // if the line is empty, the label was not estimated (see threshold param in MACE)
            if (!predictedLabelLine.isEmpty()) {
                goldLabels.put(id, predictedLabelLine);
            }
        }

        MACEOutputContainerValidation result = new MACEOutputContainerValidation();
        result.setCompetences(competences);
        result.setGoldLabelPredictions(goldLabels);
        result.setControlVector(controlVector);

        // clean up temporary files
        FileUtils.deleteDirectory(tmpDir.toFile());

        return result;
    }

    private static class MACEOutputContainerValidation
    {

        private SortedMap<String, Double> competences;
        private SortedMap<String, String> goldLabelPredictions;
        private List<Boolean> controlVector;

        public void setCompetences(SortedMap<String, Double> competences)
        {
            this.competences = competences;
        }

        public void setGoldLabelPredictions(SortedMap<String, String> goldLabelPredictions)
        {
            this.goldLabelPredictions = goldLabelPredictions;
        }

        public void setControlVector(List<Boolean> controlVector)
        {
            this.controlVector = controlVector;
        }

        public List<Boolean> getControlVector()
        {
            return controlVector;
        }
    }

    public static void prepareData(String[] args)
            throws IOException
    {
        File goldDataFile = new File(args[0]);
        File outputDir = new File(args[1]);
        Map<String, List<GoldLabelPairContainer>> data = (Map<String, List<GoldLabelPairContainer>>) XStreamTools
                .fromXML(FileUtils.readFileToString(goldDataFile));

        //        statistics(data);
        List<Map<String, GoldLabelPairContainer>> fakedData = sampleFakedData(data, 500);

        System.out.println(fakedData);

        generateHITs(outputDir, false, fakedData);

    }

    @SuppressWarnings("unchecked") public static void main(String[] args)
            throws IOException
    {
        checkValidationResults(new File(args[2]));
    }

}
