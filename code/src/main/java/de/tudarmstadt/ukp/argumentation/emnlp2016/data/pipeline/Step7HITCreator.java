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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.annotation.MTurkHITContainer;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import org.apache.commons.io.IOUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.*;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step7HITCreator
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
    private static final int ARGUMENTS_PER_HIT = 8;

    /**
     * Where the mustache template is stored (can also be classpath:xxx if packed in resources folder)
     */
    private final static String SOURCE_MUSTACHE_TEMPLATE = "mturk-template.mustache";
    private static final String ASPECTS_STRING = "How would you rephrase the explanation? (pick the most appropriate category)";

    /**
     * Use sandbox or real MTurk?
     */
    private final boolean sandbox;

    private Mustache mustache;

    File outputPath;

    private List<MTurkHITContainer.HITReasonUnit> reasonBuffer = new ArrayList<>();

    private int outputFileCounter = 0;

    public Step7HITCreator(boolean sandbox)
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

    public void process(ReasonUnit reasonUnit)
            throws IOException
    {
        MTurkHITContainer.HITReasonUnit hitReasonUnit = new MTurkHITContainer.HITReasonUnit();
        hitReasonUnit.reasonId = reasonUnit.getId();
        hitReasonUnit.text = reasonUnit.getTextForAnnotation();

        reasonBuffer.add(hitReasonUnit);

        if (reasonBuffer.size() >= ARGUMENTS_PER_HIT) {
            flushArgumentBufferToHIT();
        }
    }

    private void flushArgumentBufferToHIT()
            throws IOException
    {
        // fill some data first
        MTurkHITContainer tf = new MTurkHITContainer();

        tf.reasonUnitList.addAll(this.reasonBuffer);

        tf.numberOfReasonUnits = tf.reasonUnitList.size();

        // prepare questions
        for (MTurkHITContainer.HITReasonUnit hitReasonUnit : tf.reasonUnitList) {
            hitReasonUnit.annotationQuestions = new ArrayList<>(prepareAnnotationQuestions());
        }

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

    public static List<MTurkHITContainer.AnnotationQuestion> prepareAnnotationQuestions()
    {
        List<MTurkHITContainer.AnnotationQuestion> result = new ArrayList<>();

        // first question
        MTurkHITContainer.AnnotationQuestion q1 = new MTurkHITContainer.AnnotationQuestion("q1",
                "Does the explanation explain why Argument X is more/less convincing?",
                "If the explanation does not make any sense (for example looks like an incomplete part of a sentence), mark is as 'No'. If the explanation has only minor issues (starts with 'but', 'although', etc.) but in general makes sense, do not mark it as 'No'");
        MTurkHITContainer.QuestionOption o1_1 = new MTurkHITContainer.QuestionOption("o1_1", "Yes",
                null, "q2");
        MTurkHITContainer.QuestionOption o1_2 = new MTurkHITContainer.QuestionOption("o1_2", "No",
                "'Argument X does not' ← Incomplete; 'Argument X presents a far better argument' ← Not saying why; 'Argument X has a much better valid argument' ← Not saying why",
                null);
        q1.options.add(o1_1);
        q1.options.add(o1_2);
        // set the first question flag
        q1.firstQuestion = true;

        // second question
        MTurkHITContainer.AnnotationQuestion q2 = new MTurkHITContainer.AnnotationQuestion("q2",
                "Does the explanation address positive properties of Argument X?", null);
        MTurkHITContainer.QuestionOption o2_1 = new MTurkHITContainer.QuestionOption("o2_1",
                "Yes, it addresses positive properties", null, "q4");

        MTurkHITContainer.QuestionOption o2_2 = new MTurkHITContainer.QuestionOption("o2_2",
                "No, it addresses negative properties",
                "'Argument X doesn't consider xxx', 'Argument X lacks xxx', 'Argument X only attacks' Attention: 'Argument X doesn't misuse xxx' and similar are not negative properties.",
                "q3");
        q2.options.add(o2_1);
        q2.options.add(o2_2);

        // third question - negative
        MTurkHITContainer.AnnotationQuestion q3 = new MTurkHITContainer.AnnotationQuestion("q3",
                "What is the explanation mostly related to?", null);
        MTurkHITContainer.QuestionOption o3_1 = new MTurkHITContainer.QuestionOption("o3_1",
                "Explanation is related to language / presentation of Argument X",
                "language, sarcasm, attacking, clarity issues, etc.", "q5");
        MTurkHITContainer.QuestionOption o3_2 = new MTurkHITContainer.QuestionOption("o3_2",
                "Explanation is related to the content of Argument X",
                "Missing or weak reasons, weak logic, etc.", "q6");
        MTurkHITContainer.QuestionOption o3_3 = new MTurkHITContainer.QuestionOption("o3_3",
                "Explanation is a general remark about Argument X",
                "No-sense, off-topic, vague, generally weak, rant, not arguing anything, etc.",
                "q7");
        q3.options.add(o3_1);
        q3.options.add(o3_2);
        q3.options.add(o3_3);

        // fourth question - positive
        MTurkHITContainer.AnnotationQuestion q4 = new MTurkHITContainer.AnnotationQuestion("q4",
                "What is the explanation mostly related to?", null);
        MTurkHITContainer.QuestionOption o4_1 = new MTurkHITContainer.QuestionOption("o4_1",
                "Explanation is related to content of Argument X",
                "Explanation addresses details, reasons, balance, logic of Argument X", "q8");
        MTurkHITContainer.QuestionOption o4_2 = new MTurkHITContainer.QuestionOption("o4_2",
                "Explanation makes a general remark about Argument X",
                "Explanation points to good writing, provoking, etc.", "q9");
        q4.options.add(o4_1);
        q4.options.add(o4_2);

        // fifth question - negative
        MTurkHITContainer.AnnotationQuestion q5 = new MTurkHITContainer.AnnotationQuestion("q5",
                ASPECTS_STRING, null);
        MTurkHITContainer.QuestionOption o5_1 = new MTurkHITContainer.QuestionOption("o5_1",
                "Argument X is attacking opponent / abusive", null, null);
        MTurkHITContainer.QuestionOption o5_2 = new MTurkHITContainer.QuestionOption("o5_2",
                "Argument X has language issues / bad grammar / uses humor, jokes, or sarcasm",
                null, null);
        MTurkHITContainer.QuestionOption o5_3 = new MTurkHITContainer.QuestionOption("o5_3",
                "Argument X is unclear, hard to follow", null, null);
        q5.options.add(o5_1);
        q5.options.add(o5_2);
        q5.options.add(o5_3);

        // sixth question - negative
        MTurkHITContainer.AnnotationQuestion q6 = new MTurkHITContainer.AnnotationQuestion("q6",
                ASPECTS_STRING, null);
        MTurkHITContainer.QuestionOption o6_1 = new MTurkHITContainer.QuestionOption("o6_1",
                "Argument X provides no facts / not enough support / not credible evidence / no clear explanation",
                "'Argument X is providing no actual facts'", null);
        MTurkHITContainer.QuestionOption o6_2 = new MTurkHITContainer.QuestionOption("o6_2",
                "Argument X has no reasoning / less or insufficient reasoning",
                "'Argument X does not make an effective connection between the A and B'", null);
        MTurkHITContainer.QuestionOption o6_3 = new MTurkHITContainer.QuestionOption("o6_3",
                "Argument X uses irrelevant reasons / irrelevant information", null, null);
        q6.options.add(o6_1);
        q6.options.add(o6_2);
        q6.options.add(o6_3);

        // seventh question - negative
        MTurkHITContainer.AnnotationQuestion q7 = new MTurkHITContainer.AnnotationQuestion("q7",
                ASPECTS_STRING, null);
        MTurkHITContainer.QuestionOption o7_1 = new MTurkHITContainer.QuestionOption("o7_1",
                "Argument X is not an argument / is only opinion / is rant", null, null);
        MTurkHITContainer.QuestionOption o7_2 = new MTurkHITContainer.QuestionOption("o7_2",
                "Argument X is non-sense / has no logical sense / confusing", null, null);
        MTurkHITContainer.QuestionOption o7_3 = new MTurkHITContainer.QuestionOption("o7_3",
                "Argument X is off topic / doesn't address the issue",
                "'Argument X seems to be off-topic'; 'Argument X does not argue the question'",
                null);
        MTurkHITContainer.QuestionOption o7_4 = new MTurkHITContainer.QuestionOption("o7_4",
                "Argument X is generally weak / vague",
                "'Argument X does not really have teeth', 'Argument X does not quite try as hard', 'Argument X doesn't argue anything'",
                null);
        q7.options.add(o7_1);
        q7.options.add(o7_2);
        q7.options.add(o7_3);
        q7.options.add(o7_4);

        // eight question - positive
        MTurkHITContainer.AnnotationQuestion q8 = new MTurkHITContainer.AnnotationQuestion("q8",
                ASPECTS_STRING, null);
        MTurkHITContainer.QuestionOption o8_1 = new MTurkHITContainer.QuestionOption("o8_1",
                "Argument X has more details, information, facts, or examples / more reasons / better reasoning / goes deeper / is more specific",
                null, null);
        //        MTurkHITContainer.QuestionOption o8_2 = new MTurkHITContainer.QuestionOption("o8_2",
        //                "Argument X gives facts or examples", null, null);
        //        MTurkHITContainer.QuestionOption o8_3 = new MTurkHITContainer.QuestionOption("o8_3",
        //                "Argument X has better reasoning or logic",
        //                "'Argument X does not contradicts itself'", null);
        MTurkHITContainer.QuestionOption o8_4 = new MTurkHITContainer.QuestionOption("o8_4",
                "Argument X is balanced, objective, discusses several viewpoints / well-rounded / tackles flaws in opposing views",
                "'Argument X considers both sides', 'Argument X is argued on more than one level', 'Argument X gets rid of any ideas that might have opposed the claim'",
                null);
        MTurkHITContainer.QuestionOption o8_5 = new MTurkHITContainer.QuestionOption("o8_5",
                "Argument X has better credibility / reliability / confidence", null, null);
        MTurkHITContainer.QuestionOption o8_6 = new MTurkHITContainer.QuestionOption("o8_6",
                "Explanation is highly topic-specific and addresses the content of Argument X in detail",
                "'Argument X addresses how the culture was improved after the revolution', 'Argument X makes very good points about the bacteria that can be found in bottles after being re-used so much', 'Argument X introduces the element of crime'",
                null);
        q8.options.add(o8_1);
        //        q8.options.add(o8_2);
        //        q8.options.add(o8_3);
        q8.options.add(o8_4);
        q8.options.add(o8_5);
        q8.options.add(o8_6);

        // ninth question - positive
        MTurkHITContainer.AnnotationQuestion q9 = new MTurkHITContainer.AnnotationQuestion("q9",
                ASPECTS_STRING, null);
        MTurkHITContainer.QuestionOption o9_1 = new MTurkHITContainer.QuestionOption("o9_1",
                "Argument X is clear, crisp, to the point / well written", null, null);
        MTurkHITContainer.QuestionOption o9_2 = new MTurkHITContainer.QuestionOption("o9_2",
                "Argument X sticks to the topic", null, null);
        MTurkHITContainer.QuestionOption o9_3 = new MTurkHITContainer.QuestionOption("o9_3",
                "Argument X has provoking question / makes you think",
                "'Argument X has a good rhetorical question', 'Argument X brings up moral aspects', 'Argument X discusses some consequences",
                null);
        MTurkHITContainer.QuestionOption o9_4 = new MTurkHITContainer.QuestionOption("o9_4",
                "Argument X is well thought of / has smart remarks / higher complexity", null,
                null);

        q9.options.add(o9_1);
        q9.options.add(o9_2);
        q9.options.add(o9_3);
        q9.options.add(o9_4);

        result.add(q1);
        result.add(q2);
        result.add(q3);
        result.add(q4);
        result.add(q5);
        result.add(q6);
        result.add(q7);
        result.add(q8);
        result.add(q9);

        return result;
    }

    /**
     * Returns a map (option ID, QuestionOption)
     *
     * @param finalOnly only if "final" options are required ("terminal" questions)
     * @return map
     */
    public static Map<String, MTurkHITContainer.QuestionOption> listAllOptions(boolean finalOnly)
    {
        Map<String, MTurkHITContainer.QuestionOption> result = new TreeMap<>();

        for (MTurkHITContainer.AnnotationQuestion question : prepareAnnotationQuestions()) {
            for (MTurkHITContainer.QuestionOption option : question.options) {

                // do we need to filter them?
                if (finalOnly) {
                    if (option.targetQuestionId == null) {
                        result.put(option.optionId, option);
                    }
                }
                else {
                    result.put(option.optionId, option);
                }
            }
        }

        return result;
    }

    public static void questionsToGraphviz()
            throws IOException
    {

        PrintWriter pw = new PrintWriter(new FileWriter("/tmp/questions.dot"));

        pw.println("digraph graphname {");
        pw.println("rankdir=LR;");

        for (MTurkHITContainer.AnnotationQuestion question : prepareAnnotationQuestions()) {
            String text1 = question.question;
            pw.printf(Locale.ENGLISH, "%s [label=\"%s\"];%n", question.questionId, text1);

            for (MTurkHITContainer.QuestionOption option : question.options) {

                String targetQuestionId = option.targetQuestionId;
                if (targetQuestionId == null) {
                    targetQuestionId = "" + option.optionId;
                }

                pw.printf(Locale.ENGLISH, "%s -> %s [label=\"%s\"];%n", question.questionId,
                        targetQuestionId, option.answer);
            }
        }
        pw.println("{ rank=same; q1 q2 }");
        pw.println("}");

        pw.close();
    }

    public static void prepareBatchFromTo(File inputDir, File outputDir, boolean useSandbox, int from, int to)
            throws IOException
    {
        Step7HITCreator hitCreator = new Step7HITCreator(useSandbox);
        hitCreator.outputPath = outputDir;
        hitCreator.initialize();

        // now the second pass over the data
        List<File> allFiles = new ArrayList<>(IOHelper.listXmlFiles(inputDir));

        System.out.println("All files size: " + allFiles.size());

        // container for all reason units
        List<ReasonUnit> allReasonUnits = new ArrayList<>();

        for (File file : allFiles) {
            List<ReasonUnit> allReasonUnitsPerTopic = new ArrayList<>();

            // collect all reason units from this topic
            List argumentPairs = (List) XStreamTools.getXStream().fromXML(file);
            for (Object o : argumentPairs) {
                AnnotatedArgumentPair argumentPair = (AnnotatedArgumentPair) o;
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        if (!reasonUnit.isIgnored() && !reasonUnit.isDuplicate()) {
                            allReasonUnitsPerTopic.add(reasonUnit);
                        }
                    }
                }
            }

            //            System.out.println(allReasonUnitsPerTopic.subList(0, 10));
            // now sort them according to turker competence; descending
            Collections.sort(allReasonUnitsPerTopic, new Comparator<ReasonUnit>()
            {
                @Override
                public int compare(ReasonUnit o1, ReasonUnit o2)
                {
                    return o2.getAverageCompetenceOfOriginalWorkers()
                            .compareTo(o1.getAverageCompetenceOfOriginalWorkers());
                }
            });
            //            System.out.println(allReasonUnitsPerTopic.subList(0, 10));

            // and select the first 320 reason units
            List<ReasonUnit> first320reasonUnitsPerTopic = allReasonUnitsPerTopic.subList(from, to);

            // add them to the final one
            allReasonUnits.addAll(first320reasonUnitsPerTopic);
        }

        // shuffle them
        Random random = new Random(0);
        Collections.shuffle(allReasonUnits, random);

        for (ReasonUnit reasonUnit : allReasonUnits) {
            hitCreator.process(reasonUnit);
        }

        hitCreator.collectionProcessComplete();
    }

    public static void prepareFirstBatch(File inputDir, File outputDir, boolean useSandbox) throws IOException {
        prepareBatchFromTo(inputDir, outputDir, useSandbox, 0, 320);
    }

    public static void prepareSecondBatch(File inputDir, File outputDir, boolean useSandbox) throws IOException {
        prepareBatchFromTo(inputDir, outputDir, useSandbox, 320, 320 + 500);
    }

    public static void main2(String[] args)
            throws IOException
    {
        questionsToGraphviz();
        System.out.println(listAllOptions(true));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws IOException
    {
//        questionsToGraphviz();

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        // sandbox or real MTurk?
        final boolean useSandbox = false;

        // required only for pilot
        // final int pilotDataSize = 80;
        // pseudo-random generator
        // this was first devel
        // final Random random = new Random(2);
        // second devel set; first pilot
        // final Random random = new Random(1);
        // second devel set; second pilot
        // final Random random = new Random(3);

//        prepareFirstBatch(inputDir, outputDir, useSandbox);
        prepareSecondBatch(inputDir, outputDir, useSandbox);
    }
}
