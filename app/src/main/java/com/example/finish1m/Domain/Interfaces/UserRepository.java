package com.example.finish1m.Domain.Interfaces;

import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.Event;
import com.example.finish1m.Domain.Models.User;

import java.util.ArrayList;

public interface UserRepository {

    void getUserList(OnGetDataListener<ArrayList<User>> listener);    // получение списка пользователей
    void getUserByEmail(String userEmail, OnGetDataListener<User> listener);    // получение пользователя по id
    void setUser(String email, User user, OnSetDataListener listener);    // запись данных в пользователя по id
    String getNewId();    // получение нового id
}
