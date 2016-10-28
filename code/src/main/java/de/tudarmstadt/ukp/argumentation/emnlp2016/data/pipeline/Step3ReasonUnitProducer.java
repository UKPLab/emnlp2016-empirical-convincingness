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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline.io.ReasonReader;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.NSUBJ;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class Step3ReasonUnitProducer
{
    /**
     * Cleaning and normalizing a single EDU from the parsed reason
     *
     * @param edu edu
     * @return "clean" edu
     */
    public static String normalizeSingleEDU(String edu)
    {
        String result = edu.toLowerCase();

        // replace miss-spelling
        result = result.replaceAll("arguement", "argument");
        result = result.replaceAll("arugument", "argument");
        result = result.replaceAll("arguemnt", "argument");
        result = result.replaceAll("arguemt", "argument");
        result = result.replaceAll("agument", "argument");
        result = result.replaceAll("arument", "argument");
        result = result.replaceAll("arugment", "argument");
        result = result.replaceAll("arhument", "argument");
        result = result.replaceAll("argumentn", "argument");
        result = result.replaceAll("atgument", "argument");
        result = result.replaceAll("aegument", "argument");
        result = result.replaceAll("where as", "whereas");

        // normalize doesn't
        result = result.replaceAll("does n't", "does not");

        // normalize isn't
        result = result.replaceAll("is n't", "is not");

        // brackets - we don't care
        result = result.replaceAll("-lrb-", "(");
        result = result.replaceAll("-rrb-", ")");
        result = result.replaceAll("-lcb-", "(");
        result = result.replaceAll("-rcb-", ")");
        result = result.replaceAll("-lsb-", "(");
        result = result.replaceAll("-rsb-", ")");

        // typoS
        result = result.replaceAll("a !", "a1");
        result = result.replaceAll("b2", "a2");
        result = result.replaceAll("a @", "a2");

        // missing whitespace
        result = result.replaceAll("a1s", "a1 's");
        result = result.replaceAll("a2s", "a2 's");

        // missing a
        result = result.replaceAll("^1", "a1");
        result = result.replaceAll("^2", "a2");

        // replace AX's argument -> AX
        result = result.replaceAll("a1 's argument", "a1");
        result = result.replaceAll("a2 's argument", "a2");

        // replace "argument X"
        result = result.replaceAll("argument 1", "a1");
        result = result.replaceAll("argument 2", "a2");

        // 3 -> probably 2
        result = result.replaceAll("a3", "a2");

        // argument 1 / 2
        result = result.replaceAll("argument one", "a1");
        result = result.replaceAll("argument two", "a2");

        // replace "argument aX"
        result = result.replaceAll("argument a1", "a1");
        result = result.replaceAll("argument a2", "a2");

        // with "the first one/the first argument" ?
        result = result.replaceAll("first argument", "a1");
        result = result.replaceAll("second argument", "a2");
        result = result.replaceAll("first one", "a1");
        result = result.replaceAll("second one", "a1");

        result = result.replaceAll("the first", "a1");
        result = result.replaceAll("the second", "a1");

        // side
        result = result.replaceAll("side 1", "a1");
        result = result.replaceAll("side 2", "a2");

        // speaker
        result = result.replaceAll("speaker 1", "a1");
        result = result.replaceAll("speaker one", "a1");
        result = result.replaceAll("speaker 2", "a2");
        result = result.replaceAll("speaker two", "a2");

        // person
        result = result.replaceAll("person 2", "a2");
        result = result.replaceAll("person 1", "a1");

        // answer
        result = result.replaceAll("answer 1", "a1");
        result = result.replaceAll("answer 2", "a2");

        // number
        result = result.replaceAll("number 1", "a1");
        result = result.replaceAll("number one", "a1");
        result = result.replaceAll("number 2", "a2");
        result = result.replaceAll("number two", "a2");
        result = result.replaceAll("# 1", "a1");
        result = result.replaceAll("# 2", "a2");

        // article
        result = result.replaceAll("article 1", "a1");
        result = result.replaceAll("article 2", "a2");

        // remove definite article
        result = result.replaceAll("the a1", "a1");
        result = result.replaceAll("the a2", "a2");

        // whitespace
        result = result.replaceAll("a 1", "a1");
        result = result.replaceAll("a 2", "a2");

        // possessives
        result = result.replaceAll("a1 's argument", "a1");
        result = result.replaceAll("a2 's argument", "a2");

        // remove trailing and final comma/colon
        result = result.replaceAll("[\\.,;]$", "").trim();

        result = result.trim();

        // now remove simple comparative
        result = result.replaceAll("than a\\d does$", "").trim();
        result = result.replaceAll("than a\\d did$", "").trim();
        result = result.replaceAll("than a\\d provides$", "").trim();
        result = result.replaceAll("than does a\\d$", "").trim();
        result = result.replaceAll("than a\\d is$", "").trim();
        result = result.replaceAll("than a\\d here$", "").trim();
        result = result.replaceAll("than a\\d 's$", "").trim();
        result = result.replaceAll("than a\\d$", "").trim();
        result = result.replaceAll("than a\\d has$", "").trim();
        result = result.replaceAll("than in a\\d$", "").trim();
        result = result.replaceAll("as much as a\\d$", "").trim();
        result = result.replaceAll("as much as a\\d does$", "").trim();
        result = result.replaceAll("as well as a\\d$", "").trim();
        result = result.replaceAll("as does a\\d$", "").trim();
        result = result.replaceAll("as a\\d$", "").trim();
        result = result.replaceAll("than that of a\\d$", "").trim();
        result = result.replaceAll("like a\\d$", "").trim();
        result = result.replaceAll("like a\\d does$", "").trim();
        result = result.replaceAll("like a\\d is$", "").trim();
        result = result.replaceAll("then a\\d$", "").trim();
        result = result.replaceAll("compared to a\\d$", "").trim();
        result = result.replaceAll("compared to a\\d 's$", "").trim();
        result = result.replaceAll("unlike a\\d$", "").trim();
        result = result.replaceAll("as opposed to a\\d$", "").trim();
        result = result.replaceAll("over a\\d$", "").trim();

        if (result.contains("a1 a2")) {
            System.err.println(edu);
            System.err.println(result);
        }

        // no distinction between A1 and A2
        //        result = result.replaceAll("[aA][12]", "aX");

        return result.trim();
    }

    /**
     * Counts of references to particular argument (a1 or a2)
     *
     * @param eduList  list of edu
     * @param argument a1 or a2
     * @return count
     */
    public static int countArgumentReference(List<String> eduList, String argument)
    {
        return StringUtils.countMatches(StringUtils.join(eduList, " "), argument);
    }

    /**
     * Resolves target argument (a1 or a2) only if unambiguously mentioned; null otherwise.
     *
     * @param edu edu
     * @return a1, a2 or null
     */
    public static ReasonUnit.Target resolveTarget(String edu, AnalysisEngineDescription pipeline)
    {
        if (StringUtils.countMatches(edu, "a1") > 0 && StringUtils.countMatches(edu, "a2") == 0) {
            return ReasonUnit.Target.a1;
        }

        if (StringUtils.countMatches(edu, "a2") > 0 && StringUtils.countMatches(edu, "a1") == 0) {
            return ReasonUnit.Target.a2;
        }

        if (StringUtils.countMatches(edu, "a2") > 0 && StringUtils.countMatches(edu, "a1") > 0) {
            // parse
            try {
                JCas jCas = JCasFactory.createJCas();
                jCas.setDocumentLanguage("en");
                jCas.setDocumentText(edu);

                SimplePipeline.runPipeline(jCas, pipeline);

                Collection<NSUBJ> nsubjCollection = JCasUtil.select(jCas, NSUBJ.class);

                for (NSUBJ n : nsubjCollection) {
                    System.out.println("nsubjs: " + n.getCoveredText());
                }

                if (nsubjCollection.size() == 1) {
                    String nsubj = nsubjCollection.iterator().next().getCoveredText();

                    if ("a1".equals(nsubj)) {
                        return ReasonUnit.Target.a1;
                    }

                    if ("a2".equals(nsubj)) {
                        return ReasonUnit.Target.a2;
                    }
                }

            }
            catch (UIMAException e) {
                // ignore
            }

        }

        return null;
    }

    /**
     * Splits the given edu list into several ReasonUnit instances
     *
     * @param eduList list of edu for a single argument pair
     * @return list of reason units (might be empty but never {@code null})
     */
    public static List<ReasonUnit> splitReasonInReasonUnit(List<String> eduList,
            AnalysisEngineDescription pipeline)
    {
        // first normalize and clean all independently
        List<String> cleanList = new ArrayList<>();
        for (String s : eduList) {
            String normalizedEDU = normalizeSingleEDU(s);

            List<String> splitEduList = splitNormalizedEDU(normalizedEDU);

            // and normalized again
            List<String> splitNormalizedEduList = new ArrayList<>();
            for (String splitEdu : splitEduList) {
                String normalizedSingleEDU = normalizeSingleEDU(splitEdu);

                // omit empty edu
                if (!normalizedSingleEDU.isEmpty()) {
                    splitNormalizedEduList.add(normalizedSingleEDU);
                }
            }

            cleanList.addAll(splitNormalizedEduList);
        }

        // let's find non-referencing reasons
        int referenceCounts =
                countArgumentReference(cleanList, "a1") + countArgumentReference(cleanList, "a2");

        // single reason unit but no reference starting with "there is" -> fix
        if (referenceCounts == 0) {
            if (cleanList.get(0).startsWith("there is ") || cleanList.get(0)
                    .startsWith("there 's ")) {
                cleanList.set(0, "in a1 " + cleanList.get(0));
            }
        }

        // let's find non-referencing reasons
        referenceCounts =
                countArgumentReference(cleanList, "a1") + countArgumentReference(cleanList, "a2");

        // if there are no references, we do not resolve reason units
        if (referenceCounts == 0) {
            return new ArrayList<>();
        }

        // we don't do any merging if only one EDU is present
        if (cleanList.size() == 1) {
            String edu = cleanList.get(0);
            ReasonUnit.Target target = resolveTarget(edu, pipeline);

            // if no target, return empty list
            if (target == null) {
                return new ArrayList<>();
            }

            ReasonUnit reasonUnit = new ReasonUnit();
            reasonUnit.setReasonUnitText(edu);
            reasonUnit.setTarget(target);

            return Collections.singletonList(reasonUnit);
        }

        // ok, now do the real job
        List<ReasonUnit> zeroPass = new ArrayList<>(cleanList.size());

        // first, localize mentions of a1/a2
        for (String edu : cleanList) {
            ReasonUnit.Target target = resolveTarget(edu, pipeline);

            ReasonUnit reasonUnit = new ReasonUnit();
            reasonUnit.setReasonUnitText(edu);

            if (target != null) {
                reasonUnit.setTarget(target);
            }

            zeroPass.add(reasonUnit);
        }

        // some units might have ambiguous targets, let's see if we have at least one with clear target
        boolean presenceOfNonNullTargets = false;
        for (ReasonUnit reasonUnit : zeroPass) {
            presenceOfNonNullTargets |= reasonUnit.getTarget() != null;
        }
        if (!presenceOfNonNullTargets) {
            return new ArrayList<>();
        }

        // merge left side first with nulls, such as [null, A1, A2, null]
        System.out.println("-----------");
        System.out.println("zeroPass:   " + zeroPass);

        ReasonUnit mergedLeft = new ReasonUnit();
        int positionOfFirstNonNull = 0;
        boolean firstNonNullFound = false;
        while (!firstNonNullFound) {
            ReasonUnit reasonUnit = zeroPass.get(positionOfFirstNonNull);

            // append text
            mergedLeft.appendText(reasonUnit.getReasonUnitText());

            if (reasonUnit.getTarget() != null) {
                firstNonNullFound = true;
                // update target
                mergedLeft.setTarget(reasonUnit.getTarget());
            }

            positionOfFirstNonNull++;
        }

        // now we have the first unit with target, so let's rearrange with the rest
        List<ReasonUnit> firstPass = new ArrayList<>();
        // add the first
        firstPass.add(mergedLeft);
        // and copy the rest
        firstPass.addAll(zeroPass.subList(positionOfFirstNonNull, zeroPass.size()));

        // show difference
        System.out.println("firstPass:  " + firstPass);

        Queue<ReasonUnit> queue = new ArrayDeque<>(firstPass);
        Stack<ReasonUnit> stack = new Stack<>();
        // add at the top of the stack the last unit
        stack.add(queue.poll());

        while (!queue.isEmpty()) {
            ReasonUnit currentUnit = queue.poll();

            // if no target, merge with the stack top
            if (currentUnit.getTarget() == null) {
                // now look at the discourse connective
                String reasonUnitText = currentUnit.getReasonUnitText();

                String firstWord = reasonUnitText.split("\\s+")[0];
                // if this is "and" connective, add new unit with the same target
                if ("and".equals(firstWord)) {
                    currentUnit.setTarget(stack.peek().getTarget());
                    // set trimmed text
                    currentUnit.setReasonUnitText(reasonUnitText);
                    stack.add(currentUnit);
                }
                else {
                    stack.peek().appendText(reasonUnitText);
                }
            }
            else {
                stack.add(currentUnit);
            }
        }

        // reamove leading connectives from each unit
        for (ReasonUnit unit : stack) {
            unit.removeOpeningConnective();
            unit.normalizeWhiteSpaces();
        }

        List<ReasonUnit> secondPass = new ArrayList<>(stack);

        // filter units without text
        Iterator<ReasonUnit> iterator = secondPass.iterator();
        while (iterator.hasNext()) {
            ReasonUnit unit = iterator.next();

            if (unit.getReasonUnitText().isEmpty()) {
                iterator.remove();
            }
        }

        System.out.println("secondPass: " + secondPass);

        return secondPass;
    }

    /**
     * Replaces first tokens that are co-reference (it, this argument, ...) with a1
     *
     * @param reason reason
     * @return normalized reason
     */

    private static String resolveCoreference(String reason)
    {
        if (reason.contains("a1 a2")) {
            throw new RuntimeException(reason);
        }

        // we need to trim here!
        String r = reason.toLowerCase().replaceAll("^because", "").trim();

        // remove crap
        r = r.toLowerCase().replaceAll("^again", "").trim();

        // resolve coreference
        for (String coRef : Arrays
                .asList("it", "this argument", "the argument", "this one", "this person",
                        "the person", "this", "their argument")) {
            if (r.startsWith(coRef)) {
                r = r.replaceAll("^" + coRef, "a1").trim();

                r = r.replaceAll("a1 a2", "a2");
                r = r.replaceAll("a1 a1", "a1");

                return r;
            }
        }

        // this is actually a2
        for (String coRef : Collections.singletonList("the other argument")) {
            if (r.startsWith(coRef + " ")) {
                return r.replaceAll("^" + coRef, "a2").trim();
            }
        }

        // ok, some explanations do start with only verb, so let's assume if the first
        // word ends with *s, it's a verb and pre-append a1
        // but it cannot contain number
        String firstToken = r.split(" ")[0];
        if (firstToken.endsWith("s") && !firstToken.matches(".*\\d.*")) {
            r = "a1 " + r;
        }

        return r.trim();
    }

    private static List<String> splitNormalizedEDU(String edu)
    {
        // is it a sentence with comma?
        int i = edu.indexOf(" , ");

        if (i < 0) {
            i = edu.indexOf(" ; ");
        }

        if (i < 0) {
            i = edu.indexOf(" and a");
        }
        if (i < 0) {
            i = edu.indexOf(" but a");
        }
        if (i < 0) {
            i = edu.indexOf(" while a");
        }
        if (i < 0) {
            i = edu.indexOf(" because a");
        }
        if (i < 0) {
            i = edu.indexOf(" whereas a");
        }
        if (i < 0) {
            i = edu.indexOf(" whilst a");
        }

        if (i < 0) {
            return Collections.singletonList(edu);
        }

        List<String> result = new ArrayList<>();
        if (i > 0) {
            result.add(edu.substring(0, i).replaceAll(",", "").trim());
            result.add(edu.substring(i + 1, edu.length()).replaceAll(",", "").trim());
        }

        return result;
    }

    public static LinkedHashMap<String, List<ReasonUnit>> preprocessReasonUnits(
            InputStream inputStream)
            throws Exception
    {
        LineIterator iterator = IOUtils.lineIterator(inputStream, "utf-8");
        AnalysisEngineDescription pipeline = AnalysisEngineFactory.createEngineDescription(
                AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(StanfordParser.class));

        LinkedHashMap<String, List<ReasonUnit>> result = new LinkedHashMap<>();

        while (iterator.hasNext()) {
            String line = iterator.next();

            // first entry is id
            String[] split = line.split("\t");
            String id = split[0];

            List<String> edu = new ArrayList<>();

            // we have to resolve co-ref in the first edu
            String resolveCoreference = resolveCoreference(split[1]);

            if (resolveCoreference.contains("a1 a2")) {
                System.err.println(split[1] + " -- " + resolveCoreference);
            }

            edu.add(resolveCoreference);

            // and add the rest (from index 2 on)
            edu.addAll(Arrays.asList(split).subList(2, split.length));

            List<ReasonUnit> reasonUnits = splitReasonInReasonUnit(edu, pipeline);

            if (reasonUnits.isEmpty()) {
                System.err.println(StringUtils.join(edu, "__"));
            }

            result.put(id, reasonUnits);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        // load and preprocess reasons; they have to be exported and splitted first
        // see Step1
        BZip2CompressorInputStream bz2in = new BZip2CompressorInputStream(
                new FileInputStream("reasons-edu.txt.bz2"));
        LinkedHashMap<String, List<ReasonUnit>> reasonUnits = preprocessReasonUnits(bz2in);

        // for generating ConvArgStrict use this
        //        String prefix = "no-eq_DescendingScoreArgumentPairListSorter";
        //        Iterator<File> iterator = files.iterator();
        //        while (iterator.hasNext()) {
        //            File file = iterator.next();
        //
        //            if (!file.getName().startsWith(prefix)) {
        //                iterator.remove();
        //            }
        //        }

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);
            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                // create new assignments by copying the old ones
                List<MTurkAssignment> copyAssignments = new ArrayList<>();
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits copy = new MTurkAssignmentWithReasonUnits(
                            assignment);
                    copyAssignments.add(copy);
                }
                // and set the new list
                argumentPair.setMTurkAssignments(copyAssignments);

                // and now add the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    if (!(assignment instanceof MTurkAssignmentWithReasonUnits)) {
                        throw new IllegalStateException();
                    }

                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    // get the id
                    String uniqueID = ReasonReader.extractUniqueID(argumentPair, a);

                    // look-up in the prepared map
                    List<ReasonUnit> assignmentReasonUnits = reasonUnits.get(uniqueID);

                    // and add the reasons
                    if (assignmentReasonUnits != null) {
                        a.getReasonUnits().addAll(assignmentReasonUnits);
                    }
                }
            }

            File outputFile = new File(outputDir, file.getName());
            XStreamTools.toXML(argumentPairs, outputFile);
        }

        // save the to file
        //        XStreamTools.toXML(reasonUnits, new File("/tmp/reasons-preprocessed.xml"));
    }

}
