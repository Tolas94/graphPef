package cz.mendelu.tomas.graphpef.helperObjects;

import android.provider.BaseColumns;

public class QuizContract {

    private QuizContract() {}

    public static final String DATABASE_NAME = "GraphPefDatabase.db";

    public static class CategoryTable implements BaseColumns {
        public static final String CATEGORY_TABLE_NAME = "quiz_categories";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_UNLOCKED = "unlocked";
        public static final String COLUMN_FIRESTORE_ID = "firestore_id";
    }

    public static final String COLLECTION_NAME_QUIZ_QUESTIONS = "quizQuestions";
    public static final String COLLECTION_NAME_USERS = "users";
    public static final String COLLECTION_NAME_QUIZ_CATEGORIES = "categories";
    public static final String COLLECTION_NAME_DATABASE_VERSION = "databaseVersion";
    public static final String DOCUMENT_NAME_DATABASE_VERSION = "version";
    public static final int DATABASE_VERSION = 12;
    //FirestoreTags
    //quizQuestions
    public static final String FIRESTORE_QUESTION_TEXT = "question";
    public static final String FIRESTORE_QUESTION_ANSWER1 = "answer1";
    public static final String FIRESTORE_QUESTION_ANSWER2 = "answer2";
    public static final String FIRESTORE_QUESTION_ANSWER3 = "answer3";
    public static final String FIRESTORE_QUESTION_ANSWER4 = "answer4";
    public static final String FIRESTORE_QUESTION_CATEGORY = "category";
    public static final String FIRESTORE_QUESTION_CATEGORY_ID = "category_ID";
    public static final String FIRESTORE_QUESTION_CORRECT_ANSWER = "correctAnswer";
    public static final String FIRESTORE_QUESTION_SUBJECT = "subject";
    //categories
    public static final String FIRESTORE_CATEGORIES_TITLE = "Title";
    public static final String FIRESTORE_CATEGORIES_UNLOCKED = "unlocked";
    //Users
    public static final String FIRESTORE_USER_ID = "auth_id";
    public static final String FIRESTORE_USER_MAIL = "email";
    public static final String FIRESTORE_USER_POINTS = "points";
    public static final String FIRESTORE_USER_POINTS_STREAK = "high_points_streak";
    public static final String FIRESTORE_USER_ANSWERS_STREAK = "high_answer_streak";
    //end Firestore tags


    public static class QuestionsTable implements BaseColumns {

        public static final String QUIZ_TABLE_NAME = "quiz_quesions";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_OPTION1 = "option1";
        public static final String COLUMN_OPTION2 = "option2";
        public static final String COLUMN_OPTION3 = "option3";
        public static final String COLUMN_OPTION4 = "option4";
        public static final String COLUMN_ANSWER_ID = "answer_id";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_CATEGORY_ID = "categoryId";
        public static final String COLUMN_ANSWERED = "answered";
        public static final String COLUMN_SUBJECT = "subject";
        public static final String COLUMN_FIRESTORE_ID = "firestore_id";

    }
    public static class QuizAnswerTable implements BaseColumns {
        public static final String ANSWERS_TABLE_NAME = "quiz_answered_data";
        public static final String COLUMN_TIMETAG = "time_of_answer";
        public static final String COLUMN_POINTS_ACQUIRED = "points_acquired";
        public static final String COLUMN_QUESTIONS_ANSWERED = "questions_answered";
    }


}
