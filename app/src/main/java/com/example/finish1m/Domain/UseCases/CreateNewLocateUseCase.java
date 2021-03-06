package com.example.finish1m.Domain.UseCases;

import android.graphics.Bitmap;

import com.example.finish1m.Domain.Interfaces.ChatRepository;
import com.example.finish1m.Domain.Interfaces.EventRepository;
import com.example.finish1m.Domain.Interfaces.ImageRepository;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetImageListener;
import com.example.finish1m.Domain.Interfaces.LocateRepository;
import com.example.finish1m.Domain.Models.Chat;
import com.example.finish1m.Domain.Models.Event;
import com.example.finish1m.Domain.Models.Locate;

import java.util.ArrayList;

// создание метки

public class CreateNewLocateUseCase {

    private LocateRepository locateRepository;
    private ChatRepository chatRepository;
    private ImageRepository imageRepository;
    private Locate locate;
    private Chat chat;
    private ArrayList<Bitmap> images;
    private OnSetDataListener listener;

    public CreateNewLocateUseCase(LocateRepository locateRepository, ChatRepository chatRepository, ImageRepository imageRepository, Locate locate, Chat chat, ArrayList<Bitmap> images, OnSetDataListener listener) {
        this.locateRepository = locateRepository;
        this.chatRepository = chatRepository;
        this.imageRepository = imageRepository;
        this.locate = locate;
        this.chat = chat;
        this.images = images;
        this.listener = listener;
    }

    public void execute(){
        // создание чата
        chatRepository.setChat(chat.getId(), chat, new OnSetDataListener() {
            @Override
            public void onSetData() {
                // загрузка картинок
                final int[] count = {0};
                ArrayList<String> imageRefs = new ArrayList<>();
                if (images.size() > 0) {
                    for (int i = 0; i < images.size(); i++) {
                        imageRepository.setImage(images.get(i), new OnSetImageListener() {
                            @Override
                            public void onSetData(String ref) {
                                count[0]++;
                                imageRefs.add(ref);
                                if (count[0] == images.size()) {
                                    locate.setImageRefs(imageRefs);
                                    // запись
                                    locateRepository.setLocate(locate.getId(), locate, listener);
                                }
                            }

                            @Override
                            public void onFailed() {
                                listener.onFailed();
                            }

                            @Override
                            public void onCanceled() {
                                listener.onCanceled();
                            }
                        });

                    }
                }
                else{
                    locate.setImageRefs(imageRefs);
                    // запись
                    locateRepository.setLocate(locate.getId(), locate, listener);
                }
            }

            @Override
            public void onFailed() {
                listener.onFailed();
            }

            @Override
            public void onCanceled() {
                listener.onCanceled();
            }
        });
    }
}
