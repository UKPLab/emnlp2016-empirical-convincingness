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

package de.tudarmstadt.ukp.argumentation.emnlp2016.data.annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class MTurkHITContainer
{
    // main container
    public List<HITReasonUnit> reasonUnitList = new ArrayList<>();

    // number of arguments on page
    public int numberOfReasonUnits;

    // mturk URL for form action (sandbox, actual one)
    public String mturkURL;

    public static class HITReasonUnit
    {
        public String reasonId;
        public String text;

        // questions
        public List<AnnotationQuestion> annotationQuestions;
    }

    /**
     * @author Ivan Habernal
     */
    public static class AnnotationQuestion
    {
        public AnnotationQuestion(String questionId, String question, String additionalInformation)
        {
            this.questionId = questionId;
            this.question = question;
            this.additionalInformation = additionalInformation;
        }

        final public String questionId;
        final public String question;
        final public String additionalInformation;

        public boolean firstQuestion = false;

        final public List<QuestionOption> options = new ArrayList<>();
    }

    public static class QuestionOption
    {
        public QuestionOption(String optionId, String answer, String additionalInformation,
                String targetQuestionId)
        {
            this.optionId = optionId;
            this.answer = answer;
            this.additionalInformation = additionalInformation;
            this.targetQuestionId = targetQuestionId;
        }

        public String optionId;
        public String answer;
        public String additionalInformation;
        public String targetQuestionId;
    }
}
