package com.github.sohe1l.inspiremealarmclock.ui.alarm;

import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

class TextHighlighter extends AsyncTask<Void, Spannable, Void> {
    private final static String TAG = "TextHighlighter";
    private static final int targetPercent = 85;
    private final Set<Integer> lettersSaid = new HashSet<>();

    private final TextHighlighterCallback callback;

    private final String textLC;
    private final WeakReference<TextView> textView;

    private final ArrayList<String> wordsChecked = new ArrayList<>();
    private final Queue<String> wordsToCheck = new LinkedList<>();

    private final Spannable spannable;

    private final int highlightColor;

    TextHighlighter(TextView tv, String s, int highlightColor, TextHighlighterCallback callback) {
        Log.wtf(TAG, "Started - q : " + s);
        this.textView = new WeakReference<>(tv);
        spannable = new SpannableString(s);
        textLC = s.toLowerCase();
        this.callback = callback;
        this.highlightColor = highlightColor;
    }

    public void checkWords(String words){
        Log.wtf("WORDS", words);
        if(words.equals("")) return;
        String[] wordsArr = words.split("\\s");
        wordsToCheck.addAll(Arrays.asList(wordsArr));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.onHighlightingDone();
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        while(true){

            while(wordsToCheck.isEmpty()){
                if (isCancelled()) break;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String s = wordsToCheck.poll().toLowerCase();


            // make sure to check each word only once
            if(wordsChecked.contains(s)){
                continue;
            }else{
                wordsChecked.add(s);
            }

            int index = textLC.indexOf(s);
            while(index != -1){
                for(int j = index; j<=index+s.length(); j++){
                    lettersSaid.add(j);
                }
                spannable.setSpan(new ForegroundColorSpan(highlightColor), index, index+s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                publishProgress(spannable);
                int start = index+s.length() ;
                index = textLC.indexOf(s, start);
            }

            if(  (lettersSaid.size()  * 100) / textLC.length() > targetPercent   ){
                break;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Spannable... values) {
        super.onProgressUpdate(values);
        textView.get().setText(values[0]);
    }

    public interface TextHighlighterCallback{
        void onHighlightingDone();
    }
}

