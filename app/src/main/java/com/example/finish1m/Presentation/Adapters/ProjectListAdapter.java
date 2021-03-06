package com.example.finish1m.Presentation.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finish1m.Data.Firebase.ImageRepositoryImpl;
import com.example.finish1m.Data.Firebase.ProjectRepositoryImpl;
import com.example.finish1m.Data.VK.VKImageRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.Follow;
import com.example.finish1m.Domain.Models.Project;
import com.example.finish1m.Domain.Models.User;
import com.example.finish1m.Domain.UseCases.DeleteProjectByIdUseCase;
import com.example.finish1m.Domain.UseCases.GetImageByRefUseCase;
import com.example.finish1m.Domain.UseCases.GetProjectByIdUseCase;
import com.example.finish1m.Presentation.ChatActivity;
import com.example.finish1m.Presentation.CreateNewFollowActivity;
import com.example.finish1m.Presentation.Dialogs.DialogConfirm;
import com.example.finish1m.Presentation.Dialogs.OnConfirmListener;
import com.example.finish1m.Presentation.FollowsListActivity;
import com.example.finish1m.Presentation.PresentationConfig;
import com.example.finish1m.Presentation.RefactorProjectActivity;
import com.example.finish1m.Presentation.RemoveFollowActivity;
import com.example.finish1m.R;

import java.util.ArrayList;

// адаптер списка проектов

public class ProjectListAdapter extends  Adapter<Project, ProjectListAdapter.ViewHolder>{

    private boolean isNotifiedError = false;
    private boolean isNotifiedCancelled = false;
    private boolean isNotifiedVoidData = false;
    private User user = null;

    private ImageRepositoryImpl imageRepository;
    private VKImageRepositoryImpl vkImageRepository;
    private ProjectRepositoryImpl projectRepository;

