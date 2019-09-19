package cz.mendelu.tomas.graphpef.helperObjects;

import android.provider.BaseColumns;

public class QuizContract {

    private QuizContract() {}

    public static class QuestionsTable implements BaseColumns {
        public static final String QUIZ_TABLE_NAME = "graph_quiz_questions";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_OPTION1 = "option1";
        public static final String COLUMN_OPTION2 = "option2";
        public static final String COLUMN_OPTION3 = "option3";
        public static final String COLUMN_OPTION4 = "option4";
        public static final String COLUMN_ANSWER_ID = "answer_id";
        public static final String COLUMN_CATEGORY = "category";
    }

    public static class CategoryTable implements BaseColumns {
        public static final String CATEGORY_TABLE_NAME = "graph_quiz_questions";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_UNLOCKED = "unlocked";
    }

    public static class QuizAnswerTable implements BaseColumns {
        public static final String CATEGORY_TABLE_NAME = "graph_quiz_questions";
        public static final String COLUMN_TIMETAG = "time_of_answer";
        public static final String COLUMN_POINTS_ACQUIRED = "points_acwuired";
        public static final String COLUMN_QUESTIONS_ANSWERED = "questions_answered";
    }


}
