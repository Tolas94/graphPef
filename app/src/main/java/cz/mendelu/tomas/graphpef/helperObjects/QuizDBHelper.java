package cz.mendelu.tomas.graphpef.helperObjects;

import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuizDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GraphPefDatabase.db";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    public QuizDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION4 + " TEXT, " +
                QuestionsTable.COLUMN_CATEGORY + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_ID + " INTEGER " +
                ")";
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        fillQuestionsTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }

    private void fillQuestionsTable() {
        QuizQuestion q1 = new QuizQuestion("question 1 - a"
                , "opt 1"
                , "opt 2"
                , "opt 3"
                , "opt 4"
                , "category"
                , 1);
        addQuestion(q1);

        QuizQuestion q2 = new QuizQuestion("question 2 - c"
                , "opt 1"
                , "opt 2"
                , "opt 3"
                , "opt 4"
                , "category"
                , 3);
        addQuestion(q2);

        QuizQuestion q3 = new QuizQuestion("question 3 - b"
                , "opt 1"
                , "opt 2"
                , "opt 3"
                , "opt 4"
                , "category"
                , 2);
        addQuestion(q3);

        QuizQuestion q4 = new QuizQuestion("question 4 - d"
                , "opt 1"
                , "opt 2"
                , "opt 3"
                , "opt 4"
                , "category"
                , 4);
        addQuestion(q4);
    }

    private void addQuestion(QuizQuestion quizQuestion) {
        ContentValues values = new ContentValues();
        values.put(QuestionsTable.COLUMN_QUESTION, quizQuestion.getQuestion());
        values.put(QuestionsTable.COLUMN_OPTION1, quizQuestion.getOption1());
        values.put(QuestionsTable.COLUMN_OPTION2, quizQuestion.getOption2());
        values.put(QuestionsTable.COLUMN_OPTION3, quizQuestion.getOption3());
        values.put(QuestionsTable.COLUMN_OPTION4, quizQuestion.getOption4());
        values.put(QuestionsTable.COLUMN_CATEGORY, quizQuestion.getCategory());
        values.put(QuestionsTable.COLUMN_ANSWER_ID, quizQuestion.getCorrectAnswerId());
        db.insert(QuestionsTable.TABLE_NAME, null, values);

    }

    public List<QuizQuestion> getAllQuestions() {
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                QuizQuestion quizQuestion = new QuizQuestion();
                quizQuestion.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                quizQuestion.setCategory(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY)));
                quizQuestion.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                quizQuestion.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                quizQuestion.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                quizQuestion.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                quizQuestion.setCorrectAnswerId(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_ID)));
                quizQuestions.add(quizQuestion);
            } while (c.moveToNext());
        }

        c.close();

        return quizQuestions;
    }
}
