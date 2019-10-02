package cz.mendelu.tomas.graphpef.database;

import android.provider.BaseColumns;

class DBContractNames {

    private DBContractNames() {
    }

    static final String DATABASE_NAME = "GraphPefDatabase.db";

    static class CategoryTable implements BaseColumns {
        static final String CATEGORY_TABLE_NAME = "quiz_categories";
        static final String COLUMN_CATEGORY = "category";
        static final String COLUMN_UNLOCKED = "unlocked";
        static final String COLUMN_FIRESTORE_ID = "firestore_id";
    }

    static final int DATABASE_VERSION = 12;
    //FirestoreTags

    static final String COLLECTION_NAME_QUIZ_QUESTIONS = "quizQuestions";
    static final String COLLECTION_NAME_USERS = "users";
    static final String COLLECTION_NAME_QUIZ_CATEGORIES = "categories";
    static final String COLLECTION_NAME_DATABASE_VERSION = "databaseVersion";
    static final String DOCUMENT_NAME_DATABASE_VERSION = "version";
    static final String COLLECTION_NAME_ANSWERED_QUESTIONS = "answeredQuestions";

    //quizQuestions
    static final String FIRESTORE_QUESTION_TEXT = "question";
    static final String FIRESTORE_QUESTION_ANSWER1 = "answer1";
    static final String FIRESTORE_QUESTION_ANSWER2 = "answer2";
    static final String FIRESTORE_QUESTION_ANSWER3 = "answer3";
    static final String FIRESTORE_QUESTION_ANSWER4 = "answer4";
    static final String FIRESTORE_QUESTION_CATEGORY = "category";
    static final String FIRESTORE_QUESTION_CATEGORY_ID = "category_ID";
    static final String FIRESTORE_QUESTION_CORRECT_ANSWER = "correctAnswer";
    static final String FIRESTORE_QUESTION_SUBJECT = "subject";
    //categories
    static final String FIRESTORE_CATEGORIES_TITLE = "Title";
    static final String FIRESTORE_CATEGORIES_UNLOCKED = "unlocked";
    //Users
    static final String FIRESTORE_USER_ID = "auth_id";
    static final String FIRESTORE_USER_MAIL = "email";
    static final String FIRESTORE_USER_POINTS = "points";
    static final String FIRESTORE_USER_POINTS_STREAK = "high_points_streak";
    static final String FIRESTORE_USER_ANSWERS_STREAK = "high_answer_streak";
    //answeredQuestion
    static final String FIRESTORE_ANSWERED_QUESTIONS_QUESTION_ID = "question_id";
    static final String FIRESTORE_ANSWERED_QUESTIONS_USER_ID = "user_id";
    static final String FIRESTORE_ANSWERED_QUESTIONS_TIMETAG = "time_tag";

    //end Firestore tags


    static class QuestionsTable implements BaseColumns {

        static final String QUIZ_TABLE_NAME = "quiz_quesions";
        static final String COLUMN_QUESTION = "question";
        static final String COLUMN_OPTION1 = "option1";
        static final String COLUMN_OPTION2 = "option2";
        static final String COLUMN_OPTION3 = "option3";
        static final String COLUMN_OPTION4 = "option4";
        static final String COLUMN_ANSWER_ID = "answer_id";
        static final String COLUMN_CATEGORY = "category";
        static final String COLUMN_CATEGORY_ID = "categoryId";
        static final String COLUMN_ANSWERED = "answered";
        static final String COLUMN_SUBJECT = "subject";
        static final String COLUMN_FIRESTORE_ID = "firestore_id";

    }

    static class QuizAnswerTable implements BaseColumns {
        static final String ANSWERS_TABLE_NAME = "quiz_answered_data";
        static final String COLUMN_TIMETAG = "time_of_answer";
        static final String COLUMN_POINTS_ACQUIRED = "points_acquired";
        static final String COLUMN_QUESTIONS_ANSWERED = "questions_answered";
    }


}