    public ProjectListAdapter(Activity activity, Context context, ArrayList<Project> items) {
        super(activity, context, items);
        this.imageRepository = new ImageRepositoryImpl(context);
        this.projectRepository = new ProjectRepositoryImpl(context);
        vkImageRepository = new VKImageRepositoryImpl(context);
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                isNotifiedError = false;
                isNotifiedCancelled = false;
                isNotifiedVoidData = false;
            }
        });
        try {
            user = PresentationConfig.getUser();
        } catch (Exception e) {
            Toast.makeText(context, R.string.data_load_error_try_again, Toast.LENGTH_SHORT).show();
            items.clear();
        }
    }

    @Override
    protected ViewHolder onCreateHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_project, parent, false));
    }

    public class ViewHolder extends Holder<Project> {

        private Button btReg;
        private TextView tvTitle;
        private TextView tvMessage;
        private GridLayout glImages;
        private ProgressBar progressBar;
        private Button btUsers;
        private ImageButton btMenu;
        private Button btChat;
        private Button btRemoveReg;
        private TextView tvReadMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btReg = itemView.findViewById(R.id.bt_reg);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            glImages = itemView.findViewById(R.id.gl_images);
            progressBar = itemView.findViewById(R.id.progress);
            btUsers = itemView.findViewById(R.id.bt_users);
            btChat = itemView.findViewById(R.id.bt_chat);
            btRemoveReg = itemView.findViewById(R.id.bt_remove_reg);
            btMenu = itemView.findViewById(R.id.bt_menu);
            tvReadMore = itemView.findViewById(R.id.tv_read_more);
        }

        @Override
        public void bind(int position) {
            item = getItem(position);
            glImages.removeAllViews();
            btReg.setVisibility(View.VISIBLE);
            btChat.setVisibility(View.VISIBLE);
            btUsers.setVisibility(View.VISIBLE);
            btMenu.setVisibility(View.VISIBLE);


            // открытие/скрытие кнопок и установка данных
            btChat.setVisibility(View.GONE);
            btUsers.setVisibility(View.GONE);
            btReg.setText("Записаться");

            btChat.setVisibility(View.VISIBLE);
            if (!user.isAdmin()){
                btMenu.setVisibility(View.GONE);
                btUsers.setVisibility(View.GONE);
            }
            else {
                btUsers.setVisibility(View.VISIBLE);
            }

            // проверка наличия заявки от пользователя
            boolean isRegistered = false;
            if (item.getFollows() != null) {
                for (Follow s : item.getFollows()) {
                    if (s.getUserEmail().equals(user.getEmail())) {
                        isRegistered = true;
                        break;
                    }
                }
            }

            btRemoveReg.setVisibility(View.GONE);
            btRemoveReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, RemoveFollowActivity.class);
                    intent.putExtra("projectId", item.getId());
                    activity.startActivity(intent);
                }
            });
            if (isRegistered){
                btRemoveReg.setVisibility(View.VISIBLE);
            }


            tvTitle.setText(item.getTitle());
            tvMessage.setText(item.getMessage());

            if (item.getMessage().length() > PresentationConfig.SMALL_MESSAGE_SIZE) {
                tvMessage.setText(item.getMessage().substring(0, PresentationConfig.SMALL_MESSAGE_SIZE));
                tvReadMore.setVisibility(View.VISIBLE);
                tvReadMore.setText(R.string.read_more);
                final boolean[] isHide = {true};
                tvReadMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isHide[0]){
                            isHide[0] = false;
                            tvMessage.setText(item.getMessage());
                            tvReadMore.setVisibility(View.VISIBLE);
                            tvReadMore.setText(R.string.hide);
                        }
                        else {
                            isHide[0] = true;
                            tvMessage.setText(item.getMessage().substring(0, PresentationConfig.SMALL_MESSAGE_SIZE));
                            tvReadMore.setVisibility(View.VISIBLE);
                            tvReadMore.setText(R.string.read_more);
                        }
                    }
                });
            }
            else {
                tvReadMore.setVisibility(View.GONE);
            }


            // создание заявки
            btReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, CreateNewFollowActivity.class);
                    intent.putExtra("projectId", item.getId());
                    activity.startActivity(intent);
                }
            });
            // открытие чата
            btChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    intent.putExtra("chatId", item.getChatId());
                    activity.startActivity(intent);
                }
            });
            // открытие списка пользователей
            btUsers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    Intent intent = new Intent(activity, FollowsListActivity.class);
                    intent.putExtra("projectId", item.getId());
                    activity.startActivity(intent);
                }
            });
            // открытие меню с изменением и удалением
            btMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu menu = new PopupMenu(context, view);
                    menu.inflate(R.menu.popup_menu_project_list_item);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch(menuItem.getItemId()) {
                                case R.id.refactor:
                                    Intent intent = new Intent(activity, RefactorProjectActivity.class);
                                    intent.putExtra("projectId", item.getId());
                                    activity.startActivity(intent);
                                    break;
                                case R.id.delete:
                                    DialogConfirm dialogConfirm = new DialogConfirm((AppCompatActivity) activity, "Удаление", "Удалить", "Вы действительно хотите удалить событие?", new OnConfirmListener() {
                                        @Override
                                        public void onConfirm(DialogConfirm d) {
                                            DeleteProjectByIdUseCase deleteProjectByIdUseCase = new DeleteProjectByIdUseCase(projectRepository, item.getId(), new OnSetDataListener() {
                                                @Override
                                                public void onSetData() {
                                                    Toast.makeText(context, R.string.event_delete_success, Toast.LENGTH_SHORT).show();
                                                    d.destroy();
                                                }

                                                @Override
                                                public void onFailed() {
                                                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                                                    d.destroy();
                                                }

                                                @Override
                                                public void onCanceled() {
                                                    Toast.makeText(context, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                                    d.destroy();
                                                }
                                            });
                                            deleteProjectByIdUseCase.execute();
                                        }
                                    });
                                    dialogConfirm.create(activity.findViewById(R.id.fragmentContainerView));
                                    break;
                            }

                            return false;
                        }
                    });
                    menu.show();
                }
            });


            // получение и установка данных
            GetProjectByIdUseCase getProjectListUseCase = new GetProjectByIdUseCase(projectRepository, item.getId(), new OnGetDataListener<Project>() {
                @Override
                public void onGetData(Project data) {
                    if (item.getImageRefs() != null) {
                        for (int i = 0; i < item.getImageRefs().size(); i++) {
                            GetImageByRefUseCase getImageByRefUseCase = new GetImageByRefUseCase(imageRepository, vkImageRepository, item.getImageRefs().get(i), new OnGetDataListener<Bitmap>() {
                                @Override
                                public void onGetData(Bitmap data1) {
                                    if(item.getId().equals(data.getId()));{
                                        ImageView iv = new ImageView(context);
                                        iv.setImageBitmap(data1);
                                        glImages.addView(iv);
                                    }
                                }

                                @Override
                                public void onVoidData() {
                                    if(!isNotifiedVoidData) {
                                        isNotifiedVoidData = true;
                                        Toast.makeText(context, R.string.get_data_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailed() {
                                    if(!isNotifiedError) {
                                        isNotifiedError = true;
                                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCanceled() {
                                    if(!isNotifiedCancelled) {
                                        isNotifiedCancelled = true;
                                        Toast.makeText(context, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            getImageByRefUseCase.execute();
                        }
                    }
                }

                @Override
                public void onVoidData() {
                    if(!isNotifiedVoidData) {
                        isNotifiedVoidData = true;
                        Toast.makeText(context, R.string.get_data_failed, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed() {
                    if(!isNotifiedError) {
                        isNotifiedError = true;
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCanceled() {
                    if(!isNotifiedCancelled) {
                        isNotifiedCancelled = true;
                        Toast.makeText(context, R.string.access_denied, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            getProjectListUseCase.execute();
        }
    }
}
