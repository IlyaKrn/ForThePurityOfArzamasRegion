package com.example.finish1m.Presentation.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.finish1m.Data.Firebase.EventRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Models.Event;
import com.example.finish1m.Domain.UseCases.CreateNewEventUseCase;
import com.example.finish1m.Domain.UseCases.GetEventListUseCase;
import com.example.finish1m.Presentation.Adapters.EventListAdapter;
import com.example.finish1m.Presentation.CreateNewEventActivity;
import com.example.finish1m.R;
import com.example.finish1m.databinding.FragmentHomeBinding;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private EventRepositoryImpl eventRepository;

    private GetEventListUseCase getEventListUseCase;

    private EventListAdapter adapter;
    private ArrayList<Event> events = new ArrayList<Event>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        eventRepository = new EventRepositoryImpl(getContext());

        getEventListUseCase = new GetEventListUseCase(eventRepository, new OnGetDataListener<ArrayList<Event>>() {
            @Override
            public void onGetData(ArrayList<Event> data) {
                events.clear();
                events.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onVoidData() {
                events.clear();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed() {
                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(getContext(), R.string.access_denied, Toast.LENGTH_SHORT).show();
            }
        });
        getEventListUseCase.execute();

        adapter = new EventListAdapter(getContext(), events);
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEvents.setAdapter(adapter);
        binding.btAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateNewEventActivity.class);
                startActivity(intent);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}