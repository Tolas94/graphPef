package cz.mendelu.tomas.graphpef.helperObjects;

public class QuizQuestion {
    private String question;
    public String firestoreID;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String category;
    private int correctAnswerId;
    private String questionID;
    private boolean answered;
    private String subject;
    private String categoryId;

    public QuizQuestion() {
    }

    public QuizQuestion(String question, String firestoreID, String option1, String option2, String option3, String option4, String category, int correctAnswerId, String subject, String categoryId) {
        this.question = question;
        this.firestoreID = firestoreID;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.category = category;
        this.correctAnswerId = correctAnswerId;
        this.subject = subject;
        this.categoryId = categoryId;
        answered = false;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFirestoreID() {
        return firestoreID;
    }

    public void setFirestoreID(String firestoreID) {
        this.firestoreID = firestoreID;
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public int getCorrectAnswerId() {
        return correctAnswerId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public void setCorrectAnswerId(int correctAnswerId) {
        this.correctAnswerId = correctAnswerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
}
