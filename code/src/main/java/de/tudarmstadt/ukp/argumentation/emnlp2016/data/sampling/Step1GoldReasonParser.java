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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class Step1GoldReasonParser
{

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
            throws Exception
    {
        String inputDir = args[0];

        Collection<File> files = IOHelper.listXmlFiles(new File(inputDir));

        // for generating ConvArgStrict use this
        String prefix = "no-eq_DescendingScoreArgumentPairListSorter";

        List<String> allReasons = new ArrayList<>();

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            if (!file.getName().startsWith(prefix)) {
                iterator.remove();
            }
        }

        AnalysisEngineDescription pipeline = AnalysisEngineFactory.createEngineDescription(
                AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class),
                AnalysisEngineFactory.createEngineDescription(StanfordParser.class));
//                AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class))

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);
            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                // get gold reason statistics
                for (MTurkAssignment assignment : argumentPair.mTurkAssignments) {
                    String reason = assignment.getReason();

                    //                    allReasons.add(reason);

                    JCas jCas = JCasFactory.createJCas();
                    jCas.setDocumentLanguage("en");
                    jCas.setDocumentText(reason);

                    SimplePipeline.runPipeline(jCas, pipeline);

                    Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);

                    System.out.println(sentences.size());

                }
            }
        }

    }
}
