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
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.CategoryTable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.QuestionsTable;
import cz.mendelu.tomas.graphpef.helperObjects.QuizContract.QuizAnswerTable;

import static android.database.Cursor.FIELD_TYPE_NULL;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_ANSWER1;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_ANSWER2;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_ANSWER3;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_ANSWER4;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_CATEGORY;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_CATEGORY_ID;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_CORRECT_ANSWER;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_QUESTION;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_SUBJECT;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_USER_ANSWERS_STREAK;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_USER_ID;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_USER_MAIL;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_USER_POINTS;
import static cz.mendelu.tomas.graphpef.helperObjects.QuizContract.FIRESTORE_USER_POINTS_STREAK;

public class QuizDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "QuizDBHelper";
    private boolean databaseInitDone = true;
    private boolean userSuccesfullyLogged = false;

    private ObservableDB observable = new ObservableDB();

    //set to true only if creating new questions
    private static final Boolean GENERATE_QUESTIONS = false;

    private FirebaseFirestore onlineDatabase = FirebaseFirestore.getInstance();
    private CollectionReference questionsRef = onlineDatabase.collection(QuizContract.COLLECTION_NAME_QUIZ_QUESTIONS);
    private CollectionReference categoriesRef = onlineDatabase.collection(QuizContract.COLLECTION_NAME_QUIZ_CATEGORIES);
    private DocumentReference usersRef;
    private DocumentReference databaseVersion;

    private SQLiteDatabase db;

    private int lastScore;

    public QuizDBHelper(@Nullable Context context) {
        super(context, QuizContract.DATABASE_NAME, null, QuizContract.DATABASE_VERSION);
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

        createDBTables();

        databaseInitDone = true;
        getQuizQuestionsFromFirestore();
        getQuizCategoriesFromFirestore();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "OnUpgrade");
        //TODO upgrade mechanism
        destroyDBTables();
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
        if (!databaseInitDone) {
            return;
        }
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
            values.add(document.getString(QuizContract.FIRESTORE_TITLE));
            values.add(document.getId());
            if (document.get(QuizContract.FIRESTORE_ANSWERED) != null) {
                values.add("0");
            }

            addCategory(values);
            values.clear();
        }
    }

    private void fillQuestionsTable(List<DocumentSnapshot> documents) {


        Log.d(TAG, "fillQuestionsTable: size [" + documents.size() + "]");
        for (DocumentSnapshot document : documents) {
            QuizQuestion temp = new QuizQuestion(document.getString(FIRESTORE_QUESTION)
                    , document.getId() //firestoreID
                    , document.getString(FIRESTORE_ANSWER1)
                    , document.getString(FIRESTORE_ANSWER2)
                    , document.getString(FIRESTORE_ANSWER3)
                    , document.getString(FIRESTORE_ANSWER4)
                    , document.getString(FIRESTORE_CATEGORY)
                    , document.getDouble(FIRESTORE_CORRECT_ANSWER).intValue()
                    , document.getString(FIRESTORE_SUBJECT)
                    , document.getString(FIRESTORE_CATEGORY_ID));
            addQuestion(temp);
        }
    }

    private void addCategory(List<String> firestoreValues) {
        Log.d(TAG, "addCategory: category [" + firestoreValues.get(0) + "]");
        ContentValues values = new ContentValues();
        values.put(CategoryTable.COLUMN_CATEGORY, firestoreValues.get(0));
        values.put(CategoryTable.COLUMN_FIRESTORE_ID, firestoreValues.get(1));
        if (firestoreValues.size() > 2) {
            values.put(CategoryTable.COLUMN_UNLOCKED, firestoreValues.get(2));
        }
        db.insert(CategoryTable.CATEGORY_TABLE_NAME, null, values);


    }

    private void addQuestion(QuizQuestion quizQuestion) {
        Log.d(TAG, "addQuestion: question [" + quizQuestion.getQuestion() + "]");
        ContentValues values = new ContentValues();
        values.put(QuestionsTable.COLUMN_QUESTION, quizQuestion.getQuestion());
        values.put(QuestionsTable.COLUMN_FIRESTORE_ID, quizQuestion.getFirestoreID());
        values.put(QuestionsTable.COLUMN_OPTION1, quizQuestion.getOption1());
        values.put(QuestionsTable.COLUMN_OPTION2, quizQuestion.getOption2());
        values.put(QuestionsTable.COLUMN_OPTION3, quizQuestion.getOption3());
        values.put(QuestionsTable.COLUMN_OPTION4, quizQuestion.getOption4());
        values.put(QuestionsTable.COLUMN_CATEGORY, quizQuestion.getCategory());
        values.put(QuestionsTable.COLUMN_CATEGORY_ID, quizQuestion.getCategory());
        values.put(QuestionsTable.COLUMN_ANSWER_ID, quizQuestion.getCorrectAnswerId());
        values.put(QuestionsTable.COLUMN_SUBJECT, quizQuestion.getFirestoreID());
        values.put(QuestionsTable.COLUMN_ANSWERED, quizQuestion.isAnswered());

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
                quizQuestion.setFirestoreID(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_FIRESTORE_ID)));
                quizQuestion.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                quizQuestion.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                quizQuestion.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                quizQuestion.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                quizQuestion.setOption4(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION4)));
                quizQuestion.setCategory(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY)));
                quizQuestion.setCategoryId(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                quizQuestion.setCorrectAnswerId(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_ID)));
                quizQuestion.setAnswered(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWERED)) == 1);
                quizQuestion.setSubject(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_SUBJECT)));
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
        int question = 0;

        /* example
        newQuestion.put(FIRESTORE_QUESTION,"");
        newQuestion.put(FIRESTORE_ANSWER1,"");
        newQuestion.put(FIRESTORE_ANSWER2,"");
        newQuestion.put(FIRESTORE_ANSWER3,"");
        newQuestion.put(FIRESTORE_ANSWER4,"");
        newQuestion.put(FIRESTORE_CATEGORY,"");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1"); 
		newQuestion.put(FIRESTORE_CORRECT_ANSWER,"");

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
		question++;
		Log.d(TAG, "generateQustions: " + question);
         */

        //Vztah potřeb

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "objektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "subjektivní");
        newQuestion.put(FIRESTORE_ANSWER3, "vyčíslitelné");
        newQuestion.put(FIRESTORE_ANSWER4, "přímo úměrné bohatstvu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);
        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Potřeby jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "statické");
        newQuestion.put(FIRESTORE_ANSWER2, "vzácne");
        newQuestion.put(FIRESTORE_ANSWER3, "proměnlivé");
        newQuestion.put(FIRESTORE_ANSWER4, "neekonomické");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Ekonomické potřeby");
        newQuestion.put(FIRESTORE_ANSWER1, "lze vyčíslit kapitálem");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou statky");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou stejné pro všechny");
        newQuestion.put(FIRESTORE_ANSWER4, "mají bezprostřední vztah k hospodářské činnosti");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Spotřeba");
        newQuestion.put(FIRESTORE_ANSWER1, "je podmínená existenci trhu");
        newQuestion.put(FIRESTORE_ANSWER2, "objektivne uspokojí každého jedince");
        newQuestion.put(FIRESTORE_ANSWER3, "je proces uspokojení potřeby při kterém dojde ke zničení statku");
        newQuestion.put(FIRESTORE_ANSWER4, "není závislá na existenci směny");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Půda");
        newQuestion.put(FIRESTORE_ANSWER1, "není druhotným výrobním faktorem");
        newQuestion.put(FIRESTORE_ANSWER2, "v ekonomii je jenom ta, která je zemědělsky opracovávaná");
        newQuestion.put(FIRESTORE_ANSWER3, "je majetkem států");
        newQuestion.put(FIRESTORE_ANSWER4, "je všechno co není statek");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Práce");
        newQuestion.put(FIRESTORE_ANSWER1, "je nedílnou součastí života");
        newQuestion.put(FIRESTORE_ANSWER2, "existuje nezávisle na lidech a lidské činnosti");
        newQuestion.put(FIRESTORE_ANSWER3, "je ta činnost za kterou je získaná odměna");
        newQuestion.put(FIRESTORE_ANSWER4, "je předpokladem pro produkci");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Křivka poptávky ukazuje");
        newQuestion.put(FIRESTORE_ANSWER1, "jak se mění kupované množství daného statku v závislosti na jeho produkci");
        newQuestion.put(FIRESTORE_ANSWER2, "jak se mění kupované množství daného statku v závislosti na jeho cene");
        newQuestion.put(FIRESTORE_ANSWER3, "jak se mění prodávané množství daného statku v závislosti na ochotě spotřebitele kupovat tenhle statek");
        newQuestion.put(FIRESTORE_ANSWER4, "ukazuje změnu všech kupujích na trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Barter");
        newQuestion.put(FIRESTORE_ANSWER1, "je naturální směna");
        newQuestion.put(FIRESTORE_ANSWER2, "je směna, při které je potřeba jiné platidlo než peníze");
        newQuestion.put(FIRESTORE_ANSWER3, "není forma směny, protože při ní nepotřebujeme peníze");
        newQuestion.put(FIRESTORE_ANSWER4, "je forma platby");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Zákon klesajících výnosů");
        newQuestion.put(FIRESTORE_ANSWER1, "za jinak stejných podmínek zvýšení produkce zvyšuje zisk");
        newQuestion.put(FIRESTORE_ANSWER2, "za jinak stejných podmínek zvýšení produkce nemění náklady");
        newQuestion.put(FIRESTORE_ANSWER3, "za jinak stejných podmínek zvýšení produkce minimalizuje výnosy");
        newQuestion.put(FIRESTORE_ANSWER4, "za jinak stejných podmínek zvýšení produkce vyžaduje stále větší zvyšování množství výrobního faktoru");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Mezní produkt");
        newQuestion.put(FIRESTORE_ANSWER1, "je změna vyráběného množství produktu v meziobdobí");
        newQuestion.put(FIRESTORE_ANSWER2, "je změna poměru zisků a nákladů");
        newQuestion.put(FIRESTORE_ANSWER3, "vyjadřuje přírůstek výstupu firmy při změně právě jednoho ze vstupů o jednu jednotku");
        newQuestion.put(FIRESTORE_ANSWER4, "značíme ho jako MRP");
        newQuestion.put(FIRESTORE_CATEGORY, "Vztah potřeb");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "02");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Vztah potřeb

        //Ekonomie a předmět zkoumání

        newQuestion.put(FIRESTORE_QUESTION, "Cena");
        newQuestion.put(FIRESTORE_ANSWER1, "vzniká v bode střetu poptávky a nabídky");
        newQuestion.put(FIRESTORE_ANSWER2, "je pro všechny nakupující stejná");
        newQuestion.put(FIRESTORE_ANSWER3, "vyjadřuje vnitřní hodnotu statku");
        newQuestion.put(FIRESTORE_ANSWER4, "vychází z ceny práce");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Vlastní zájem");
        newQuestion.put(FIRESTORE_ANSWER1, "člověk sleduje jenom svůj osobní zájem");
        newQuestion.put(FIRESTORE_ANSWER2, "není v souladu s volním trhem");
        newQuestion.put(FIRESTORE_ANSWER3, "se týká jenom ekonomických aktivit");
        newQuestion.put(FIRESTORE_ANSWER4, "neexistuje");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Preference jsou");
        newQuestion.put(FIRESTORE_ANSWER1, "subjektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "objektivní");
        newQuestion.put(FIRESTORE_ANSWER3, "neovlivnitelné");
        newQuestion.put(FIRESTORE_ANSWER4, "pro všechny stejné");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Cenová liberalizace");
        newQuestion.put(FIRESTORE_ANSWER1, "je cena určována sdružením liberálních hnutí");
        newQuestion.put(FIRESTORE_ANSWER2, "vzniká na volném trhu");
        newQuestion.put(FIRESTORE_ANSWER3, "nedošlo k ni v České republice");
        newQuestion.put(FIRESTORE_ANSWER4, "se prosazuje v každém hospodářském mechanismu");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Tržní ekonomika");
        newQuestion.put(FIRESTORE_ANSWER1, "vzniká jenom v státech, které mají západní kulturu");
        newQuestion.put(FIRESTORE_ANSWER2, "je všude tam, kde jsou splněny podmínky pro vznik volného trhu");
        newQuestion.put(FIRESTORE_ANSWER3, "vzniká jenom za účasti státu jako dominantního prvku");
        newQuestion.put(FIRESTORE_ANSWER4, "není ovlivnitelná státními zásahy");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Vzácnost je");
        newQuestion.put(FIRESTORE_ANSWER1, "objektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "subjektivní ");
        newQuestion.put(FIRESTORE_ANSWER3, "vyčíslitelná");
        newQuestion.put(FIRESTORE_ANSWER4, "přímo úměrná bohatství národa");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Neviditelná ruka trhu");
        newQuestion.put(FIRESTORE_ANSWER1, "funguje jenom v tržní ekonomice");
        newQuestion.put(FIRESTORE_ANSWER2, "je systém zákonů, který reguluje cenu");
        newQuestion.put(FIRESTORE_ANSWER3, "vede ke vlastnímu prospěchu");
        newQuestion.put(FIRESTORE_ANSWER4, "ovlivňuje cenu");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Ekonomie je");
        newQuestion.put(FIRESTORE_ANSWER1, "nejednoznační věda");
        newQuestion.put(FIRESTORE_ANSWER2, "přírodovědná věda");
        newQuestion.put(FIRESTORE_ANSWER3, "společenská věda");
        newQuestion.put(FIRESTORE_ANSWER4, "věda o penězích");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Společný prospěch");
        newQuestion.put(FIRESTORE_ANSWER1, "je sledován všemi lidmi za všech okolností");
        newQuestion.put(FIRESTORE_ANSWER2, "je v souladu s vlastním zájmem");
        newQuestion.put(FIRESTORE_ANSWER3, "neexistuje");
        newQuestion.put(FIRESTORE_ANSWER4, "je sledován lidmi, kteří nesledují vlastní zájem");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Poptávka vyjadřuje");
        newQuestion.put(FIRESTORE_ANSWER1, "schopnost lidí nakupovat");
        newQuestion.put(FIRESTORE_ANSWER2, "nabízení množství při dané ceně");
        newQuestion.put(FIRESTORE_ANSWER3, "ochotu spotřebitelů nakupovat");
        newQuestion.put(FIRESTORE_ANSWER4, "cenu, při které je spotřebitel motivován k nákupu");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Racionální chování");
        newQuestion.put(FIRESTORE_ANSWER1, "sleduje objektivní cíle");
        newQuestion.put(FIRESTORE_ANSWER2, "pomáhá dosáhnout maximálního možného užitku");
        newQuestion.put(FIRESTORE_ANSWER3, "není nutným předpokladem ekonomie");
        newQuestion.put(FIRESTORE_ANSWER4, "je chování na základě logického myšlení");
        newQuestion.put(FIRESTORE_CATEGORY, "Ekonomie a předmět zkoumání");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "03");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Ekonomie a předmět zkoumání

        //Chování spotřebitele

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při vyšší cene statku kupuje");
        newQuestion.put(FIRESTORE_ANSWER1, "méně statku, neboť mu to přinese stejný úžitek");
        newQuestion.put(FIRESTORE_ANSWER2, "víc statku, protože mu to přinese vetší úžitek");
        newQuestion.put(FIRESTORE_ANSWER3, "méne statku, protože je limitován rozpočtem");
        newQuestion.put(FIRESTORE_ANSWER4, "víc statku, jelikož to lze pokládat za investici");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3
        );

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitel při růstu ceny statku");
        newQuestion.put(FIRESTORE_ANSWER1, "kupuje vetší množství statku, z obav před budoucim zvyšovaním ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "kupuje přestáva nakupovat daný statek");
        newQuestion.put(FIRESTORE_ANSWER3, "nakupuje méne tohoto statku, protože ho substituje jinými statky");
        newQuestion.put(FIRESTORE_ANSWER4, "zvyšuje mezní míru užívaní tohoto statku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Cenova elaticita poptávky udáva vztah mezi");
        newQuestion.put(FIRESTORE_ANSWER1, "poptáváným množstvím a změnou ceny");
        newQuestion.put(FIRESTORE_ANSWER2, "zmenou poptávaného množství a zmenou nabízeného množství");
        newQuestion.put(FIRESTORE_ANSWER3, "procentuálni zmenou nabízeného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_ANSWER4, "procentuálni zmenou poptávaného množství a procentuálni zmenou ceny");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");

        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Poptávka může být");
        newQuestion.put(FIRESTORE_ANSWER1, "jenom klesajíci");
        newQuestion.put(FIRESTORE_ANSWER2, "klesajíci a horizontálni");
        newQuestion.put(FIRESTORE_ANSWER3, "klesajíci, vertikálni");
        newQuestion.put(FIRESTORE_ANSWER4, "klesajíci, vertikálni a horizontálni");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita menší  než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Jestliže je elasticita vetší než 1, jde o");
        newQuestion.put(FIRESTORE_ANSWER1, "neelastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER2, "efektivní poptávku");
        newQuestion.put(FIRESTORE_ANSWER3, "elastickou poptávku");
        newQuestion.put(FIRESTORE_ANSWER4, "neefektivní poptávku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Indiferenční křivka");
        newQuestion.put(FIRESTORE_ANSWER1, "je rostoucí");
        newQuestion.put(FIRESTORE_ANSWER2, "spojuje vybrané kombinace spotřeby se stejným užitkem");
        newQuestion.put(FIRESTORE_ANSWER3, "je klesající");
        newQuestion.put(FIRESTORE_ANSWER4, "popisuje různé hladiny užitku");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Mezní užitek");
        newQuestion.put(FIRESTORE_ANSWER1, "je konstantní");
        newQuestion.put(FIRESTORE_ANSWER2, "je rostoucí");
        newQuestion.put(FIRESTORE_ANSWER3, "se nemění se změnou množství vyráběného");
        newQuestion.put(FIRESTORE_ANSWER4, "je klesající");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud došlo k růstu ceny černého čaje");
        newQuestion.put(FIRESTORE_ANSWER1, "dojde k růstu ceny kávy, pokud ji pokládáme za substitut");
        newQuestion.put(FIRESTORE_ANSWER2, "může dojít k změně preferencí");
        newQuestion.put(FIRESTORE_ANSWER3, "sníží se spotřebitelova poptávka po černém čaji");
        newQuestion.put(FIRESTORE_ANSWER4, "zvýší se spotřebitelova poptávka po kávě");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Spotřebitelův přebytek");
        newQuestion.put(FIRESTORE_ANSWER1, "je dán rozdílem toho, co by byl spotřebitel ochoten za statek zaplatit a kolik ve skutečnosti zaplatil");
        newQuestion.put(FIRESTORE_ANSWER2, "je stejný přebytku výrobce");
        newQuestion.put(FIRESTORE_ANSWER3, "je nezávislý na ceně statku");
        newQuestion.put(FIRESTORE_ANSWER4, "záleží na výšce slevy");
        newQuestion.put(FIRESTORE_CATEGORY, "Chování spotřebitele");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "04");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);


        //end Chování spotřebitele

        //Náklady

        newQuestion.put(FIRESTORE_QUESTION, "Variabilní náklady");
        newQuestion.put(FIRESTORE_ANSWER1, "jsou vždy vyšší než-li fixní náklady");
        newQuestion.put(FIRESTORE_ANSWER2, "s růstem produkce klesají");
        newQuestion.put(FIRESTORE_ANSWER3, "se nemění s rozsahem produkce");
        newQuestion.put(FIRESTORE_ANSWER4, "je nutné pokrýt aby firma neodešla z trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Fixní náklady");
        newQuestion.put(FIRESTORE_ANSWER1, "Nejsou vyčíslitelné");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou klesající v závislosti od produkovaného množství");
        newQuestion.put(FIRESTORE_ANSWER3, "se nemění s rozsahem produkce");
        newQuestion.put(FIRESTORE_ANSWER4, "je nutné pokrýt aby firma neodešla z trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Průměrní variabilní náklady - AVC");
        newQuestion.put(FIRESTORE_ANSWER1, "jsou přepočítané variabilní náklady na jednotku produkce");
        newQuestion.put(FIRESTORE_ANSWER2, "se zvyšujícím objemem produkce stále klesají");
        newQuestion.put(FIRESTORE_ANSWER3, "když je firma na min. úrovni AVC, tak v krátkém období odchází z trhu");
        newQuestion.put(FIRESTORE_ANSWER4, "při rozhodováni o odchodu z trhu je firma nebere v úvahu");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Průměrné fixní náklady - AFC");
        newQuestion.put(FIRESTORE_ANSWER1, "s růstem produkce rostou");
        newQuestion.put(FIRESTORE_ANSWER2, "s růstem produkce klesají");
        newQuestion.put(FIRESTORE_ANSWER3, "se nemění s rozsahem produkce");
        newQuestion.put(FIRESTORE_ANSWER4, "jsou vždy vyšší než průměrní variabilní náklady");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Náklady");
        newQuestion.put(FIRESTORE_ANSWER1, "ekonomické nezahrnují i náklady obětované příležitosti");
        newQuestion.put(FIRESTORE_ANSWER2, "utopené náklady jsou brány v úvahy při ekonomickém rozhodování");
        newQuestion.put(FIRESTORE_ANSWER3, "s růstem produkce rostou");
        newQuestion.put(FIRESTORE_ANSWER4, "v krátkém období se projevují jenom fixní");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Ekonomický zisk");
        newQuestion.put(FIRESTORE_ANSWER1, "Je rovný účetnímu zisku");
        newQuestion.put(FIRESTORE_ANSWER2, "Může být v dlouhém období nulový");
        newQuestion.put(FIRESTORE_ANSWER3, "Nesmí být záporný v krátkém období");
        newQuestion.put(FIRESTORE_ANSWER4, "Je v krátkém období vždy kladný");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud při daném objemu produkce platí AR > AC, pak");
        newQuestion.put(FIRESTORE_ANSWER1, "firma dosahuje ztráty");
        newQuestion.put(FIRESTORE_ANSWER2, "firma je v optimu");
        newQuestion.put(FIRESTORE_ANSWER3, "firma je v ekonomickém zisku");
        newQuestion.put(FIRESTORE_ANSWER4, "firma přestává vyrábět");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud při daném objemu produkce platí MR = MC, pak");
        newQuestion.put(FIRESTORE_ANSWER1, "firma dosahuje ztráty");
        newQuestion.put(FIRESTORE_ANSWER2, "firma je v optimu");
        newQuestion.put(FIRESTORE_ANSWER3, "firma je v ekonomickém zisku");
        newQuestion.put(FIRESTORE_ANSWER4, "firma přestává vyrábět");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud při daném objemu produkce platí AR < AC, pak");
        newQuestion.put(FIRESTORE_ANSWER1, "firma dosahuje ztráty");
        newQuestion.put(FIRESTORE_ANSWER2, "firma je v optimu");
        newQuestion.put(FIRESTORE_ANSWER3, "firma je v ekonomickém zisku");
        newQuestion.put(FIRESTORE_ANSWER4, "firma přestává vyrábět");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma odchází z trhu");
        newQuestion.put(FIRESTORE_ANSWER1, "při jakékoli krátkodobé ztrátě");
        newQuestion.put(FIRESTORE_ANSWER2, "pokud pokrývá jenom fixní náklady");
        newQuestion.put(FIRESTORE_ANSWER3, "pokud nemá v dlouhém období vetší než nulový ekonomický zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "pokud v dlouhém obdoví nedosahuje nulového ekonomického zisku");
        newQuestion.put(FIRESTORE_CATEGORY, "Náklady");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "05");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Náklady

        //Tržní rovnováha a efektivnost

        newQuestion.put(FIRESTORE_QUESTION, "MR = MC a p = AC");
        newQuestion.put(FIRESTORE_ANSWER1, "je firma v rovnováze a je ztrátová");
        newQuestion.put(FIRESTORE_ANSWER2, "je firma v rovnováze a je zisková");
        newQuestion.put(FIRESTORE_ANSWER3, "je firma v rovnováze a dosahuje 0 zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "firma dosahuje účetní zisk");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "MR = MC a p > AC");
        newQuestion.put(FIRESTORE_ANSWER1, "je firma v rovnováze a je ztrátová");
        newQuestion.put(FIRESTORE_ANSWER2, "je firma v rovnováze a je zisková");
        newQuestion.put(FIRESTORE_ANSWER3, "je firma v rovnováze a dosahuje 0 zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "firma dosahuje účetní zisk");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "MR = MC a p < AC");
        newQuestion.put(FIRESTORE_ANSWER1, "je firma v rovnováze a je ztrátová");
        newQuestion.put(FIRESTORE_ANSWER2, "je firma v rovnováze a je zisková");
        newQuestion.put(FIRESTORE_ANSWER3, "je firma v rovnováze a dosahuje 0 zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "firma dosahuje účetní zisk");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma na dokonale konkurenčním trhu dosáhne zisku");
        newQuestion.put(FIRESTORE_ANSWER1, "vždy");
        newQuestion.put(FIRESTORE_ANSWER2, "jenom v krátkém období");
        newQuestion.put(FIRESTORE_ANSWER3, "jenom v dlouhém období");
        newQuestion.put(FIRESTORE_ANSWER4, "nikdy");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma se rozhoduje o odchodu z trhu pokud");
        newQuestion.put(FIRESTORE_ANSWER1, "p = MR = MC = AC");
        newQuestion.put(FIRESTORE_ANSWER2, "p = MR = MC = AFC");
        newQuestion.put(FIRESTORE_ANSWER3, "p = MR = MC = AVC");
        newQuestion.put(FIRESTORE_ANSWER4, "MR < MC");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);


        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma je v dlouhodobé v rovnováze pokud");
        newQuestion.put(FIRESTORE_ANSWER1, "p = MR = MC = AC");
        newQuestion.put(FIRESTORE_ANSWER2, "p = MR = MC = AFC");
        newQuestion.put(FIRESTORE_ANSWER3, "p = MR = MC = AVC");
        newQuestion.put(FIRESTORE_ANSWER4, "MR < MC");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);


        newQuestion.put(FIRESTORE_QUESTION, "Alokační efektivnosti je dosaženo za předpokladu");
        newQuestion.put(FIRESTORE_ANSWER1, "p = MR = MC");
        newQuestion.put(FIRESTORE_ANSWER2, "MC = AC");
        newQuestion.put(FIRESTORE_ANSWER3, "MC = MU");
        newQuestion.put(FIRESTORE_ANSWER4, "MU = AC");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma přestává vyrábět pokud");
        newQuestion.put(FIRESTORE_ANSWER1, "je ekonomický zisk rovný nule");
        newQuestion.put(FIRESTORE_ANSWER2, "dosahuje minimálních výrobních nákladů");
        newQuestion.put(FIRESTORE_ANSWER3, "pokud má alternativu, která jí přináší stejný zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "pokud bude cena nižší než AVC");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Alokační efektivnosti (na dokonale konkurenčním trhu) je dosaženo za předpokladu");
        newQuestion.put(FIRESTORE_ANSWER1, "firma dosahuje nenulový zisk");
        newQuestion.put(FIRESTORE_ANSWER2, "vždy");
        newQuestion.put(FIRESTORE_ANSWER3, "firma dosahuje nulový zisk");
        newQuestion.put(FIRESTORE_ANSWER4, "firma je v ztrátě");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Výrobní efektivnosti je dosaženo za předpokladu");
        newQuestion.put(FIRESTORE_ANSWER1, "p = MR = MC");
        newQuestion.put(FIRESTORE_ANSWER2, "MC = AC");
        newQuestion.put(FIRESTORE_ANSWER3, "MC = MU");
        newQuestion.put(FIRESTORE_ANSWER4, "MU = AC");
        newQuestion.put(FIRESTORE_CATEGORY, "Tržní rovnováha a efektivnost");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "06");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Tržní rovnováha a efektivnost

        //Význam směny, specializace a mezinárodního obchodu

        newQuestion.put(FIRESTORE_QUESTION, "Mezinárodní obchod");
        newQuestion.put(FIRESTORE_ANSWER1, "dělá země chudšími");
        newQuestion.put(FIRESTORE_ANSWER2, "snižuje míru bohatství národu");
        newQuestion.put(FIRESTORE_ANSWER3, "je vhodný jenom pro země s absolutní výhodou");
        newQuestion.put(FIRESTORE_ANSWER4, "vede k celosvětovému zvyšování bohatství");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Dovozní kvóta");
        newQuestion.put(FIRESTORE_ANSWER1, "ochraňuje spotřebitelé");
        newQuestion.put(FIRESTORE_ANSWER2, "má vliv na cenu statku");
        newQuestion.put(FIRESTORE_ANSWER3, "zvyšuje export");
        newQuestion.put(FIRESTORE_ANSWER4, "zvyšuje množství statku na domácím trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Specializace v mezinárodním obchodě");
        newQuestion.put(FIRESTORE_ANSWER1, "v důsledku globalizace nemá význam");
        newQuestion.put(FIRESTORE_ANSWER2, "je výsledkem absolutní výhody");
        newQuestion.put(FIRESTORE_ANSWER3, "je výsledkem komparativní výhody");
        newQuestion.put(FIRESTORE_ANSWER4, "nemá vliv na výšku nákladů");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Ochranářství");
        newQuestion.put(FIRESTORE_ANSWER1, "patří do něj zejména clo a dovozní kvóty");
        newQuestion.put(FIRESTORE_ANSWER2, "je nedílnou součástí otevřených ekonomik");
        newQuestion.put(FIRESTORE_ANSWER3, "je vhodné pro tradičně odvětví");
        newQuestion.put(FIRESTORE_ANSWER4, "dlouhodobě zvyšuje efektivitu výroby");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Relativní vybavenost výrobními faktory");
        newQuestion.put(FIRESTORE_ANSWER1, "pro země s relativně drahou pracovní silou je výhodné se zaměřit na kapitálově nenáročnou výrobu");
        newQuestion.put(FIRESTORE_ANSWER2, "je předpokladem pro specializaci výroby");
        newQuestion.put(FIRESTORE_ANSWER3, "není daná geografickými a historickými podmínkami");
        newQuestion.put(FIRESTORE_ANSWER4, "vybavenost VF nemá souvis s hospodářskou politikou");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Clo");
        newQuestion.put(FIRESTORE_ANSWER1, "snižuje náklady domácích firem");
        newQuestion.put(FIRESTORE_ANSWER2, "jeho zavedením se sníží cena v odvětvích, kde zahraniční firmy neměli konkurenci");
        newQuestion.put(FIRESTORE_ANSWER3, "nemá vliv na cenu");
        newQuestion.put(FIRESTORE_ANSWER4, "pomáhá domácím nekonkurence schopným firmám");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Odstranění cla");
        newQuestion.put(FIRESTORE_ANSWER1, "zvýší příjem státu");
        newQuestion.put(FIRESTORE_ANSWER2, "zvyšuje přebytek výrobce");
        newQuestion.put(FIRESTORE_ANSWER3, "zvyšuje přebytek spotřebitele");
        newQuestion.put(FIRESTORE_ANSWER4, "nemá vliv na specializaci");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Specializace");
        newQuestion.put(FIRESTORE_ANSWER1, "pokud má jeden výrobce absolutní výhodu u obou statků, specializuje se na libovolný z nich");
        newQuestion.put(FIRESTORE_ANSWER2, "pokud má jeden výrobce absolutní výhodu u statku A a komparativní výhodu u statku B, specializuje se na B");
        newQuestion.put(FIRESTORE_ANSWER3, "nemá vliv na produktivitu práce");
        newQuestion.put(FIRESTORE_ANSWER4, "je vhodná jenom pro velké firmy");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Absolutní výhoda");
        newQuestion.put(FIRESTORE_ANSWER1, "je uplatňována v mezinárodní směně");
        newQuestion.put(FIRESTORE_ANSWER2, "platí vždy pro největší firmu na trhu");
        newQuestion.put(FIRESTORE_ANSWER3, "je daná kapitálovou vybaveností firmy");
        newQuestion.put(FIRESTORE_ANSWER4, "je definována jako schopnost prodávat statek s relativně nižšími náklady");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Komparativní výhoda");
        newQuestion.put(FIRESTORE_ANSWER1, "není možné ji specifikovat na úrovni firmy");
        newQuestion.put(FIRESTORE_ANSWER2, "je neměnná");
        newQuestion.put(FIRESTORE_ANSWER3, "je uplatňována v mezinárodní směně");
        newQuestion.put(FIRESTORE_ANSWER4, "určuje produkci ve které se vyrábí relativně víc než-li v absolutní výhodě");
        newQuestion.put(FIRESTORE_CATEGORY, "Význam směny, specializace a mezinárodního obchodu");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "07");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Význam směny, specializace a mezinárodního obchodu

        //Rovnováha nedokonale konkurenční firmy

        newQuestion.put(FIRESTORE_QUESTION, "Mezní příjem");
        newQuestion.put(FIRESTORE_ANSWER1, "je rozdíl mezi variabilními a fixními náklady");
        newQuestion.put(FIRESTORE_ANSWER2, "udává velikost účetního zisku");
        newQuestion.put(FIRESTORE_ANSWER3, "je roven průměrnému přijmu");
        newQuestion.put(FIRESTORE_ANSWER4, "udává změnu celkového příjmu z prodeje dodatečné jednotky");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Oligopol");
        newQuestion.put(FIRESTORE_ANSWER1, "je trh bez vstupních bariér");
        newQuestion.put(FIRESTORE_ANSWER2, "je firma vlastněna oligarchy");
        newQuestion.put(FIRESTORE_ANSWER3, "struktura, ve které v dlouhém období zůstává jen málo firem");
        newQuestion.put(FIRESTORE_ANSWER4, "individuální poptávka po produkci dominantní firmy není zásadní součástí tržní poptávky");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Tvorba ceny");
        newQuestion.put(FIRESTORE_ANSWER1, "je nezávislá na tržní struktuře a jejím charakteru");
        newQuestion.put(FIRESTORE_ANSWER2, "je ovlivněna individuální poptávkou");
        newQuestion.put(FIRESTORE_ANSWER3, "na nedokonalých trzích změna rozsahu produkce firmy neovlivňuje tržní cenu");
        newQuestion.put(FIRESTORE_ANSWER4, "na dokonalých trzích výrobci stanovují jakoukoliv cenu");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Cenová diskriminace");
        newQuestion.put(FIRESTORE_ANSWER1, "je strategie, při které dochází k maximalizaci zisku");
        newQuestion.put(FIRESTORE_ANSWER2, "není v reálem světe možná, protože nelze vyčlenit poptávající do dvou či více skupin");
        newQuestion.put(FIRESTORE_ANSWER3, "nelze aplikovat na nedokonalé tržní struktury");
        newQuestion.put(FIRESTORE_ANSWER4, "je situace, kdy spotřebitel ignoruje preference kvůli ceně");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Cenová strategie dominantní firmy zohledňuje");
        newQuestion.put(FIRESTORE_ANSWER1, "jenom stávající konkurenci");
        newQuestion.put(FIRESTORE_ANSWER2, "jenom náklady dané firmy");
        newQuestion.put(FIRESTORE_ANSWER3, "dosažitelnost stabilního zisku");
        newQuestion.put(FIRESTORE_ANSWER4, "relativní vybavenost VF");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Nedokonalá konkurence a alokační efektivnost");
        newQuestion.put(FIRESTORE_ANSWER1, "pro mezní pár platí MU = MC");
        newQuestion.put(FIRESTORE_ANSWER2, "pro mezního nabízejícího platí MC = AC");
        newQuestion.put(FIRESTORE_ANSWER3, "nedokonale konkurenční trh vyrábí vetší rozsah produkce, než by bylo efektivní");
        newQuestion.put(FIRESTORE_ANSWER4, "pro mezního poptávajícího platí, že MU = P");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Firma v postavení monopolistické konkurence s kladným ekonomickým ziskem");
        newQuestion.put(FIRESTORE_ANSWER1, "trpí v krátkém období nedostatečnou poptávkou, proto její zisk bude v dlouhém období 0");
        newQuestion.put(FIRESTORE_ANSWER2, "v budoucnosti bude na její trh vstupovat nová konkurence");
        newQuestion.put(FIRESTORE_ANSWER3, "je chráněná díky barierám vstupu na trh");
        newQuestion.put(FIRESTORE_ANSWER4, "za určitých podmínek je schopná udržet zisk i v dlouhém období");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud pro tržní cenu platí P = AC");
        newQuestion.put(FIRESTORE_ANSWER1, "firma dosahuje zisku, protože MC > MR");
        newQuestion.put(FIRESTORE_ANSWER2, "firma je výrobně efektivní");
        newQuestion.put(FIRESTORE_ANSWER3, "firma není v rovnováze protože MC = AVC");
        newQuestion.put(FIRESTORE_ANSWER4, "není alokačně efektivní");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Podnikatelé");
        newQuestion.put(FIRESTORE_ANSWER1, "vytváří překážky k zvyšovaní efektivity a inovacím");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou nejenom vlastníci firem");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou typicky méně efektivní než stát");
        newQuestion.put(FIRESTORE_ANSWER4, "nenesou riziko ve spojení s rozhodováním firmy");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pro firmy v postavení oligopolu a monopolistické konkurence platí");
        newQuestion.put(FIRESTORE_ANSWER1, "jsou výrobně efektivní");
        newQuestion.put(FIRESTORE_ANSWER2, "jsou alokačně efektivní");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou tu zanedbatelné bariéry vstupu na trh");
        newQuestion.put(FIRESTORE_ANSWER4, "v monopolistické konkurenci je v dlouhém období zisk rovný 0, na rozdíl od oligopolu");
        newQuestion.put(FIRESTORE_CATEGORY, "Rovnováha nedokonale konkurenční firmy");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "08");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Rovnováha nedokonale konkurenční firmy

        //Monopol

        newQuestion.put(FIRESTORE_QUESTION, "Administrativní monopol");
        newQuestion.put(FIRESTORE_ANSWER1, "je garantován zákonem či jiným rozhodnutím státu");
        newQuestion.put(FIRESTORE_ANSWER2, "je rozšířená tržní struktura");
        newQuestion.put(FIRESTORE_ANSWER3, "je definován množstvím byrokracie");
        newQuestion.put(FIRESTORE_ANSWER4, "funguje jenom v případe že na trhu působí několik firem");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Přirozený monopol");
        newQuestion.put(FIRESTORE_ANSWER1, "na rozdíl od administrativního není regulován");
        newQuestion.put(FIRESTORE_ANSWER2, "přirozené bariéry vstupu na trh nemají vliv na vznik");
        newQuestion.put(FIRESTORE_ANSWER3, "vzniká, pokud je pro vstup na trh nutno vynaložit relativně vysoké fixní náklady");
        newQuestion.put(FIRESTORE_ANSWER4, "nedosahuje zisku");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Dumping");
        newQuestion.put(FIRESTORE_ANSWER1, "je cenová politika, kde firma doprodává nadvýrobu z minulého období");
        newQuestion.put(FIRESTORE_ANSWER2, "je pokládán za neférovou obchodní praktiku");
        newQuestion.put(FIRESTORE_ANSWER3, "je nevýhodný zejména pro spotřebitele");
        newQuestion.put(FIRESTORE_ANSWER4, "nemůže být vyřešen zavedením cel (v mezinárodním obchodu)");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Regulace monopolu cenovým stropem");
        newQuestion.put(FIRESTORE_ANSWER1, "je způsob regulace");
        newQuestion.put(FIRESTORE_ANSWER2, "je za účelem udržení konkurence");
        newQuestion.put(FIRESTORE_ANSWER3, "téměř nikdy nemá vliv na vyráběné množství");
        newQuestion.put(FIRESTORE_ANSWER4, "je pouze teoretický koncept");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Náklady mrtvé váhy");
        newQuestion.put(FIRESTORE_ANSWER1, "jsou vyjádřením efektivity");
        newQuestion.put(FIRESTORE_ANSWER2, "vznikají, protože část produkce nelze z technických důvodů vyrobit");
        newQuestion.put(FIRESTORE_ANSWER3, "jsou explicitními náklady započítanými v ceně");
        newQuestion.put(FIRESTORE_ANSWER4, "je možné zmenšit regulací ceny monopolu");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Výrobní neefektivnost monopolu");
        newQuestion.put(FIRESTORE_ANSWER1, "je důsledkem existence fixních nákladů");
        newQuestion.put(FIRESTORE_ANSWER2, "znamená, že se nevyrábí s nejnižšími průměrnými náklady");
        newQuestion.put(FIRESTORE_ANSWER3, "nastává v důsledku regulace");
        newQuestion.put(FIRESTORE_ANSWER4, "nastává v důsledku existence nákladů mrtvé váhy");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Monopol může vzniknout  ");
        newQuestion.put(FIRESTORE_ANSWER1, "pokud je produkt snadno nahraditelný");
        newQuestion.put(FIRESTORE_ANSWER2, "pouze v podmínkách dokonalé konkurence");
        newQuestion.put(FIRESTORE_ANSWER3, "odchodem konkurentů z trhu");
        newQuestion.put(FIRESTORE_ANSWER4, "díky chybějící právní či administrativní restrikci");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "O monopolu obvykle platí");
        newQuestion.put(FIRESTORE_ANSWER1, "jeho cena je obvykle nižší než náklady");
        newQuestion.put(FIRESTORE_ANSWER2, "vyrábí či dodává produkt, který má velké množství substitutů");
        newQuestion.put(FIRESTORE_ANSWER3, "vyrábí s nejnižšími možnými náklady");
        newQuestion.put(FIRESTORE_ANSWER4, "vyrábí méně, než kolik by bylo alokačně efektivní");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Asymetrie informací je");
        newQuestion.put(FIRESTORE_ANSWER1, "situace, kdy prodávající nemají o výrobku tolik informací jako kupující");
        newQuestion.put(FIRESTORE_ANSWER2, "stav, kdy jeden z tržním subjektů má informační převahu");
        newQuestion.put(FIRESTORE_ANSWER3, "situace, kdy například student má o předmetu tolik informací jako učitel");
        newQuestion.put(FIRESTORE_ANSWER4, "stav, který existuje jenom na dokonale konkurenčním trhu");
        newQuestion.put(FIRESTORE_CATEGORY, "Monopol");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "09");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Monopol

        //Poptávka na trzích VF

        newQuestion.put(FIRESTORE_QUESTION, "Jak ovlivní optimální výrobní techniku nárůst ceny jednoho výrobního faktoru?\n");
        newQuestion.put(FIRESTORE_ANSWER1, "firma je motivována k omezování jeho zapojení do výroby\n");
        newQuestion.put(FIRESTORE_ANSWER2, "firma bude zvyšovat jeho zapojení do výroby, aby zvýšila jeho mezní produkt\n");
        newQuestion.put(FIRESTORE_ANSWER3, "firma bude snižovat cenu ostatních VF, dokud nebude znovu dosaženo optima\n");
        newQuestion.put(FIRESTORE_ANSWER4, "bude docházet k technické substituci ve směru od ostatních VF k tomuto danému VF\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Existence trhu výrobních faktorů\n");
        newQuestion.put(FIRESTORE_ANSWER1, "je podmínkou fungování hospodářských mechanizmů\n");
        newQuestion.put(FIRESTORE_ANSWER2, "je zdrojem nedostatečné efektivnosti tržního hospodářství\n");
        newQuestion.put(FIRESTORE_ANSWER3, "je předpokladem fungování tržního mechanismu\n");
        newQuestion.put(FIRESTORE_ANSWER4, "ovlivňuje situaci na trhu výrobků a služeb\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Trh výrobních faktorů\n");
        newQuestion.put(FIRESTORE_ANSWER1, "dochází zde k usměrňování zdrojů vůči potřebám\n");
        newQuestion.put(FIRESTORE_ANSWER2, "obchodují se zde služby výrobních faktorů a výrobní faktory samotné\n");
        newQuestion.put(FIRESTORE_ANSWER3, "zahrnuje primární trhy\n");
        newQuestion.put(FIRESTORE_ANSWER4, "zahrnuje terciárni trhy\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokles individuální poptávky po práci může být způsoben\n");
        newQuestion.put(FIRESTORE_ANSWER1, "poklesem ceny tohoto výrobního faktoru\n");
        newQuestion.put(FIRESTORE_ANSWER2, "poklesem produktivity pracovníků\n");
        newQuestion.put(FIRESTORE_ANSWER3, "zvýšením kapitálové vybavenosti práce\n");
        newQuestion.put(FIRESTORE_ANSWER4, "zvýšením poptávky po daném finálním produktu\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Technický pokrok");
        newQuestion.put(FIRESTORE_ANSWER1, "podněcuje ceny výrobních faktorů");
        newQuestion.put(FIRESTORE_ANSWER2, "spočívá v přeskupení výrobních faktorů ve výrobě");
        newQuestion.put(FIRESTORE_ANSWER3, "je spojen s krátkým i dlouhým obdobím");
        newQuestion.put(FIRESTORE_ANSWER4, "umožňuje nahrazovat drahé výrobní faktory levnějšími");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Specifický výrobní faktor");
        newQuestion.put(FIRESTORE_ANSWER1, "je konkrétní spotřebí statek");
        newQuestion.put(FIRESTORE_ANSWER2, "je najímán domácnostmi za nájemní cenu");
        newQuestion.put(FIRESTORE_ANSWER3, "je konkrétní pracovní profese");
        newQuestion.put(FIRESTORE_ANSWER4, "má specifický tvar MRP, odlišný od běžného výrobního faktoru");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Příjem z mezního produktu");
        newQuestion.put(FIRESTORE_ANSWER1, "je klesající");
        newQuestion.put(FIRESTORE_ANSWER2, "k jeho určení je nutno znát cenu výrobního faktoru");
        newQuestion.put(FIRESTORE_ANSWER3, "nemůže nabývat záporných hodnot");
        newQuestion.put(FIRESTORE_ANSWER4, "představuje mezní produkt vyjádřený v peněžních jednotkách");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Technická substituce");
        newQuestion.put(FIRESTORE_ANSWER1, "probíhá v krátkém i dlouhém období\n");
        newQuestion.put(FIRESTORE_ANSWER2, "je proces nahrazování jednotlivých VF\n");
        newQuestion.put(FIRESTORE_ANSWER3, "při provádění porovnává firma mezní náklady a cenu VF\n");
        newQuestion.put(FIRESTORE_ANSWER4, "změna ceny VF nemá vliv\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Výrobní faktor");
        newQuestion.put(FIRESTORE_ANSWER1, "reálně existuje jen v podobě specifického výrobního faktoru");
        newQuestion.put(FIRESTORE_ANSWER2, "si výrobce najme i v případě, když ARP výrobního faktoru je menší než nájemní cena výrobního faktoru");
        newQuestion.put(FIRESTORE_ANSWER3, "si výrobce najme v případě, když MRP výrobního faktoru je menší než nájemní cena výrobního faktoru");
        newQuestion.put(FIRESTORE_ANSWER4, "si výrobce najímá, pokud nedosahuje ekonomický zisk");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Optimální množství výrobního faktoru firma stanovuje na základě");
        newQuestion.put(FIRESTORE_ANSWER1, "průměrných příjmů firmy s průměrnými náklady firmy");
        newQuestion.put(FIRESTORE_ANSWER2, "příjmu z mezního a příjmu z průměrného produktu");
        newQuestion.put(FIRESTORE_ANSWER3, "příjmu z mezního produktu VF a nákladu na dodatečnou jednotku výrobního faktoru");
        newQuestion.put(FIRESTORE_ANSWER4, "příjmu z průměrného produktu a celkových nákladů firmy\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Poptávka na trzích VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "10");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end Poptávka na trzích VF

        //Nabídka VF a trh VF

        newQuestion.put(FIRESTORE_QUESTION, "Nominální mzda\n");
        newQuestion.put(FIRESTORE_ANSWER1, "ovlivňuje domácnosti v jejich rozhodování\n");
        newQuestion.put(FIRESTORE_ANSWER2, "oproti reálné mzdě lépe zobrazuje změny životní úrovně\n");
        newQuestion.put(FIRESTORE_ANSWER3, "její meziroční změna se nikdy nemůže rovnat meziroční změně reálné mzdy\n");
        newQuestion.put(FIRESTORE_ANSWER4, "díky růstu cen obvykle roste rychleji než mzda reálná\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Tržní nabídka práce\n");
        newQuestion.put(FIRESTORE_ANSWER1, "má odlišný tvar než individuální nabídka práce\n");
        newQuestion.put(FIRESTORE_ANSWER2, "zobrazuje počet firem nabízejících práci při různých úrovních mzdy\n");
        newQuestion.put(FIRESTORE_ANSWER3, "zobrazuje upřednostnění volného času při dosažení určité mzdy\n");
        newQuestion.put(FIRESTORE_ANSWER4, "má specifický tvar, který je od určitého bodu zalomen zpět\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Reálná mzda\n");
        newQuestion.put(FIRESTORE_ANSWER1, "díky růstu cen obvykle roste rychleji než nominální mzda\n");
        newQuestion.put(FIRESTORE_ANSWER2, "je klesající s rostoucí cenovou hladinou a je konstantní s rostoucí nominální mzdou\n");
        newQuestion.put(FIRESTORE_ANSWER3, "není ovlivněna nominální mzdou\n");
        newQuestion.put(FIRESTORE_ANSWER4, "její meziroční změna se nikdy nemůže rovnat meziroční změně nominální mzdy\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Individuální nabídka práce\n");
        newQuestion.put(FIRESTORE_ANSWER1, "je nabídkou práce jedné firmy\n");
        newQuestion.put(FIRESTORE_ANSWER2, "nejprve klesá a poté roste v důsledku převažujícího důchodového efektu\n");
        newQuestion.put(FIRESTORE_ANSWER3, "má stejný tvar než tržní nabídka práce\n");
        newQuestion.put(FIRESTORE_ANSWER4, "zobrazuje rozhodování nabízejícího o rozdělení času mezi práci a volný čas\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 4);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Kompenzující mzdové rozdíly\n");
        newQuestion.put(FIRESTORE_ANSWER1, "kompenzují ekonomické i neekonomické rozdíly v charakteru práce\n");
        newQuestion.put(FIRESTORE_ANSWER2, "mají pouze krátkodobý charakter \n");
        newQuestion.put(FIRESTORE_ANSWER3, "nejsou způsobeny změnou nabídky\n");
        newQuestion.put(FIRESTORE_ANSWER4, "nemohou mít dlouhodobý charakter\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 3);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Produktivita práce\n");
        newQuestion.put(FIRESTORE_ANSWER1, "je ovlivněna kapitálovou vybaveností práce\n");
        newQuestion.put(FIRESTORE_ANSWER2, "je dána součtem všech funkcí mezní produktivity práce\n");
        newQuestion.put(FIRESTORE_ANSWER3, "lze ji zvýšit jen krátkodobě\n");
        newQuestion.put(FIRESTORE_ANSWER4, "nelze ji zvýšit investicemi do lidského kapitálu\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 1);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        newQuestion.put(FIRESTORE_QUESTION, "Pokud dochází k migraci pracovníků z tuzemska do zahraničí\n");
        newQuestion.put(FIRESTORE_ANSWER1, "udeme v zahraničí moci nejprve pozorovat růst poptávky po práci");
        newQuestion.put(FIRESTORE_ANSWER2, "dojde v tuzemsku ke snížení tržní nabídky práce\n");
        newQuestion.put(FIRESTORE_ANSWER3, "je pravděpodobné, že je v tuzemsku relativně vyšší produktivita práce\n");
        newQuestion.put(FIRESTORE_ANSWER4, "pravděpodobně jsou tuzemské mzdy vyšší než zahraniční\n");
        newQuestion.put(FIRESTORE_CATEGORY, "Nabídka VF a trh VF");
        newQuestion.put(FIRESTORE_CATEGORY_ID, "11");
        newQuestion.put(FIRESTORE_SUBJECT, "MI1");
        newQuestion.put(FIRESTORE_CORRECT_ANSWER, 2);

        addQuestionToFirestore(newQuestion);
        newQuestion.clear();
        question++;
        Log.d(TAG, "generateQustions: " + question);

        //end abídka VF a trh VF
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

    public void addQuizAnswered(Integer pointsAcquired, Integer questionsAnswered) {
        Log.d(TAG, "addQuizAnswered: pointsAcquired[" + pointsAcquired + "] questionsAnswered[" + questionsAnswered + "]");
        ContentValues insertedValue = new ContentValues();

        insertedValue.put(QuizAnswerTable.COLUMN_TIMETAG, getTimeNow());
        insertedValue.put(QuizAnswerTable.COLUMN_POINTS_ACQUIRED, pointsAcquired);
        insertedValue.put(QuizAnswerTable.COLUMN_QUESTIONS_ANSWERED, questionsAnswered);

        if (db == null) {
            Log.e(TAG, "addQuizAnswered db == null");
            return;
        }
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
        if (!databaseInitDone) {
            return score;
        }

        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + QuizAnswerTable.COLUMN_POINTS_ACQUIRED + ") as Total FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            score = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        return score;
    }

    public int getAvailablePoints() {
        int usedPoints = 0, availablePoints = 0;

        if (!databaseInitDone) {
            return availablePoints;
        }
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + CategoryTable.COLUMN_UNLOCKED + ") as Total FROM " + CategoryTable.CATEGORY_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            usedPoints = cursor.getInt(cursor.getColumnIndex("Total"));
        }

        cursor.close();

        availablePoints = getScore() - usedPoints;
        if (availablePoints != lastScore) {
            lastScore = availablePoints;
            updateUserStats();
        }
        return availablePoints;
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
                boolean notLocked = cursor.getType(cursor.getColumnIndex(CategoryTable.COLUMN_UNLOCKED)) == FIELD_TYPE_NULL;
                String categoryName = cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_CATEGORY));
                if (notLocked) {
                    Integer price = (list.size() + 1) * 75;
                    Log.d(TAG, "category [" + categoryName + "] added with price[" + price + "]");
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(categoryName);
                    temp.add(price.toString());
                    temp.add(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_FIRESTORE_ID)));
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
        Cursor cursor = db.rawQuery("SELECT COUNT(" + CategoryTable.COLUMN_UNLOCKED + ") as Total FROM " + CategoryTable.CATEGORY_TABLE_NAME /*+ " WHERE " + CategoryTable.COLUMN_UNLOCKED + "<> ? ", new String[]{0 + ""}*/, null);

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

    public int getNumUnlockedQuestions() {
        Log.d(TAG, "getNumUnlockedQuestions");
        int retVal = 0;
        db = getReadableDatabase();

        for (List<String> listEntry : getAllUnlockableCategories()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) as Total FROM " + QuestionsTable.QUIZ_TABLE_NAME + " WHERE " + QuestionsTable.COLUMN_CATEGORY_ID, null);
            if (cursor.moveToFirst()) {
                retVal += cursor.getInt(cursor.getColumnIndex("Total"));
            }

            cursor.close();
        }

        Log.d(TAG, "getNumUnlockedQuestions: return " + retVal);
        return retVal;
    }

    public Integer getHighPointsScore() {
        Log.d(TAG, "getHighPointsScore");
        int retVal = 0;
        if (!databaseInitDone) {
            return retVal;
        }
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + QuizAnswerTable.COLUMN_POINTS_ACQUIRED + " ) as Score FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Score"));
        }

        cursor.close();

        Log.d(TAG, "getHighPointsScore: return " + retVal);
        return retVal;
    }

    public Integer getHighAnswersScoreStreak() {
        Log.d(TAG, "getHighAnswersScoreStreak");
        int retVal = 0;
        if (!databaseInitDone) {
            return retVal;
        }
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + QuizAnswerTable.COLUMN_QUESTIONS_ANSWERED + " ) as Score FROM " + QuizAnswerTable.ANSWERS_TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            retVal = cursor.getInt(cursor.getColumnIndex("Score"));
        }

        cursor.close();

        Log.d(TAG, "getHighAnswersScoreStreak: return " + retVal);
        return retVal;
    }

    public void createUserRef(String userID, String mail) {
        Log.d(TAG, "createUserRef[" + userID + "][" + mail + "]");
        usersRef = onlineDatabase.collection(QuizContract.COLLECTION_NAME_USERS).document(userID);
        usersRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    Log.i(TAG, "createUserRef - userRef created");
                    if (getScore() == 0) {
                        Log.i(TAG, "createUserRef - initialUserDataRead called");
                        initialUserDataRead(documentSnapshot);
                        observable.updateUI();
                        userSuccesfullyLogged = true;
                    }
                } else {
                    Log.d(TAG, "createUserRef - creating new user");
                    Map<String, Object> newUser = new HashMap<>();
                    newUser.put(FIRESTORE_USER_MAIL, mail);
                    newUser.put(FIRESTORE_USER_ID, userID);
                    newUser.put(FIRESTORE_USER_POINTS, 0);
                    newUser.put(FIRESTORE_USER_ANSWERS_STREAK, 0);
                    newUser.put(FIRESTORE_USER_POINTS_STREAK, 0);
                    //                    onlineDatabase.collection(QuizContract.COLLECTION_NAME_USERS).add(newUser)
                    onlineDatabase.collection(QuizContract.COLLECTION_NAME_USERS).document(userID).set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "createUserRef - new user created");
                            createUserRef(userID, mail);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    private void initialUserDataRead(DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "initialUserDataRead");
        int userPoints = documentSnapshot.getDouble(FIRESTORE_USER_POINTS).intValue();
        int userHighScorePoints = documentSnapshot.getDouble(FIRESTORE_USER_POINTS_STREAK).intValue();
        int userHighScoreAnswers = documentSnapshot.getDouble(FIRESTORE_USER_ANSWERS_STREAK).intValue();
        Log.d(TAG, "initialUserDataRead userPoints[" + userPoints + "]");
        Log.d(TAG, "initialUserDataRead userHighScorePoints[" + userHighScorePoints + "]");
        Log.d(TAG, "initialUserDataRead userHighScoreAnswers[" + userHighScoreAnswers + "]");

        if (userPoints > userHighScorePoints) {
            int counter = userPoints / userHighScorePoints;
            int leftOver = userPoints % userHighScorePoints;
            Log.d(TAG, "initialUserDataRead counter[" + counter + "] leftOver[" + leftOver + "]");
            for (int i = 0; i < counter; i++) {
                addQuizAnswered(userHighScorePoints, userHighScoreAnswers);
            }
            addQuizAnswered(leftOver, userHighScoreAnswers);
        } else {
            addQuizAnswered(userHighScorePoints, userHighScoreAnswers);
        }
    }

    public void updateUserStats() {
        Log.d(TAG, "updateUserStats start");
        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put(FIRESTORE_USER_POINTS, getScore());
        updatedUser.put(FIRESTORE_USER_POINTS_STREAK, getHighPointsScore());
        updatedUser.put(FIRESTORE_USER_ANSWERS_STREAK, getHighAnswersScoreStreak());

        if (usersRef == null) {
            Log.e(TAG, "updateUserStats usersRef == null");
            return;
        }
        if (!userSuccesfullyLogged) {
            Log.d(TAG, "updateUserStats userSuccesfullyLogged == false");
            return;
        }

        usersRef.update(updatedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "updateUserStats User stats updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    public void onLogout() {
        Log.d(TAG, "onLogout");
        usersRef = null;
        userSuccesfullyLogged = false;

        //refresh local database
        destroyDBTables();
        createDBTables();

        getQuizQuestionsFromFirestore();
        getQuizCategoriesFromFirestore();
    }

    private void createDBTables() {
        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.QUIZ_TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION4 + " TEXT, " +
                QuestionsTable.COLUMN_CATEGORY + " TEXT, " +
                QuestionsTable.COLUMN_CATEGORY_ID + " TEXT, " +
                QuestionsTable.COLUMN_SUBJECT + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_ID + " INTEGER, " +
                QuestionsTable.COLUMN_ANSWERED + " INTEGER, " +
                QuestionsTable.COLUMN_FIRESTORE_ID + " TEXT " +
                ")";

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                CategoryTable.CATEGORY_TABLE_NAME + " ( " +
                CategoryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoryTable.COLUMN_CATEGORY + " TEXT, " +
                CategoryTable.COLUMN_UNLOCKED + " INTEGER, " +
                CategoryTable.COLUMN_FIRESTORE_ID + " TEXT " +
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
    }

    private void destroyDBTables() {
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.QUIZ_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.CATEGORY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuizAnswerTable.ANSWERS_TABLE_NAME);
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

}
