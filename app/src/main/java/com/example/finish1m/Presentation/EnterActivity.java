package com.example.finish1m.Presentation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.finish1m.Data.Firebase.AuthRepositoryImpl;
import com.example.finish1m.Data.Firebase.UserRepositoryImpl;
import com.example.finish1m.Data.SQLite.SQLiteRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.SQLiteUser;
import com.example.finish1m.Domain.Models.User;
import com.example.finish1m.Domain.UseCases.EnterWithEmailAndPasswordUseCase;
import com.example.finish1m.Domain.UseCases.GetSQLiteUserUseCase;
import com.example.finish1m.Domain.UseCases.ResetPasswordUseCase;
import com.example.finish1m.Domain.UseCases.WriteSQLiteUserUseCase;
import com.example.finish1m.R;
import com.example.finish1m.databinding.ActivityEnterBinding;

public class EnterActivity extends AppCompatActivity {

    private ActivityEnterBinding binding;

    private UserRepositoryImpl userRepository;
    private AuthRepositoryImpl authRepository;
    private SQLiteRepositoryImpl sqLiteRepository;

    private GetSQLiteUserUseCase getSQLiteUserUseCase;
    private WriteSQLiteUserUseCase writeSQLiteUserUseCase;
    private EnterWithEmailAndPasswordUseCase enterWithEmailAndPasswordUseCase;
    private ResetPasswordUseCase resetPasswordUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userRepository = new UserRepositoryImpl(this);
        authRepository = new AuthRepositoryImpl(this);
        sqLiteRepository = new SQLiteRepositoryImpl(this);

        getSQLiteUserUseCase = new GetSQLiteUserUseCase(sqLiteRepository, new OnGetDataListener<SQLiteUser>() {
            @Override
            public void onGetData(SQLiteUser data) {
                binding.etEmail.setText(data.getEmail());
                binding.etPassword.setText(data.getPassword());
            }

            @Override
            public void onVoidData() {

            }

            @Override
            public void onFailed() {
                Toast.makeText(EnterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(EnterActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            }
        });
        getSQLiteUserUseCase.execute();

        binding.btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = binding.etEmail.getText().toString();
                final String password = binding.etPassword.getText().toString();
                if(!TextUtils.isEmpty(email)){
                    if(!TextUtils.isEmpty(password)){
                        enterWithEmailAndPasswordUseCase = new EnterWithEmailAndPasswordUseCase(userRepository, authRepository, email, password, new OnGetDataListener<User>() {
                        @Override
                        public void onGetData(User data) {
                            PresentationConfig.user = data;
                            if (binding.cbAlwaysUse.isChecked()) {
                                writeSQLiteUserUseCase = new WriteSQLiteUserUseCase(sqLiteRepository, new SQLiteUser(email, password), new OnSetDataListener() {
                                    @Override
                                    public void onSetData() {

                                    }

                                    @Override
                                    public void onFailed() {
                                        Toast.makeText(EnterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCanceled() {
                                        Toast.makeText(EnterActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                writeSQLiteUserUseCase = new WriteSQLiteUserUseCase(sqLiteRepository, null, new OnSetDataListener() {
                                    @Override
                                    public void onSetData() {

                                    }

                                    @Override
                                    public void onFailed() {
                                        Toast.makeText(EnterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCanceled() {
                                        Toast.makeText(EnterActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            writeSQLiteUserUseCase.execute();
                            Intent intent = new Intent(EnterActivity.this, HubActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onVoidData() {
                            binding.tvPasswordErr.setVisibility(View.VISIBLE);
                            binding.tvPasswordErr.setText(R.string.wrong_password);
                            binding.tvEmailErr.setVisibility(View.VISIBLE);
                            binding.tvEmailErr.setText(R.string.wrong_email);
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(EnterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCanceled() {
                            binding.tvPasswordErr.setVisibility(View.VISIBLE);
                            binding.tvPasswordErr.setText(R.string.wrong_password);
                            binding.tvEmailErr.setVisibility(View.VISIBLE);
                            binding.tvEmailErr.setText(R.string.wrong_email);
                        }
                    });
                        enterWithEmailAndPasswordUseCase.execute();
                    }
                    else {
                        binding.tvPasswordErr.setVisibility(View.VISIBLE);
                        binding.tvPasswordErr.setText(R.string.empty_edit_text_error);
                    }
                }
                else {
                    binding.tvEmailErr.setVisibility(View.VISIBLE);
                    binding.tvEmailErr.setText(R.string.empty_edit_text_error);
                }
            }
        });
        binding.btResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = binding.etEmail.getText().toString();
                if(!TextUtils.isEmpty(email)){
                    resetPasswordUseCase = new ResetPasswordUseCase(authRepository, email, new OnSetDataListener() {
                        @Override
                        public void onSetData() {
                            Toast.makeText(EnterActivity.this, R.string.email_reset_password_sended, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed() {
                            Toast.makeText(EnterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCanceled() {
                            binding.tvPasswordErr.setVisibility(View.VISIBLE);
                            binding.tvPasswordErr.setText(R.string.wrong_password);
                            binding.tvEmailErr.setVisibility(View.VISIBLE);
                            binding.tvEmailErr.setText(R.string.wrong_email);
                        }
                    });
                    resetPasswordUseCase.execute();
                }
                else {
                    binding.tvEmailErr.setVisibility(View.VISIBLE);
                    binding.tvEmailErr.setText(R.string.empty_edit_text_error);
                }
            }
        });
    }
}