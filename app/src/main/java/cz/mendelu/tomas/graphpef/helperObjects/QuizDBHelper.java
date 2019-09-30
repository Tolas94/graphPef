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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.CategoryTable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.QuestionsTable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.QuizAnswerTable;

public class QuizDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "QuizDBHelper";
    private static final String DATABASE_NAME = "GraphPefDatabase.db";
    private static final String COLLECTION_NAME_QUIZ_QUESTIONS = "quizQuestions";
    private static final String COLLECTION_NAME_USERS = "users";
    private static final String COLLECTION_NAME_QUIZ_CATEGORIES = "categories";
    private static final String COLLECTION_NAME_DATABASE_VERSION = "databaseVersion";
    private static final String DOCUMENT_NAME_DATABASE_VERSION = "version";
    private static final int DATABASE_VERSION = 1;

    //FirestoreTags
    //quizQuestions
    private static final String FIRESTORE_QUESTION = "question";
    private static final String FIRESTORE_ANSWER1 = "answer1";
    private static final String FIRESTORE_ANSWER2 = "answer2";
    private static final String FIRESTORE_ANSWER3 = "answer3";
    private static final String FIRESTORE_ANSWER4 = "answer4";
    private static final String FIRESTORE_CATEGORY = "category";
    private static final String FIRESTORE_CORRECT_ANSWER = "correctAnswer";
    //categories
    private static final String FIRESTORE_TITLE = "Title";
    //end Firestore tags

    //set to true only if creating new questions
    private static final Boolean GENERATE_QUESTIONS = false;

    private FirebaseFirestore onlineDatabase = FirebaseFirestore.getInstance();
    private CollectionReference questionsRef = onlineDatabase.collection(COLLECTION_NAME_QUIZ_QUESTIONS);
    private CollectionReference categoriesRef = onlineDatabase.collection(COLLECTION_NAME_QUIZ_CATEGORIES);
    private CollectionReference usersRef = onlineDatabase.collection(COLLECTION_NAME_USERS);
    private DocumentReference databaseVersion;

    private SQLiteDatabase db;

    private int lastScore;

    public QuizDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.QUIZ_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.CATEGORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuizAnswerTable.ANSWERS_TABLE_NAME);*/
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
                QuestionsTable.COLUMN_ANSWER_ID + " INTEGER, " +
                QuestionsTable.COLUMN_ANSWERED + " INTEGER, " +
                QuestionsTable.COLUMN_FIRESTORE_ID + " STRING " +
                ")";

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                CategoryTable.CATEGORY_TABLE_NAME + " ( " +
                CategoryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoryTable.COLUMN_CATEGORY + " TEXT, " +
                CategoryTable.COLUMN_UNLOCKED + " INTEGER, " +
                CategoryTable.COLUMN_FIRESTORE_ID + " STRING " +
                ")";

        final String SQL_CREATE_ANSWERS_TABLE = "CREATE TABLE " +
                QuizAnswerTable.ANSWERS_TABLE_NAME + " ( " +
                QuizAnswerTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuizAnswerTable.COLUMN_TIMETAG + " TEXT, " +
                QuizAnswerTable.COLUMN_POINTS_ACQUIRED + " INTEGER, " +
                QuizAnswerTable.COLUMN_QUESTIONS_ANSWERED + " INTEGER " +
                ")";

        db.execSQL(SQL_CREATE_ANSWERS_TABLE);
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        getQuizQuestionsFromFirestore();
        getQuizCategoriesFromFirestore();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "OnUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.QUIZ_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.CATEGORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuizAnswerTable.ANSWERS_TABLE_NAME);
        onCreate(db);
    }

    private void getQuizCategoriesFromFirestore() {
        categoriesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    Log.e(TAG, "queryDocumentSnapshots is empty()");
                } else {
                    fillCategoriesTable(queryDocumentSnapshots.getDocuments());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });

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

    private void fillCategoriesTable(List<DocumentSnapshot> documents) {
        Log.d(TAG, "fillCategoriesTable: size [" + documents.size() + "]");
        for (DocumentSnapshot document : documents) {
            List<String> values = new ArrayList<>();
            values.add(document.getString(FIRESTORE_TITLE));
            values.add(document.getId());
            addCategory(values);
            values.clear();
        }
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
                    , document.getDouble(FIRESTORE_CORRECT_ANSWER).intValue()
                    , document.getId());
            addQuestion(temp);
        }
    }

    private void addCategory(List<String> firestoreValues) {
        Log.d(TAG, "addCategory: category [" + firestoreValues.get(0) + "]");
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COLUMN_CATEGORY, firestoreValues.get(0));
        values.put(CategoryTable.COLUMN_FIRESTORE_ID, firestoreValues.get(1));
        db.insert(CategoryTable.CATEGORY_TABLE_NAME, null, values);

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
                quizQuestion.setQuestionID(c.getString(c.getColumnIndex(QuestionsTable._ID)));
                quizQuestion.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                quizQuestion.setCategory(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY)));
                quizQuestion.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                quizQuestion.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                quizQuestion.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                quizQuestion.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                quizQuestion.setCorrectAnswerId(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_ID)));
                quizQuestion.setAnswered(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWERED)) == 1);
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

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
         */

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "objektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "subjektivní ");
        newQuestion.put(FIRESTORE_ANSWER3, "vyčíslitelné");
        newQuestion.put(FIRESTORE_ANSWER4, "přímo úměrné bohatstvu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "statické");
        newQuestion.put(FIRESTORE_ANSWER2, "vzácne");
        newQuestion.put(FIRESTORE_ANSWER3, "proměnlivé");
        newQuestion.put(FIRESTORE_ANSWER4, "neekonomické");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Ekonomické potřeby");
        newQuestion.put(FIRESTORE_ANSWER1, "lze vyčíslit kapitálem");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou statky");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou stejné pro všechny");
        newQuestion.put(FIRESTORE_ANSWER4, "mají bezprostrědní vztah ke hospodařské činnosti");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřeba");
        newQuestion.put(FIRESTORE_ANSWER1, "je podmínená existenci trhu");
        newQuestion.put(FIRESTORE_ANSWER2, "objektivne uspokojí každého jedince");
        newQuestion.put(FIRESTORE_ANSWER3, "je proces uspokojení potřeby při kterém dojde ke zničení statku");
        newQuestion.put(FIRESTORE_ANSWER4, "není závislá na existenci směny");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Půda");
        newQuestion.put(FIRESTORE_ANSWER1, "není druhotným výrobním faktorem");
        newQuestion.put(FIRESTORE_ANSWER2, "v ekonomii je jenom ta, která je zemědělsky opracovávaná");
        newQuestion.put(FIRESTORE_ANSWER3, "je majetkem států");
        newQuestion.put(FIRESTORE_ANSWER4, "je všechno co není statek");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Práce");
        newQuestion.put(FIRESTORE_ANSWER1, "je nedílnou součastí života");
        newQuestion.put(FIRESTORE_ANSWER2, "existuje nezávisle na lidech a lidské činnosti");
        newQuestion.put(FIRESTORE_ANSWER3, "je ta činnost za kterou je získaná odměna");
        newQuestion.put(FIRESTORE_ANSWER4, "je předpokladem pro produkci");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Křivka poptávky ukazuje");
        newQuestion.put(FIRESTORE_ANSWER1, "jak se mění kupované množství daného statku v závislosti na jeho produkci");
        newQuestion.put(FIRESTORE_ANSWER2, "jak se mění kupované množství daného statku v závislosti na jeho cene");
        newQuestion.put(FIRESTORE_ANSWER3, "jak se mění prodávané množství daného statku v závislosti na ochote spotřebitele kupovat tenthle statek");
        newQuestion.put(FIRESTORE_ANSWER4, "ukazuje změnu všech kupujích na trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při vyšší cene statku kupuje");
        newQuestion.put(FIRESTORE_ANSWER1, "méně statku, neboť mu to přinese stejný úžitek");
        newQuestion.put(FIRESTORE_ANSWER2, "víc statku, protože mu to přinese vetší úžitek");
        newQuestion.put(FIRESTORE_ANSWER3, "méne statku, protože je limitován rozpočtem");
        newQuestion.put(FIRESTORE_ANSWER4, "víc statku, jelikož to lze pokládat za investici");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3
        );

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při vyšší cene statku");
        newQuestion.put(FIRESTORE_ANSWER1, "kupuje vetší množství statku, z obav před budoucim zvyšovaním ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "kupuje přestáva nakupovat daný statek");
        newQuestion.put(FIRESTORE_ANSWER3, "nakupuje méne tohoto statku, protože ho substituje jinými statky");
        newQuestion.put(FIRESTORE_ANSWER4, "zvyšuje mezní míru užívaní tohoto statku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Cenova elaticita poptávky udáva vztah mezi");
        newQuestion.put(FIRESTORE_ANSWER1, "poptávaním množním a zmenou ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "zmenou poptávaného množství a zmenou nabízeného množství");
        newQuestion.put(FIRESTORE_ANSWER3, "procentuálni zmenou nabízeného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_ANSWER4, "procentuálni zmenou poptávaného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Poptávka může být");
        newQuestion.put(FIRESTORE_ANSWER1, "jenom klesajíci");
        newQuestion.put(FIRESTORE_ANSWER2, "klesajíci a horizontálni");
        newQuestion.put(FIRESTORE_ANSWER3, "klesajíci, vertikálni");
        newQuestion.put(FIRESTORE_ANSWER4, "klesajíci, vertikálni a horizontálni");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita menší  než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita vetší než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();


    }

    private void addQuestionToFirestore(Map<String, Object> newQuestion) {
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

    private String getTimeNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void addQuizAnsered(Integer pointsAcquired, Integer questionsAnswered) {
        Log.d(TAG, "getAllQuestions: pointsAcquired[" + pointsAcquired + "] questionsAnswered" + questionsAnswered + "]");
        ContentValues insertedValue = new ContentValues();

        insertedValue.put(QuizAnswerTable.COLUMN_TIMETAG, getTimeNow());
        insertedValue.put(QuizAnswerTable.COLUMN_POINTS_ACQUIRED, pointsAcquired);
        insertedValue.put(QuizAnswerTable.COLUMN_QUESTIONS_ANSWERED, questionsAnswered);

        db.insert(QuizAnswerTable.ANSWERS_TABLE_NAME, null, insertedValue);
    }

    public void addQuestionAnswered(String questionID) {
        Log.d(TAG, "addQuestionAnswered: questionID[" + questionID + "]");
        ContentValues updatedValue = new ContentValues();

        updatedValue.put(QuestionsTable.COLUMN_ANSWERED, true);

        db.update(QuestionsTable.QUIZ_TABLE_NAME, updatedValue, "_id = " + questionID, null);
    }

    public void unlockCategory(String categoryID, Integer price) {
        Log.d(TAG, "addQuestionAnswered: categoryID[" + categoryID + "]");
        ContentValues updatedValue = new ContentValues();

        updatedValue.put(CategoryTable.COLUMN_UNLOCKED, price);

        db.update(CategoryTable.CATEGORY_TABLE_NAME, updatedValue, CategoryTable._ID + " = " + categoryID, null);
    }

    public int getScore() {
        int score = 0;

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + QuizAnswerTable.COLUMN_POINTS_ACQUIRED + ") as Total FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            score = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        return score;
    }

    public int getAvailablePoints() {
        int usedScore = 0, newScore = 0;

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + CategoryTable.COLUMN_UNLOCKED + ") as Total FROM " + CategoryTable.CATEGORY_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            usedScore = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        newScore = getScore() - usedScore;
        if (newScore != lastScore) {
            lastScore = newScore;

            //TODO zapisat to do firestoru
        }
        return newScore;
    }

    public List<String> getAllCategoriesNames() {
        List<String> list = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + CategoryTable.CATEGORY_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_CATEGORY)));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    //returns (name,price,id)
    public ArrayList<List<String>> getAllUnlockableCategories() {
        Log.d(TAG, "getAllUnlockableCategoriesNames");
        ArrayList<List<String>> list = new ArrayList<>();
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + CategoryTable.CATEGORY_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                boolean locked = cursor.getInt(cursor.getColumnIndex(CategoryTable.COLUMN_UNLOCKED)) == 0;
                String categoryName = cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_CATEGORY));
                if (locked) {
                    Integer price = (list.size() + 1) * 75;
                    Log.d(TAG, "category [" + categoryName + "] added with price[" + price + "]");
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(categoryName);
                    temp.add(price.toString());
                    String id = cursor.getString(cursor.getColumnIndex(CategoryTable._ID));
                    temp.add(id);
                    list.add(temp);
                } else {
                    Log.d(TAG, "category [" + categoryName + "] is locked");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    public int getNumCorrectlyAnsweredQuestion() {
        Log.d(TAG, "getNumCorrectlyAnsweredQuestion");
        db = getReadableDatabase();
        int retVal = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + QuestionsTable.COLUMN_ANSWERED + ") as Total FROM " + QuestionsTable.QUIZ_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();


        Log.d(TAG, "getNumCorrectlyAnsweredQuestion: return " + retVal);
        return retVal;
    }

    public int getNumUnlockedCategories() {
        Log.d(TAG, "getNumUnlockedCategories");
        db = getReadableDatabase();
        int retVal = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(" + CategoryTable.COLUMN_UNLOCKED + ") as Total FROM " + CategoryTable.CATEGORY_TABLE_NAME + " WHERE " + CategoryTable.COLUMN_UNLOCKED + "<> ? ", new String[]{0 + ""});

        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();


        Log.d(TAG, "getNumUnlockedCategories: return " + retVal);
        return retVal;
    }

    public int getNumAllCategories() {
        Log.d(TAG, "getNumAllCategories");
        int retVal = 0;
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) as Total FROM " + CategoryTable.CATEGORY_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        Log.d(TAG, "getNumAllCategories: return " + retVal);
        return retVal;
    }

    public int getNumAllQuestions() {
        Log.d(TAG, "getNumAllQuestions");
        int retVal = 0;
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) as Total FROM " + QuestionsTable.QUIZ_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        Log.d(TAG, "getNumAllQuestions: return " + retVal);
        return retVal;
    }

    public Integer getHighScore() {
        Log.d(TAG, "getHighScore");
        int retVal = 0;
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + QuizAnswerTable.COLUMN_POINTS_ACQUIRED + " ) as Score FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Score"));
        }

        cursor.close();

        Log.d(TAG, "getHighScore: return " + retVal);
        return retVal;
    }

    public Integer getHighScoreStreak() {
        Log.d(TAG, "getHighScore");
        int retVal = 0;
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + QuizAnswerTable.COLUMN_QUESTIONS_ANSWERED + " ) as Score FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Score"));
        }

        cursor.close();

        Log.d(TAG, "getHighScore: return " + retVal);
        return retVal;
    }
}
