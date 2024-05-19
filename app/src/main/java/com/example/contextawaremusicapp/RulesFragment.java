package com.example.contextawaremusicapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

public class RulesFragment extends Fragment {

    private static final String PREFS_NAME = "SpotifyPrefs";
    private static final String KEY_RULES = "rules";

    private Spinner contextSpinner;
    private Spinner musicSpinner;
    private LinearLayout rulesListContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules, container, false);

        contextSpinner = view.findViewById(R.id.contextSpinner);
        musicSpinner = view.findViewById(R.id.musicSpinner);
        rulesListContainer = view.findViewById(R.id.rulesListContainer);
        ImageButton btnAddRule = view.findViewById(R.id.btnAddRule);

        btnAddRule.setOnClickListener(v -> addRule());

        loadRules();
        return view;
    }

    private void addRule() {
        String context = contextSpinner.getSelectedItem().toString();
        String music = musicSpinner.getSelectedItem().toString();
        String rule = context + "|" + music;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> rules = sharedPreferences.getStringSet(KEY_RULES, new HashSet<>());
        rules.add(rule);
        sharedPreferences.edit().putStringSet(KEY_RULES, rules).apply();

        addRuleToView(rule);
    }

    private void loadRules() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> rules = sharedPreferences.getStringSet(KEY_RULES, new HashSet<>());
        for (String rule : rules) {
            addRuleToView(rule);
        }
    }

    private void addRuleToView(String rule) {
        String[] parts = rule.split("\\|");
        String context = parts[0];
        String music = parts[1];

        View ruleView = LayoutInflater.from(getContext()).inflate(R.layout.rule_item, rulesListContainer, false);
        TextView textView = ruleView.findViewById(R.id.ruleTextView);
        textView.setText("In " + context + ", play " + music);

        ImageButton btnDeleteRule = ruleView.findViewById(R.id.btnDeleteRule);
        btnDeleteRule.setOnClickListener(v -> removeRule(rule, ruleView));

        rulesListContainer.addView(ruleView);
    }

    private void removeRule(String rule, View ruleView) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> rules = sharedPreferences.getStringSet(KEY_RULES, new HashSet<>());
        rules.remove(rule);
        sharedPreferences.edit().putStringSet(KEY_RULES, rules).apply();

        rulesListContainer.removeView(ruleView);
    }
}
