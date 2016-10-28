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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.pipeline.io;

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Reading reasons from ConvArgStrict data as UIMA reader
 * <p/>
 * @author Ivan Habernal
 */
public class ReasonReader
        extends JCasCollectionReader_ImplBase
{
    /**
     * Folder containing XML files with gold data and reasons
     */
    public static final String PARAM_SOURCE_FOLDER = "sourceFolder";
    @ConfigurationParameter(name = PARAM_SOURCE_FOLDER, mandatory = true)
    File sourceFolder;

    /**
     * File prefix
     * for generating ConvArgStrict use
     * no-eq_DescendingScoreArgumentPairListSorter
     */
    public static final String PARAM_FILE_PREFIX = "prefix";
    @ConfigurationParameter(name = PARAM_FILE_PREFIX, mandatory = false, defaultValue = "")
    String prefix;

    private LinkedHashMap<String, String> assignmentIDReasonMap = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        Collection<File> files;
        try {
            files = IOHelper.listXmlFiles(sourceFolder);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();

            if (!file.getName().startsWith(prefix)) {
                iterator.remove();
            }
        }

        for (File file : files) {
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);
            for (AnnotatedArgumentPair argumentPair : argumentPairs) {
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    String reason = assignment.getReason();

                    // assignment IDs are not unique
                    String id = extractUniqueID(argumentPair, assignment);

                    // only non-equal reasons
                    if (!"equal".equals(assignment.getValue())) {
                        // add ID and reason
                        assignmentIDReasonMap.put(id, reason);
                    }
                }
            }
        }
    }

    public static String extractUniqueID(AnnotatedArgumentPair argumentPair, MTurkAssignment assignment)
    {
        return argumentPair.getId() + "-" + assignment.getAssignmentId();
    }

    @Override
    public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        // poll head of the queue
        Iterator<Map.Entry<String, String>> iterator = assignmentIDReasonMap.entrySet().iterator();
        Map.Entry<String, String> entry = iterator.next();

        // remove the first item
        assignmentIDReasonMap.remove(entry.getKey());

        jCas.setDocumentLanguage("en");
        jCas.setDocumentText(entry.getValue());
        DocumentMetaData.create(jCas).setDocumentId(entry.getKey());
    }

    @Override
    public boolean hasNext()
            throws IOException, CollectionException
    {
        return !assignmentIDReasonMap.isEmpty();
    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[0];
    }

}
