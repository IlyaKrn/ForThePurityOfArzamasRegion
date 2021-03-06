package com.example.finish1m.Presentation.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.example.finish1m.Data.Firebase.EventRepositoryImpl;
import com.example.finish1m.Data.Firebase.ImageRepositoryImpl;
import com.example.finish1m.Data.VK.VKImageRepositoryImpl;
import com.example.finish1m.Data.VK.VKRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.Event;
import com.example.finish1m.Domain.Models.User;
import com.example.finish1m.Domain.UseCases.AddUserToEventByEmailUseCase;
import com.example.finish1m.Domain.UseCases.DeleteEventByIdUseCase;
import com.example.finish1m.Domain.UseCases.GetEventByIdUseCase;
import com.example.finish1m.Domain.UseCases.GetImageByRefUseCase;
import com.example.finish1m.Domain.UseCases.RemoveUserFromEventUseCase;
import com.example.finish1m.Presentation.ChatActivity;
import com.example.finish1m.Presentation.Dialogs.DialogConfirm;
import com.example.finish1m.Presentation.Dialogs.OnConfirmListener;
import com.example.finish1m.Presentation.PresentationConfig;
import com.example.finish1m.Presentation.RefactorEventActivity;
import com.example.finish1m.Presentation.UserListActivity;
import com.example.finish1m.R;


import java.util.ArrayList;

// адаптер списка событий

public class EventListAdapter extends Adapter<Event, EventListAdapter.ViewHolder> {

    private boolean isNotifiedError = false;
    private boolean isNotifiedCancelled = false;
    private boolean isNotifiedVoidData = false;
    private User user = null;

    private ImageRepositoryImpl imageRepository;
    private VKImageRepositoryImpl vkImageRepository;
    private EventRepositoryImpl eventRepository;
    private VKRepositoryImpl vkRepository;

