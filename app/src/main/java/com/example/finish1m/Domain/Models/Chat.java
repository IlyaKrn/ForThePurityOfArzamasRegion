package com.example.finish1m.Domain.Models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat {

    private String id;
    private ArrayList<Message> messages; // список сообщений в чате

    public Chat(String id, ArrayList<Message> messages, ArrayList<String> members) {
        this.id = id;
        this.messages = messages;
    }

    public Chat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id='" + id + '\'' +
                ", messages=" + messages +
                '}';
    }
}
