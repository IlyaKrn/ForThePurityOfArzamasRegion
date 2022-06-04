package com.example.finish1m.Presentation.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// абстрактный диалог

public abstract class Dialog extends Fragment {

    public static final String LOG_TAG = "FragmentTag";

    protected View rootView;
    protected FragmentManager fragmentManager;
    protected Context context;

    private static ArrayList<Dialog> currentDialogs = new ArrayList<>(); // текущие диалоги

    private OnDestroyListener onDestroyListener;

    private int containerId;

    public Dialog(AppCompatActivity activity) {
        context = activity.getApplicationContext();
        fragmentManager = activity.getSupportFragmentManager();
    }

    public Dialog(Fragment fragment) {
        context = fragment.getActivity().getApplicationContext();
        fragmentManager = fragment.getChildFragmentManager();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return rootView;

    }
    // создание и уничтожение диалога
    public void create(int containerId){
        this.containerId = containerId;
        // уничтожение всех предыдущих диалогов
        for (Dialog d : currentDialogs) {
            d.destroy();
        }
        fragmentManager.beginTransaction().add(containerId, this).commit();
        currentDialogs.add(this);
    }
    public void destroy(){
        try {
            fragmentManager.beginTransaction().remove(this).commit();
            if (onDestroyListener != null) {
                onDestroyListener.onDestroy();
            }
        } catch (Exception e){
            Log.w(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setClickable(getActivity().findViewById(containerId).getRootView(), false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setClickable(getActivity().findViewById(containerId).getRootView(), true);
    }

    public void setOnDestroyListener(OnDestroyListener onDestroyListener) {
        this.onDestroyListener = onDestroyListener;
    }
    // установка clickable на все элементы диалога
    public void freeze(){
        setClickable((ViewGroup) rootView, false);
    }
    protected void defreeze(){
        setClickable((ViewGroup) rootView, true);
    }
    private void setClickable(View view, boolean clickable) {
        if (view != null) {
            view.setClickable(clickable);
            if (view instanceof ViewGroup) {
                ViewGroup vg = ((ViewGroup) view);
                for (int i = 0; i < vg.getChildCount(); i++) {
                    setClickable(vg.getChildAt(i), clickable);
                }
            }
        }
    }
}
