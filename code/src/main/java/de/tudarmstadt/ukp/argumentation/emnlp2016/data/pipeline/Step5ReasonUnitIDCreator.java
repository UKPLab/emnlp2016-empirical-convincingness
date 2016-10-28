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

import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.AnnotatedArgumentPair;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.IOHelper;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkAssignment;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.XStreamTools;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class Step5ReasonUnitIDCreator

{
    public static void extractAllReasons(File inputDir, File outputDir)
            throws Exception
    {
        Collection<File> files = IOHelper.listXmlFiles(inputDir);

        int counter = 0;

        for (File file : files) {
            List argumentPairs = (List) XStreamTools.getXStream().fromXML(file);
            for (Object o : argumentPairs) {
                AnnotatedArgumentPair argumentPair = (AnnotatedArgumentPair) o;
                // and now the reasons
                for (MTurkAssignment assignment : argumentPair.getMTurkAssignments()) {
                    MTurkAssignmentWithReasonUnits a = (MTurkAssignmentWithReasonUnits) assignment;

                    for (int i = 0; i < a.getReasonUnits().size(); i++) {
                        ReasonUnit reasonUnit = a.getReasonUnits().get(i);

                        reasonUnit.setId(String.valueOf(counter));

                        counter++;
                    }
                }
            }

            File outputFile = new File(outputDir, file.getName());
            XStreamTools.toXML(argumentPairs, outputFile);
        }
    }

    public static void main(String[] args)
            throws Exception
    {

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        extractAllReasons(inputDir, outputDir);
    }
}
