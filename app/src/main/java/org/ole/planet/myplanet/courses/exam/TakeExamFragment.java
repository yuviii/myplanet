package org.ole.planet.myplanet.courses.exam;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.ole.planet.myplanet.Data.realm_UserModel;
import org.ole.planet.myplanet.Data.realm_answerChoices;
import org.ole.planet.myplanet.Data.realm_examQuestion;
import org.ole.planet.myplanet.Data.realm_answer;
import org.ole.planet.myplanet.Data.realm_stepExam;
import org.ole.planet.myplanet.Data.realm_submissions;
import org.ole.planet.myplanet.R;
import org.ole.planet.myplanet.datamanager.DatabaseService;
import org.ole.planet.myplanet.userprofile.UserProfileDbHandler;
import org.ole.planet.myplanet.utilities.Utilities;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.internal.Util;


/**
 * A simple {@link Fragment} subclass.
 */
public class TakeExamFragment extends BaseExamFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    TextView tvQuestionCount, header, body;
    EditText etAnswer;
    Button btnSubmit;
    RadioGroup listChoices;
    LinearLayout llCheckbox;

    NestedScrollView container;

    public TakeExamFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_take_exam, parent, false);
        listAns = new HashMap<>();
        tvQuestionCount = view.findViewById(R.id.tv_question_count);
        header = view.findViewById(R.id.tv_header);
        body = view.findViewById(R.id.tv_body);
        llCheckbox = view.findViewById(R.id.ll_checkbox);
        etAnswer = view.findViewById(R.id.et_answer);
        btnSubmit = view.findViewById(R.id.btn_submit);
        listChoices = view.findViewById(R.id.group_choices);
        container = view.findViewById(R.id.container);
        db = new DatabaseService(getActivity());
        mRealm = db.getRealmInstance();
        UserProfileDbHandler dbHandler = new UserProfileDbHandler(getActivity());
        user = mRealm.copyFromRealm(dbHandler.getUserModel());
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initExam();
        questions = mRealm.where(realm_examQuestion.class).equalTo("examId", exam.getId()).findAll();
        tvQuestionCount.setText("Question : 1/" + questions.size());
        sub = mRealm.where(realm_submissions.class)
                .equalTo("userId", user.getId())
                .equalTo("parentId", exam.getId())
                .sort("date", Sort.DESCENDING)
                .findFirst();
        if (questions.size() > 0) {
            createSubmission();
            startExam(questions.get(currentIndex));
        } else {
            container.setVisibility(View.GONE);
            Snackbar.make(tvQuestionCount, "No questions available", Snackbar.LENGTH_LONG).show();
        }
    }


    private void createSubmission() {
        startTransaction();
        if (sub == null || questions.size() == sub.getAnswers().size())
            sub = mRealm.createObject(realm_submissions.class, UUID.randomUUID().toString());
        sub.setParentId(exam.getId());
        sub.setUserId(user.getId());
        sub.setType(type);
        sub.setDate(new Date().getTime());
        if (sub.getAnswers() != null) {
            currentIndex = sub.getAnswers().size();
        }
        mRealm.commitTransaction();
    }

    @Override
    public void startExam(realm_examQuestion question) {
        tvQuestionCount.setText("Question : " + (currentIndex + 1) + "/" + questions.size());
        if (currentIndex == questions.size() - 1) {
            btnSubmit.setText("Finish");
        }
        listChoices.removeAllViews();
        llCheckbox.removeAllViews();
        etAnswer.setVisibility(View.GONE);
        listChoices.setVisibility(View.GONE);
        llCheckbox.setVisibility(View.GONE);

        if (question.getType().equalsIgnoreCase("select")) {
            listChoices.setVisibility(View.VISIBLE);
            selectQuestion(question);
        } else if (question.getType().equalsIgnoreCase("input")) {
            etAnswer.setVisibility(View.VISIBLE);
        } else if (question.getType().equalsIgnoreCase("selectMultiple")) {
            Utilities.log("Select multiple");
            llCheckbox.setVisibility(View.VISIBLE);
            showCheckBoxes(question);
        }
        etAnswer.setText("");
        ans = "";
        header.setText(question.getHeader());
        body.setText(question.getBody());
        btnSubmit.setOnClickListener(this);
    }


    private void showCheckBoxes(realm_examQuestion question) {
        RealmResults<realm_answerChoices> choices = mRealm.where(realm_answerChoices.class)
                .equalTo("questionId", question.getId()).findAll();
        for (int i = 0; i < choices.size(); i++) {
            addCheckBoxes(choices.get(i));
        }
    }

    private void selectQuestion(realm_examQuestion question) {
        if (question.getChoices().size() > 0) {
            radioWithOutCorrectChoice(question);
        } else {
            radioWithCorrectChoice(question);
        }
    }


    private void radioWithCorrectChoice(realm_examQuestion question) {
        RealmResults<realm_answerChoices> choices = mRealm.where(realm_answerChoices.class)
                .equalTo("questionId", question.getId()).findAll();
        for (int i = 0; i < choices.size(); i++) {
            addRadioButton(choices.get(i).getText());
        }
    }

    private void radioWithOutCorrectChoice(realm_examQuestion question) {
        RealmList choices = question.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            addRadioButton(choices.get(i).toString());
        }
    }

    public void addRadioButton(String choice) {
        RadioButton rdBtn = (RadioButton) LayoutInflater.from(getActivity()).inflate(R.layout.item_radio_btn, null);
        rdBtn.setText(choice);
        rdBtn.setOnCheckedChangeListener(this);
        listChoices.addView(rdBtn);
    }

    public void addCheckBoxes(realm_answerChoices choice) {
        CheckBox rdBtn = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.item_checkbox, null);
        rdBtn.setText(choice.getText());
        rdBtn.setTag(choice);
        rdBtn.setOnCheckedChangeListener(this);
        llCheckbox.addView(rdBtn);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {
            String type = questions.get(currentIndex).getType();
            if (type.equalsIgnoreCase("input")) {
                ans = etAnswer.getText().toString();
            }
            if (showErrorMessage("Please select / write your answer to continue")) {
                return;
            }
            boolean cont = updateAnsDb();
            checkAnsAndContinue(cont);
        }
    }


    private boolean updateAnsDb() {
        boolean flag;
        startTransaction();
        sub.setStatus(currentIndex == questions.size() - 1 ? "graded" : "pending");
        RealmList<realm_answer> list = sub.getAnswers();
        realm_answer answer = mRealm.copyFromRealm(createAnswer(list));
        realm_examQuestion que = mRealm.copyFromRealm(questions.get(currentIndex));
        answer.setQuestionId(que.getId());
        answer.setValue(ans);
        answer.setValueChoices(listAns);
        answer.setSubmissionId(sub.getId());
        if (TextUtils.isEmpty(que.getCorrectChoice())) {
            answer.setGrade(0);
            answer.setMistakes(0);
            flag = true;
        } else {
            flag = checkCorrectAns(answer, que);
        }
        list.add(currentIndex, answer);
        sub.setAnswers(list);
        mRealm.commitTransaction();
        return flag;
    }

    private boolean checkCorrectAns(realm_answer answer, realm_examQuestion que) {
        boolean flag = false;
        answer.setPassed(que.getCorrectChoice().equalsIgnoreCase(ans));
        answer.setGrade(1);
        int mistake = answer.getMistakes();
        if (que.getCorrectChoice().equalsIgnoreCase(ans)) {
            mistake++;
        } else {
            flag = true;
        }
        answer.setMistakes(mistake);
        return flag;
    }

    private void startTransaction() {
        if (!mRealm.isInTransaction()) {
            mRealm.beginTransaction();
        }
    }

    private realm_answer createAnswer(RealmList<realm_answer> list) {
        realm_answer answer;
        if (list == null) {
            list = new RealmList<>();
        }
        if (list.size() > currentIndex) {
            answer = list.get(currentIndex);
        } else {
            answer = mRealm.createObject(realm_answer.class, UUID.randomUUID().toString());
        }
        return answer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            addAnswer(compoundButton);
        } else if (compoundButton.getTag() != null) {
            listAns.remove(compoundButton.getText().toString());
        }
    }

}
