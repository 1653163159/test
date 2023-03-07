package com.example.test.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.pojo.Subject;
import com.example.test.pojo.SubjectAnswer;

import java.util.List;

/**
 * @author : hqx
 * @date : 15/2/2023 下午 1:26
 * @descriptions:
 */
public class SubjectListViewAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<Subject> items;
    final int TYPE_Choice4 = 0, TYPE_Materia = 1, TYPE_Choice75 = 2, TYPE_composition = 3;
    SubjectAnswer[] answersSentence;
    SubjectAnswer[] answersAlphabet;

    public SubjectListViewAdapter(Context context, List<Subject> items) {
        this.items = items;
        inflater = LayoutInflater.from(context);
        answersSentence = new SubjectAnswer[110];
        answersAlphabet = new SubjectAnswer[110];
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).getType().equals("hearing_choice4") || items.get(position).getType().equals("read_choice4")) {
            return TYPE_Choice4;
        } else if (items.get(position).getType().equals("read_choice75_materia") || items.get(position).getType().equals("read_choice4_materia") || items.get(position).getType().equals("hearing_materia") || items.get(position).getType().equals("composition")) {
            return TYPE_Materia;
        } else if (items.get(position).getType().equals("read_choice75")) {
            return TYPE_Choice75;
        } else {
            return -1;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        if (type == TYPE_Choice4) {
            ViewHolder1 viewHolder1 = new ViewHolder1();
            convertView = inflater.inflate(R.layout.choice4, parent, false);
            viewHolder1.relativeLayout = convertView.findViewById(R.id.hear);
            viewHolder1.number = convertView.findViewById(R.id.subject_number);
            viewHolder1.content = convertView.findViewById(R.id.subject_content);
            viewHolder1.group = convertView.findViewById(R.id.subject_answer_group);
            View finalConvertView = convertView;
            viewHolder1.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    for (int i = 0; i < viewHolder1.group.getChildCount(); i++) {
                        RadioButton button = (RadioButton) viewHolder1.group.getChildAt(i);
                        button.setButtonDrawable(R.drawable.check_circle_default);
                    }
                    RadioButton button = finalConvertView.findViewById(viewHolder1.group.getCheckedRadioButtonId());
                    button.setButtonDrawable(R.drawable.check_circle);
                    int index = viewHolder1.group.indexOfChild(button);
                    int lo = Integer.valueOf(items.get(position).getNumber());
                    answersAlphabet[lo] = new SubjectAnswer(items.get(position).getNumber(), intToAlphabet(index + 1));
                    answersSentence[lo] = new SubjectAnswer(items.get(position).getNumber(), (String) button.getText());
                    System.out.println(lo + "{number:" + items.get(position).getNumber() + ";answer:" + intToAlphabet(index + 1));
                }
            });
            viewHolder1.a = convertView.findViewById(R.id.sub_ans_a);
            viewHolder1.b = convertView.findViewById(R.id.sub_ans_b);
            viewHolder1.c = convertView.findViewById(R.id.sub_ans_c);
            viewHolder1.d = convertView.findViewById(R.id.sub_ans_d);
            viewHolder1.number.setText(items.get(position).getNumber() + ". ");
            viewHolder1.content.setText(items.get(position).getContent());
            viewHolder1.a.setText(items.get(position).getOptionA());
            viewHolder1.b.setText(items.get(position).getOptionB());
            viewHolder1.c.setText(items.get(position).getOptionC());
            viewHolder1.d.setText(items.get(position).getOptionD());
            int lo = Integer.valueOf(items.get(position).getNumber());
            //listview会自动刷新控件，所以需要重现答题结果
            if (answersAlphabet[lo] != null) {
                int index = alphabetToInt(answersAlphabet[lo].getAnswer()) - 1;
                RadioButton button = (RadioButton) viewHolder1.group.getChildAt(index);
                viewHolder1.group.check(button.getId());
            }
        } else if (type == TYPE_Materia) {
            ViewHolder2 viewHolder2 = new ViewHolder2();
            convertView = inflater.inflate(R.layout.choice_materia, parent, false);
            viewHolder2.number = convertView.findViewById(R.id.subject_numbers);
            viewHolder2.content = convertView.findViewById(R.id.subject_materia);
            if (items.get(position).getType().equals("composition")) {
                viewHolder2.number.setText("写作不进行统计");
                viewHolder2.number.setTextColor(Color.RED);
            } else if (items.get(position).getType().equals("hearing_materia")) {
                viewHolder2.number.setText(items.get(position).getNumber() + ". ");
            } else {
                viewHolder2.number.setText(items.get(position).getNumber() + ". 请阅读材料选择正确选项");
            }
            viewHolder2.content.setText(items.get(position).getContent());
        } else if (type == TYPE_Choice75) {
            ViewHolder3 viewHolder3 = new ViewHolder3();
            convertView = inflater.inflate(R.layout.choice75, parent, false);
            viewHolder3.number = convertView.findViewById(R.id.subject75_number);
            viewHolder3.content = convertView.findViewById(R.id.subject75_content);
            viewHolder3.group = convertView.findViewById(R.id.subject75_answer_group);
            View finalConvertView1 = convertView;
            viewHolder3.group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    for (int i = 0; i < viewHolder3.group.getChildCount(); i++) {
                        RadioButton button = (RadioButton) viewHolder3.group.getChildAt(i);
                        button.setButtonDrawable(R.drawable.check_circle_default);
                    }
                    RadioButton button = finalConvertView1.findViewById(viewHolder3.group.getCheckedRadioButtonId());
                    button.setButtonDrawable(R.drawable.check_circle);
                    int index = viewHolder3.group.indexOfChild(button);
                    int lo = Integer.valueOf(items.get(position).getNumber());
                    answersAlphabet[lo] = new SubjectAnswer(items.get(position).getNumber(), intToAlphabet(index + 1));
                    answersSentence[lo] = new SubjectAnswer(items.get(position).getNumber(), (String) button.getText());
                }
            });
            viewHolder3.a = convertView.findViewById(R.id.sub75_ans_a);
            viewHolder3.b = convertView.findViewById(R.id.sub75_ans_b);
            viewHolder3.c = convertView.findViewById(R.id.sub75_ans_c);
            viewHolder3.d = convertView.findViewById(R.id.sub75_ans_d);
            viewHolder3.e = convertView.findViewById(R.id.sub75_ans_e);
            viewHolder3.number.setText(items.get(position).getNumber() + ". ");
            viewHolder3.content.setText(items.get(position).getContent());
            viewHolder3.a.setText(items.get(position).getOptionA());
            viewHolder3.b.setText(items.get(position).getOptionB());
            viewHolder3.c.setText(items.get(position).getOptionC());
            viewHolder3.d.setText(items.get(position).getOptionD().split("E")[0]);
            viewHolder3.e.setText(items.get(position).getOptionD().split("E")[1]);
            int lo = Integer.valueOf(items.get(position).getNumber());
            if (answersAlphabet[lo] != null) {
                int index = alphabetToInt(answersAlphabet[lo].getAnswer()) - 1;
                RadioButton button = (RadioButton) viewHolder3.group.getChildAt(index);
                viewHolder3.group.check(button.getId());
            }
        }
        return convertView;
    }

    String intToAlphabet(int i) {
        switch (i) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "C";
            case 4:
                return "D";
            case 5:
                return "E";
            case 6:
                return "F";
            default:
                return null;
        }
    }

    int alphabetToInt(String s) {
        switch (s) {
            case "A":
                return 1;
            case "B":
                return 2;
            case "C":
                return 3;
            case "D":
                return 4;
            case "E":
                return 5;
            case "F":
                return 6;
            default:
                return -1;
        }
    }

    public SubjectAnswer[] getAnswersAlphabet() {
        return answersAlphabet;
    }

    public SubjectAnswer[] getAnswersSentence() {
        return answersSentence;
    }

    /**
     * 4选1
     */
    static class ViewHolder1 {
        RelativeLayout relativeLayout;
        TextView number, content;
        RadioGroup group;
        RadioButton a, b, c, d;
    }

    /**
     * 题目材料
     */
    static class ViewHolder2 {
        TextView number, content;
    }

    static class ViewHolder3 {
        TextView number, content;
        RadioGroup group;
        RadioButton a, b, c, d, e;
    }
}