    public EventListAdapter(Activity activity, Context context, ArrayList<Event> items) {
        super(activity, context, items);
        this.imageRepository = new ImageRepositoryImpl(context);
        this.eventRepository = new EventRepositoryImpl(context);
        this.vkRepository = new VKRepositoryImpl(context);
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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_event, parent, false));
    }

    public class ViewHolder extends Adapter.Holder<Event> {

        private Button btReg;
        private TextView tvTitle;
        private TextView tvMessage;
        private GridLayout glImages;
        private ProgressBar progressBar;
        private Button btUsers;
        private ImageButton btMenu;
        private Button btChat;
        private Button btOpenInVk;
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
            btMenu = itemView.findViewById(R.id.bt_menu);
            tvReadMore = itemView.findViewById(R.id.tv_read_more);
            btOpenInVk = itemView.findViewById(R.id.bt_open_in_vk);
        }

        @Override
        public void bind(int position) {
            item = getItem(position);
            glImages.removeAllViews();
            btReg.setVisibility(View.VISIBLE);
            btChat.setVisibility(View.VISIBLE);
            btUsers.setVisibility(View.VISIBLE);
            btMenu.setVisibility(View.VISIBLE);
            btReg.setText("Вы записаны");

            // проверка наличия заявки от пользователя
            boolean isRegistered = false;
            if (item.getMembers() != null) {
                for (String s : item.getMembers()) {
                    if (s.equals(user.getEmail())) {
                        isRegistered = true;
                        break;
                    }
                }
            }

            // открытие/скрытие кнопок и установка данных
            if(!isRegistered){
                btChat.setVisibility(View.GONE);
                btUsers.setVisibility(View.GONE);
                btReg.setText("Записаться");
            }
            if (!user.isAdmin()){
                btMenu.setVisibility(View.GONE);
            }
            else {
                btChat.setVisibility(View.VISIBLE);
                btUsers.setVisibility(View.VISIBLE);
            }

            if(item.getDataSource() == Event.DATA_SOURCE_VK){
                btOpenInVk.setVisibility(View.VISIBLE);
            }
            else{
                btOpenInVk.setVisibility(View.GONE);
            }


            if (item.getType() == Event.NEWS){
            //    btChat.setVisibility(View.GONE);
                btUsers.setVisibility(View.GONE);
                btReg.setVisibility(View.GONE);
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

            // открыть в вк
            btOpenInVk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/club197453413?w=wall-197453413_%s", item.getId().substring(1)) + "%2Fall"));
                    activity.startActivity(browserIntent);
                }
            });

            // создание заявки
            boolean finalIsRegistered = isRegistered;
            btReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!finalIsRegistered) {
                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Запись", "Да", "Вы действительно хотите участвовать в мероприятии?", new OnConfirmListener() {
                            @Override
                            public void onConfirm(DialogConfirm d) {
                                d.freeze();
                                AddUserToEventByEmailUseCase addUserToEventByEmailUseCase = new AddUserToEventByEmailUseCase(eventRepository, item, user.getEmail(), new OnSetDataListener() {
                                    @Override
                                    public void onSetData() {
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
                                addUserToEventByEmailUseCase.execute();
                            }

                        });
                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                    }
                    else {
                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Отказ от участия", "Да", "Вы действительно хотите отказаться от участия в мероприятии?", new OnConfirmListener() {
                            @Override
                            public void onConfirm(DialogConfirm d) {
                                d.freeze();
                                RemoveUserFromEventUseCase removeUserFromEventUseCase = new RemoveUserFromEventUseCase(eventRepository, item, user.getEmail(), new OnSetDataListener() {
                                    @Override
                                    public void onSetData() {
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
                                removeUserFromEventUseCase.execute();
                            }

                        });
                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                    }
                }
            });
            // открытие чата
            btChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(item.getDataSource() == Event.DATA_SOURCE_FIREBASE) {
                        Intent intent = new Intent(activity, ChatActivity.class);
                        intent.putExtra("chatId", item.getChatId());
                        activity.startActivity(intent);
                    }
                    else{
                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Действие недоступно", "Перейти", "Данное действие недоступно в приложении. Перейдите в ВК", new OnConfirmListener() {
                            @Override
                            public void onConfirm(DialogConfirm d) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/club197453413?w=wall-197453413_%s", item.getId().substring(1)) + "%2Fall"));
                                activity.startActivity(browserIntent);
                            }
                        });
                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                    }
                }
            });
            // открытие списка пользователей
            btUsers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    if(item.getDataSource() == Event.DATA_SOURCE_FIREBASE) {
                        Intent intent = new Intent(activity, UserListActivity.class);
                        if (item.getMembers() == null)
                            item.setMembers(new ArrayList<>());
                        intent.putExtra("user_size", item.getMembers().size());
                        for (int i = 0; i < item.getMembers().size(); i++) {
                            intent.putExtra("user" + i, item.getMembers().get(i));
                        }
                        activity.startActivity(intent);
                    }
                    else{
                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Действие недоступно", "Перейти", "Данное действие недоступно в приложении. Перейдите в ВК", new OnConfirmListener() {
                            @Override
                            public void onConfirm(DialogConfirm d) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/club197453413?w=wall-197453413_%s", item.getId().substring(1)) + "%2Fall"));
                                activity.startActivity(browserIntent);
                            }
                        });
                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                    }
                }
            });
            // открытие меню с изменением и удалением
            btMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu menu = new PopupMenu(context, view);
                    menu.inflate(R.menu.popup_menu_event_list_item);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch(menuItem.getItemId()) {
                                case R.id.refactor:
                                    if(item.getDataSource() == Event.DATA_SOURCE_FIREBASE) {
                                        Intent intent = new Intent(activity, RefactorEventActivity.class);
                                        intent.putExtra("eventId", item.getId());
                                        activity.startActivity(intent);
                                    }
                                    else{
                                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Действие недоступно", "Перейти", "Данное действие недоступно в приложении. Перейдите в ВК", new OnConfirmListener() {
                                            @Override
                                            public void onConfirm(DialogConfirm d) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/club197453413?w=wall-197453413_%s", item.getId().substring(1)) + "%2Fall"));
                                                activity.startActivity(browserIntent);
                                            }
                                        });
                                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                                    }
                                    break;
                                case R.id.delete:
                                    if(item.getDataSource() == Event.DATA_SOURCE_FIREBASE) {
                                        DialogConfirm dialogConfirm = new DialogConfirm((AppCompatActivity) activity, "Удаление", "Удалить", "Вы действительно хотите удалить публикацию?", new OnConfirmListener() {
                                            @Override
                                            public void onConfirm(DialogConfirm d) {
                                                DeleteEventByIdUseCase deleteEventByIdUseCase = new DeleteEventByIdUseCase(eventRepository, item.getId(), new OnSetDataListener() {
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
                                                deleteEventByIdUseCase.execute();
                                            }
                                        });
                                        dialogConfirm.create(activity.findViewById(R.id.fragmentContainerView));
                                    }
                                    else{
                                        DialogConfirm dialog = new DialogConfirm((AppCompatActivity) activity, "Действие недоступно", "Перейти", "Данное действие недоступно в приложении. Перейдите в ВК", new OnConfirmListener() {
                                            @Override
                                            public void onConfirm(DialogConfirm d) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://vk.com/club197453413?w=wall-197453413_%s", item.getId().substring(1)) + "%2Fall"));
                                                activity.startActivity(browserIntent);
                                            }
                                        });
                                        dialog.create(activity.findViewById(R.id.fragmentContainerView));
                                    }
                                    break;
                            }

                            return false;
                        }
                    });
                    menu.show();
                }
            });


            // получение и установка данных
            GetEventByIdUseCase getEventListUseCase = new GetEventByIdUseCase(eventRepository, vkRepository, item.getId(), new OnGetDataListener<Event>() {
                @Override
                public void onGetData(Event data) {
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
            getEventListUseCase.execute();
        }
    }
}
