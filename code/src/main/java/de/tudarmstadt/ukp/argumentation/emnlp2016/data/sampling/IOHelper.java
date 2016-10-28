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

import org.apache.commons.io.FileUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.createdebate.DebateMetaData;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ivan Habernal
 */
public class IOHelper
{
    public static String createFileName(DebateMetaData metaData, String stance)
    {
        return metaData.getTitle().toLowerCase().replaceAll("\\W+", "-") + "_" + stance
                .toLowerCase().replaceAll("\\W+", "-") + ".xml";
    }

    /**
     * Lists all xml files and throw an IOException if no XML files were found
     *
     * @param inputDir input dir
     * @return list of xml files
     * @throws IOException exception
     */
    public static List<File> listXmlFiles(File inputDir)
            throws IOException
    {
        ArrayList<File> files = new ArrayList<>(
                FileUtils.listFiles(inputDir, new String[] { "xml" }, false));

        if (files.isEmpty()) {
            throw new IOException("No xml files found in " + inputDir);
        }

        return files;
    }

    /**
     * Sorts map by value (http://stackoverflow.com/a/2581754)
     *
     * @param map map
     * @param <K> key
     * @param <V> value
     * @return sorted map by value
     */
    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(
            Map<K, V> map, final boolean asc)
    {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            @Override public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                if (asc) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
                else {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            }
        });

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Loads argument pairs
     *
     * @param inputDir input dir with XMLs
     * @return map (file name, list of argument pairs)
     * @throws IOException exception
     */
    @SuppressWarnings("unchecked") public static Map<String, List<AnnotatedArgumentPair>> loadAnnotatedPais(
            File inputDir)
            throws IOException
    {
        Map<String, List<AnnotatedArgumentPair>> result = new TreeMap<>();

        for (File file : listXmlFiles(inputDir)) {
            // collect all reason units from this topic
            List<AnnotatedArgumentPair> argumentPairs = (List<AnnotatedArgumentPair>) XStreamTools
                    .getXStream().fromXML(file);

            result.put(file.getName(), argumentPairs);
        }

        return result;
    }

    /**
     * Saves pairs to file in the output dir
     *
     * @param data      map (file name, list (pairs))
     * @param outputDir output dir
     * @throws IOException exception
     */
    public static void saveAnnotatedPairs(Map<String, List<AnnotatedArgumentPair>> data,
            File outputDir)
            throws IOException
    {
        for (Map.Entry<String, List<AnnotatedArgumentPair>> entry : data.entrySet()) {
            String fileName = entry.getKey();
            // save the file
            File outputFile = new File(outputDir, fileName);
            XStreamTools.toXML(entry.getValue(), outputFile);
        }
    }
}
