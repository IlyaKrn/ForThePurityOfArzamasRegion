package com.example.finish1m.Data.Firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.finish1m.Domain.Interfaces.AuthRepository;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthRepositoryImpl implements AuthRepository {

    private Context context;
    public AuthRepositoryImpl(Context context) {
        this.context = context;
    }

    // вход с почтой и паролем
    @Override
    public void EnterWithEmailAndPassword(String email, String password, OnSetDataListener listener) {
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (context != null) {
                        if(task.isSuccessful())
                            listener.onSetData();
                        else
                            listener.onCanceled();
                    }
                }
            });
        } catch (Exception e){
            listener.onFailed();
        }
    }

    // регистрация с почтой и паролем
    @Override
    public void RegisterWithEmailAndPassword(String email, String password, OnSetDataListener listener) {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (context != null) {
                        if(task.isSuccessful())
                            listener.onSetData();
                        else
                            listener.onCanceled();
                    }
                }
            });
        } catch (Exception e){
            listener.onFailed();
        }
    }

    // отправка письма для верификации
    @Override
    public void sendVerificationEmail(String email, OnSetDataListener listener) {
        try {
            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (context != null) {
                        if(task.isSuccessful())
                            listener.onSetData();
                        else
                            listener.onCanceled();
                    }
                }
            });
        } catch (Exception e){
            listener.onFailed();
        }
    }

    // отправка письма для сброса пароля
    @Override
    public void ResetPassword(String email, OnSetDataListener listener) {
        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (context != null) {
                        if(task.isSuccessful())
                            listener.onSetData();
                        else
                            listener.onCanceled();
                    }
                }
            });
        } catch (Exception e){
            listener.onFailed();
        }
    }

    // проверка верификации текущего пользователя
    @Override
    public boolean isCurrentUserVerified() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser().isEmailVerified();
        } catch (Exception e){
            return false;
        }
    }
}
