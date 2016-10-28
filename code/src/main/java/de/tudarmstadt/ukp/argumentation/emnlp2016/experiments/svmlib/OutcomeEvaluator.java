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

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * (c) 2016 AUTHOR_HIDDEN
 */
public class OutcomeEvaluator
{
    public static void main(String[] args)
            throws IOException
    {
        File f = new File(args[0]);

        ConfusionMatrix cm = new ConfusionMatrix();

        LineIterator iterator = FileUtils.lineIterator(f);
        while (iterator.hasNext()) {
            String line = iterator.next();
            String[] split = line.split("\t");

            String id = split[0];
            String gold = split[1];
            String pred = split[2];

            cm.increaseValue(gold, pred);
        }

        System.out.println(cm);
        System.out.println(cm.printClassDistributionGold());
        System.out.println(cm.printLabelPrecRecFm());
        System.out.println(cm.printNiceResults());

//        random();
//        majority();
    }

    public static void random()
    {
        ConfusionMatrix cm = new ConfusionMatrix();

        Random r = new Random(0);

        for (int i = 0; i < 856; i++) {
            int nextInt = r.nextInt(3);
            cm.increaseValue(0 + "", nextInt + "");
        }

        for (int i = 0; i < 1203; i++) {
            int nextInt = r.nextInt(3);
            cm.increaseValue(1 + "", nextInt + "");
        }


        for (int i = 0; i < 1651; i++) {
            int nextInt = r.nextInt(3);
            cm.increaseValue(2 + "", nextInt + "");
        }



        System.out.println(cm);
        System.out.println(cm.printClassDistributionGold());
        System.out.println(cm.printLabelPrecRecFm());
        System.out.println(cm.printNiceResults());
    }

    public static void majority()
    {
        ConfusionMatrix cm = new ConfusionMatrix();

        Random r = new Random(0);

        for (int i = 0; i < 856; i++) {
            int nextInt = 3;
            cm.increaseValue(0 + "", nextInt + "");
        }

        for (int i = 0; i < 1203; i++) {
            int nextInt = r.nextInt(3);
            cm.increaseValue(1 + "", nextInt + "");
        }


        for (int i = 0; i < 1651; i++) {
            int nextInt = r.nextInt(3);
            cm.increaseValue(2 + "", nextInt + "");
        }



        System.out.println(cm);
        System.out.println(cm.printClassDistributionGold());
        System.out.println(cm.printLabelPrecRecFm());
        System.out.println(cm.printNiceResults());
    }
}
