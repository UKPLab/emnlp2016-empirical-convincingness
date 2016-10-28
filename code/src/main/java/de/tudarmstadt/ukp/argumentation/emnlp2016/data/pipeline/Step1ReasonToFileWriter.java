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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline.io.ReasonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author Ivan Habernal
 */
public class Step1ReasonToFileWriter
        extends JCasAnnotator_ImplBase
{
    public static final String PARAM_OUTPUT_FILE = "outputFile";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
    File outputFile;

    private PrintWriter pw;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            pw = new PrintWriter(outputFile, "utf-8");
        }
        catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {
        pw.print(DocumentMetaData.get(aJCas).getDocumentId());
        pw.print("\t");
        pw.println(aJCas.getDocumentText());
    }

    @Override
    public void collectionProcessComplete()
            throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        pw.close();
    }

    public static void main(String[] args)
            throws Exception
    {
        SimplePipeline.runPipeline(CollectionReaderFactory
                        .createReaderDescription(ReasonReader.class, ReasonReader.PARAM_SOURCE_FOLDER,
                                args[0]),
                AnalysisEngineFactory.createEngineDescription(Step1ReasonToFileWriter.class,
                        Step1ReasonToFileWriter.PARAM_OUTPUT_FILE, args[1])
        );
    }
}
