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

import org.apache.commons.lang3.RandomStringUtils;
import de.tudarmstadt.ukp.argumentation.emnlp2016.data.sampling.MTurkReasonUnitAssignment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Randomly (equal distribution) returns a gold label
 * <p/>
 * @author Ivan Habernal
 */
public class MockGoldLabelProvider
        implements GoldLabelProvider
{
    private final Random random = new Random(1234);

    public static final List<String> OPTIONS = new ArrayList<>(
            Step7HITCreator.listAllOptions(true).keySet());

    @Override
    public String provideGoldLabel(int reasonUnitId)
    {
        // only give sample with 50% probability
        if (random.nextBoolean()) {
            return OPTIONS.get(random.nextInt(OPTIONS.size()));
        }

        return null;
    }

    @Override
    public List<MTurkReasonUnitAssignment> getMTurkReasonUnitAssignments(int reasonUnitId)
    {
        List<MTurkReasonUnitAssignment> result = new ArrayList<>();

        // make random five
        for (int i = 0; i < 5; i++) {

            String worker = "R" + RandomStringUtils.randomAlphanumeric(13);
            String hit = "H" + RandomStringUtils.randomAlphanumeric(29);
            String a = "A" + RandomStringUtils.randomAlphanumeric(29);

            Date date1 = new Date(
                    -946771200000L + (Math.abs(random.nextLong()) % (70L * 365 * 24 * 60 * 60
                            * 1000)));
            Date date2 = new Date(
                    -946771200000L + (Math.abs(random.nextLong()) % (70L * 365 * 24 * 60 * 60
                            * 1000)));
            String label = OPTIONS.get(random.nextInt(OPTIONS.size()));

            MTurkReasonUnitAssignment unitAssignment = new MTurkReasonUnitAssignment(worker, hit, a,
                    date1, date2, label);

            result.add(unitAssignment);
        }

        return result;
    }
}
