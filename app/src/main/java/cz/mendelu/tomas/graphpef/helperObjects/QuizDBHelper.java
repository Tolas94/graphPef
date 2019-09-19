package cz.mendelu.tomas.graphpef.helperObjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.QuestionsTable;

public class QuizDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "QuizDBHelper";
    private static final String DATABASE_NAME = "GraphPefDatabase.db";
    private static final String COLLECTION_NAME = "quizQuestions";
    private static final int DATABASE_VERSION = 1;

    //FirestoreTags
    private static final String FIRESTORE_QUESTION = "question";
    private static final String FIRESTORE_ANSWER1 = "answer1";
    private static final String FIRESTORE_ANSWER2 = "answer2";
    private static final String FIRESTORE_ANSWER3 = "answer3";
    private static final String FIRESTORE_ANSWER4 = "answer4";
    private static final String FIRESTORE_CATEGORY = "category";
    private static final String FIRESTORE_CORRECT_ANSWER = "correctAnswer";
    //end Firestore tags

    //set to true only if creating new questions
    private static final Boolean GENERATE_QUESTIONS = false;

    private FirebaseFirestore onlineDatabase = FirebaseFirestore.getInstance();
    private CollectionReference questionsRef = onlineDatabase.collection(COLLECTION_NAME);

    private SQLiteDatabase db;

    public QuizDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate");
        this.db = db;

        if (GENERATE_QUESTIONS) {
            generateQustions();
            Log.d(TAG, "Generating questions");
        }

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.QUIZ_TABLE_NAME + " ( " +
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
        getQuizQuestionsFromFirestore();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "OnUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.QUIZ_TABLE_NAME);
        onCreate(db);
    }

    private void getQuizQuestionsFromFirestore() {
        questionsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.e(TAG, "queryDocumentSnapshots is empty()");
                } else {
                    fillQuestionsTable(queryDocumentSnapshots.getDocuments());
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    private void fillQuestionsTable(List<DocumentSnapshot> documents) {

        Log.d(TAG, "fillQuestionsTable: size [" + documents.size() + "]");
        for (DocumentSnapshot document : documents) {
            QuizQuestion temp = new QuizQuestion(document.getString(FIRESTORE_QUESTION)
                    , document.getString(FIRESTORE_ANSWER1)
                    , document.getString(FIRESTORE_ANSWER2)
                    , document.getString(FIRESTORE_ANSWER3)
                    , document.getString(FIRESTORE_ANSWER4)
                    , document.getString(FIRESTORE_CATEGORY)
                    , document.getDouble(FIRESTORE_CORRECT_ANSWER).intValue());
            addQuestion(temp);
        }
    }

    private void addQuestion(QuizQuestion quizQuestion) {
        Log.d(TAG, "addQuestion: question [" + quizQuestion.getQuestion() + "]");
        ContentValues values = new ContentValues();
        values.put(QuestionsTable.COLUMN_QUESTION, quizQuestion.getQuestion());
        values.put(QuestionsTable.COLUMN_OPTION1, quizQuestion.getOption1());
        values.put(QuestionsTable.COLUMN_OPTION2, quizQuestion.getOption2());
        values.put(QuestionsTable.COLUMN_OPTION3, quizQuestion.getOption3());
        values.put(QuestionsTable.COLUMN_OPTION4, quizQuestion.getOption4());
        values.put(QuestionsTable.COLUMN_CATEGORY, quizQuestion.getCategory());
        values.put(QuestionsTable.COLUMN_ANSWER_ID, quizQuestion.getCorrectAnswerId());
        db.insert(QuestionsTable.QUIZ_TABLE_NAME, null, values);

    }

    public List<QuizQuestion> getAllQuestions() {
        Log.d(TAG, "getAllQuestions: ");
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.QUIZ_TABLE_NAME, null);

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

        Log.d(TAG, "getAllQuestions: finish size[" + quizQuestions.size() + "]");
        return quizQuestions;
    }

    // this method is to be used only to create entry in Firesstore database once
    private void generateQustions() {
        Map<String, Object> newQuestion = new HashMap<>();

        /* example
        newQuestion.put(FIRESTORE_QUESTION,"");
        newQuestion.put(FIRESTORE_ANSWER1,"");
        newQuestion.put(FIRESTORE_ANSWER2,"");
        newQuestion.put(FIRESTORE_ANSWER3,"");
        newQuestion.put(FIRESTORE_ANSWER4,"");
        newQuestion.put(FIRESTORE_CATEGORY,"");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER,"");

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();
         */

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "objektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "subjektivní ");
        newQuestion.put(FIRESTORE_ANSWER3, "vyčíslitelné");
        newQuestion.put(FIRESTORE_ANSWER4, "přímo úměrné bohatstvu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "statické");
        newQuestion.put(FIRESTORE_ANSWER2, "vzácne");
        newQuestion.put(FIRESTORE_ANSWER3, "proměnlivé");
        newQuestion.put(FIRESTORE_ANSWER4, "neekonomické");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Ekonomické potřeby");
        newQuestion.put(FIRESTORE_ANSWER1, "lze vyčíslit kapitálem");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou statky");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou stejné pro všechny");
        newQuestion.put(FIRESTORE_ANSWER4, "mají bezprostrědní vztah ke hospodařské činnosti");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřeba");
        newQuestion.put(FIRESTORE_ANSWER1, "je podmínená existenci trhu");
        newQuestion.put(FIRESTORE_ANSWER2, "objektivne uspokojí každého jedince");
        newQuestion.put(FIRESTORE_ANSWER3, "je proces uspokojení potřeby při kterém dojde ke zničení statku");
        newQuestion.put(FIRESTORE_ANSWER4, "není závislá na existenci směny");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Půda");
        newQuestion.put(FIRESTORE_ANSWER1, "není druhotným výrobním faktorem");
        newQuestion.put(FIRESTORE_ANSWER2, "v ekonomii je jenom ta, která je zemědělsky opracovávaná");
        newQuestion.put(FIRESTORE_ANSWER3, "je majetkem států");
        newQuestion.put(FIRESTORE_ANSWER4, "je všechno co není statek");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Práce");
        newQuestion.put(FIRESTORE_ANSWER1, "je nedílnou součastí života");
        newQuestion.put(FIRESTORE_ANSWER2, "existuje nezávisle na lidech a lidské činnosti");
        newQuestion.put(FIRESTORE_ANSWER3, "je ta činnost za kterou je získaná odměna");
        newQuestion.put(FIRESTORE_ANSWER4, "je předpokladem pro produkci");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Křivka poptávky ukazuje");
        newQuestion.put(FIRESTORE_ANSWER1, "jak se mění kupované množství daného statku v závislosti na jeho produkci");
        newQuestion.put(FIRESTORE_ANSWER2, "jak se mění kupované množství daného statku v závislosti na jeho cene");
        newQuestion.put(FIRESTORE_ANSWER3, "jak se mění prodávané množství daného statku v závislosti na ochote spotřebitele kupovat tenthle statek");
        newQuestion.put(FIRESTORE_ANSWER4, "ukazuje změnu všech kupujích na trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při vyšší cene statku kupuje");
        newQuestion.put(FIRESTORE_ANSWER1, "méně statku, neboť mu to přinese stejný úžitek");
        newQuestion.put(FIRESTORE_ANSWER2, "víc statku, protože mu to přinese vetší úžitek");
        newQuestion.put(FIRESTORE_ANSWER3, "méne statku, protože je limitován rozpočtem");
        newQuestion.put(FIRESTORE_ANSWER4, "víc statku, jelikož to lze pokládat za investici");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3
        );

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při vyšší cene statku");
        newQuestion.put(FIRESTORE_ANSWER1, "kupuje vetší množství statku, z obav před budoucim zvyšovaním ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "kupuje přestáva nakupovat daný statek");
        newQuestion.put(FIRESTORE_ANSWER3, "nakupuje méne tohoto statku, protože ho substituje jinými statky");
        newQuestion.put(FIRESTORE_ANSWER4, "zvyšuje mezní míru užívaní tohoto statku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Cenova elaticita poptávky udáva vztah mezi");
        newQuestion.put(FIRESTORE_ANSWER1, "poptávaním množním a zmenou ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "zmenou poptávaného množství a zmenou nabízeného množství");
        newQuestion.put(FIRESTORE_ANSWER3, "procentuálni zmenou nabízeného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_ANSWER4, "procentuálni zmenou poptávaného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Poptávka může být");
        newQuestion.put(FIRESTORE_ANSWER1, "jenom klesajíci");
        newQuestion.put(FIRESTORE_ANSWER2, "klesajíci a horizontálni");
        newQuestion.put(FIRESTORE_ANSWER3, "klesajíci, vertikálni");
        newQuestion.put(FIRESTORE_ANSWER4, "klesajíci, vertikálni a horizontálni");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita menší  než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita vetší než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFIrestore(newQuestion);
        newQuestion.clear();


    }

    private void addQuestionToFIrestore(Map<String, Object> newQuestion) {
        questionsRef
                .add(newQuestion)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }
}
