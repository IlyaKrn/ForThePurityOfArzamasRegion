package com.example.finish1m.Presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.finish1m.Data.Firebase.ProjectRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.Follow;
import com.example.finish1m.Domain.Models.Project;
import com.example.finish1m.Domain.UseCases.GetProjectByIdUseCase;
import com.example.finish1m.Domain.UseCases.RemoveFollowFromProjectUseCase;
import com.example.finish1m.Presentation.Adapters.Adapter;
import com.example.finish1m.Presentation.Adapters.FollowListAdapter;
import com.example.finish1m.Presentation.Dialogs.DialogConfirm;
import com.example.finish1m.Presentation.Dialogs.OnConfirmListener;
import com.example.finish1m.R;
import com.example.finish1m.databinding.ActivityRemoveFollowBinding;

import java.util.ArrayList;

public class RemoveFollowActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityRemoveFollowBinding binding;

    private FollowListAdapter adapter;
    private ArrayList<Follow> follows = new ArrayList<>(); // заявки
    private ProjectRepositoryImpl projectRepository;

    private GetProjectByIdUseCase getProjectByIdUseCase;
    private RemoveFollowFromProjectUseCase removeFollowFromProjectUseCase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoveFollowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        projectRepository = new ProjectRepositoryImpl(this);

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        // получение и установка данных
        getProjectByIdUseCase = new GetProjectByIdUseCase(projectRepository, getIntent().getStringExtra("projectId"), new OnGetDataListener<Project>() {
            @Override
            public void onGetData(Project data) {
                follows.clear();
                binding.noElements.setVisibility(View.VISIBLE);
                if(data.getFollows() != null) {
                    binding.noElements.setVisibility(View.GONE);
                    for(Follow f : data.getFollows()){
                        try {
                            if(f.getUserEmail().equals(PresentationConfig.getUser().getEmail()))
                                follows.add(f);
                        }catch (Exception e){
                            Toast.makeText(RemoveFollowActivity.this, R.string.data_load_error_try_again, Toast.LENGTH_SHORT).show();
                            follows.clear();
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onVoidData() {
                follows.clear();
                adapter.notifyDataSetChanged();
                binding.noElements.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailed() {

            }

            @Override
            public void onCanceled() {

            }
        });
        getProjectByIdUseCase.execute();






        // адаптер
        adapter = new FollowListAdapter(this, this, follows);
        adapter.setOnItemClickListener(new Adapter.OnStateClickListener<Follow>() {
            @Override
            public void onClick(Follow item, int position) {

            }

            @Override
            public void onLongClick(Follow item, int position) {
                // удаление заявки
                DialogConfirm dialog = new DialogConfirm(RemoveFollowActivity.this, "Отмена заявки", "отменить заявку", "Вы действителькно хотит отменить заявку?", new OnConfirmListener() {
                    @Override
                    public void onConfirm(DialogConfirm d) {
                        d.freeze();
                        removeFollowFromProjectUseCase = new RemoveFollowFromProjectUseCase(projectRepository, getIntent().getStringExtra("projectId"), item.getId(), new OnSetDataListener() {
                            @Override
                            public void onSetData() {
                                Toast.makeText(RemoveFollowActivity.this, R.string.follow_delete_success, Toast.LENGTH_SHORT).show();
                                d.destroy();
                            }

                            @Override
                            public void onFailed() {
                                Toast.makeText(RemoveFollowActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                d.destroy();
                            }

                            @Override
                            public void onCanceled() {
                                Toast.makeText(RemoveFollowActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                d.destroy();
                            }
                        });
                        removeFollowFromProjectUseCase.execute();
                    }
                });
                dialog.create(binding.fragmentContainerView);

            }
        });
        binding.rvFollows.setAdapter(adapter);
        binding.rvFollows.setLayoutManager(new LinearLayoutManager(this));
        binding.btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });





    }

    @Override
    public void onRefresh() {
        // получение и установка данных
        getProjectByIdUseCase = new GetProjectByIdUseCase(projectRepository, getIntent().getStringExtra("projectId"), new OnGetDataListener<Project>() {
            @Override
            public void onGetData(Project data) {
                follows.clear();
                binding.noElements.setVisibility(View.VISIBLE);
                if(data.getFollows() != null) {
                    binding.noElements.setVisibility(View.GONE);
                    for(Follow f : data.getFollows()){
                        try {
                            if(f.getUserEmail().equals(PresentationConfig.getUser().getEmail()))
                                follows.add(f);
                        }catch (Exception e){
                            Toast.makeText(RemoveFollowActivity.this, R.string.data_load_error_try_again, Toast.LENGTH_SHORT).show();
                            follows.clear();
                            break;
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onVoidData() {
                follows.clear();
                adapter.notifyDataSetChanged();
                binding.noElements.setVisibility(View.VISIBLE);
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailed() {
                Toast.makeText(RemoveFollowActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCanceled() {
                Toast.makeText(RemoveFollowActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
        getProjectByIdUseCase.execute();
    }
}