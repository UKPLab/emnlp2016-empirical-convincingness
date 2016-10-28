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

package de.tudarmstadt.ukp.argumentation.emnlp2016.experiments.svmlib;

import de.tudarmstadt.ukp.argumentation.emnlp2016.experiments.AdHocFeature;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class LIBSVMFileProducer
        extends JCasAnnotator_ImplBase
{

    public static final String PARAM_FEATURES_TO_INT_MAPPING = "mappingFile";
    @ConfigurationParameter(name = PARAM_FEATURES_TO_INT_MAPPING, mandatory = true)
    protected File mappingFile;

    protected SortedMap<String, Integer> featuresToIntMapping;

    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
    protected File outputFile;

    protected PrintWriter printWriter;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            outputFile.getParentFile().mkdirs();

            printWriter = new PrintWriter(outputFile, "utf-8");

            featuresToIntMapping = (SortedMap<String, Integer>) new ObjectInputStream(
                    new FileInputStream(mappingFile)).readObject();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        printWriter.close();
    }

    static final Set<String> lessConvincing = new TreeSet<String>(Arrays.asList(
            "o5_1", "o5_2", "o5_3", "o6_1", "o6_2", "o6_3", "o7_1", "o7_2", "o7_3", "o7_4"
    ));

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        // get the label first
        DocumentMetaData metaData = DocumentMetaData.get(aJCas);
        System.out.println(metaData.getDocumentId());
        String[] split = metaData.getDocumentId().split("_", 5);
        //        String labels = split[split.length - 1];
        String labels = split[split.length - 1];


        int lessConvincingReasonsCount = 0;

        List<String> splitLabels = Arrays.asList(labels.split(","));
        for (String s : splitLabels) {
            if (lessConvincing.contains(s)) {
                lessConvincingReasonsCount++;
            }
        }

        String label = null;
        if (lessConvincingReasonsCount == 1) {
            for (String s : splitLabels) {
                if (lessConvincing.contains(s)) {
                    label = s.split("_")[0];
                }
            }
        }

        System.out.println(Arrays.toString(split));
        System.out.println(label);

        if ("o5".equals(label)) {
            label = "0";
        }
        else if ("o6".equals(label)) {
            label = "1";
        }
        else if ("o7".equals(label)) {
            label = "2";
        }
        else {
            label = null;
        }

        if (label != null) {
            SortedMap<Integer, Double> featureValues = new TreeMap<Integer, Double>();

            Collection<AdHocFeature> adHocFeatures = JCasUtil.select(aJCas, AdHocFeature.class);
            //        System.out.println("adHocFeatures: " + adHocFeatures.size());
            for (AdHocFeature feature : adHocFeatures) {
                Integer key = featuresToIntMapping.get(feature.getName());
                Double value = feature.getValue();

                // only non-zero features
                if (value > 0.001) {
                    featureValues.put(key, value);
                }
            }

            //        System.out.println("Mapped instance features: " + featureValues.size());

            // now print it!
            printWriter.printf(Locale.ENGLISH, "%s\t", label);

            for (Map.Entry<Integer, Double> featureValue : featureValues.entrySet()) {
                printWriter.printf(Locale.ENGLISH, "%d:%.3f\t", featureValue.getKey(),
                        featureValue.getValue());
            }

            // add pair ID as comment
            printWriter.println("#" + metaData.getDocumentId());
        }
    }
}
