package com.example.finish1m.Presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finish1m.Data.Firebase.ChatRepositoryImpl;
import com.example.finish1m.Data.Firebase.ImageRepositoryImpl;
import com.example.finish1m.Data.Firebase.LocateRepositoryImpl;
import com.example.finish1m.Data.VK.VKImageRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Interfaces.Listeners.OnSetDataListener;
import com.example.finish1m.Domain.Models.Locate;
import com.example.finish1m.Domain.UseCases.DeleteLocateByIdUseCase;
import com.example.finish1m.Domain.UseCases.GetImageByRefUseCase;
import com.example.finish1m.Domain.UseCases.GetLocateByIdUseCase;
import com.example.finish1m.Domain.UseCases.RefactorLocateUseCase;
import com.example.finish1m.Presentation.Adapters.ImageListAdapter;
import com.example.finish1m.Presentation.Dialogs.DialogConfirm;
import com.example.finish1m.Presentation.Dialogs.DialogLoading;
import com.example.finish1m.Presentation.Dialogs.OnConfirmListener;
import com.example.finish1m.R;
import com.example.finish1m.databinding.ActivityRefactorLocateBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class RefactorLocateActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityRefactorLocateBinding binding;

    private LocateRepositoryImpl locateRepository;
    private ChatRepositoryImpl chatRepository;
    private VKImageRepositoryImpl vkImageRepository;
    private ImageRepositoryImpl imageRepository;
    private RefactorLocateUseCase refactorLocateUseCase;
    private GetLocateByIdUseCase getLocateByIdUseCase;
    private ArrayList<Bitmap> images = new ArrayList<>();// картинки для метки
    private ImageListAdapter adapter;

    private Locate locate; // метка
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRefactorLocateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatRepository = new ChatRepositoryImpl(this);
        imageRepository = new ImageRepositoryImpl(this);
        locateRepository = new LocateRepositoryImpl(this);
        vkImageRepository = new VKImageRepositoryImpl(this);

        DialogLoading dialog = new DialogLoading(RefactorLocateActivity.this, getString(R.string.loading_data));
        dialog.create(binding.fragmentContainerView);
        getLocateByIdUseCase = new GetLocateByIdUseCase(locateRepository, getIntent().getStringExtra("locateId"), new OnGetDataListener<Locate>() {
            @Override
            public void onGetData(Locate data) {
                locate = data;
                // установка маркера на карту
                if (googleMap != null)
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(locate.getLatitude(), locate.getLongitude())));
                binding.etMessage.setText(data.getMessage());
                binding.etTitle.setText(data.getTitle());
                if (locate.getImageRefs() == null)
                    dialog.destroy();
                final int[] count = {0};
                if(data.getImageRefs() != null) {
                    binding.btCreate.setClickable(false);
                    for (String s : data.getImageRefs()){
                        GetImageByRefUseCase getImageByRefUseCase = new GetImageByRefUseCase(imageRepository, vkImageRepository, s, new OnGetDataListener<Bitmap>() {
                            @Override
                            public void onGetData(Bitmap data1) {
                                count[0]++;
                                images.add(data1);
                                adapter.notifyDataSetChanged();
                                if (count[0] == data.getImageRefs().size())
                                    dialog.destroy();
                            }

                            @Override
                            public void onVoidData() {
                                count[0]++;
                                Toast.makeText(RefactorLocateActivity.this, R.string.get_data_failed, Toast.LENGTH_SHORT).show();
                                if (count[0] == data.getImageRefs().size()) {
                                    binding.btCreate.setClickable(true);
                                    dialog.destroy();
                                }
                            }

                            @Override
                            public void onFailed() {
                                count[0]++;
                                Toast.makeText(RefactorLocateActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                if (count[0] == data.getImageRefs().size()) {
                                    binding.btCreate.setClickable(true);
                                    dialog.destroy();
                                }
                            }

                            @Override
                            public void onCanceled() {
                                count[0]++;
                                Toast.makeText(RefactorLocateActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                if (count[0] == data.getImageRefs().size()) {
                                    binding.btCreate.setClickable(true);
                                    dialog.destroy();
                                }
                            }
                        });
                        getImageByRefUseCase.execute();
                    }
                }
            }

            @Override
            public void onVoidData() {
                Toast.makeText(RefactorLocateActivity.this, R.string.get_data_failed, Toast.LENGTH_SHORT).show();
                dialog.destroy();
            }

            @Override
            public void onFailed() {
                Toast.makeText(RefactorLocateActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                dialog.destroy();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(RefactorLocateActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                dialog.destroy();
            }
        });
        getLocateByIdUseCase.execute();




        // закрытие активности
        binding.btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // изменение метки
        binding.btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = binding.etTitle.getText().toString();
                final String message = binding.etMessage.getText().toString();
                if(!TextUtils.isEmpty(title)){
                    DialogLoading dialog = new DialogLoading(RefactorLocateActivity.this, getString(R.string.loading_data));
                    dialog.create(binding.fragmentContainerView);
                    if(!TextUtils.isEmpty(title)){
                        locate.setTitle(title);
                        locate.setMessage(message);
                        refactorLocateUseCase = new RefactorLocateUseCase(locateRepository, imageRepository, locate, images, new OnSetDataListener() {
                            @Override
                            public void onSetData() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.locate_refactor_success, Toast.LENGTH_SHORT).show();
                                dialog.destroy();
                                finish();
                            }

                            @Override
                            public void onFailed() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                dialog.destroy();
                                finish();
                            }

                            @Override
                            public void onCanceled() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                dialog.destroy();
                                finish();
                            }
                        });
                        refactorLocateUseCase.execute();
                    }
                    else {
                        binding.tvTitleErr.setVisibility(View.VISIBLE);
                        binding.tvTitleErr.setText(R.string.empty_edit_text_error);
                    }
                }
                else {
                    binding.tvTitleErr.setVisibility(View.VISIBLE);
                    binding.tvTitleErr.setText(R.string.empty_edit_text_error);
                }
            }
        });

        // удаление метки
        binding.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogConfirm dialog = new DialogConfirm((AppCompatActivity) RefactorLocateActivity.this, "Удаление метки", "Удалить", "Вы действительно хотите удалить метку?", new OnConfirmListener() {
                    @Override
                    public void onConfirm(DialogConfirm d) {
                        DeleteLocateByIdUseCase deleteEventByIdUseCase = new DeleteLocateByIdUseCase(locateRepository, locate.getId(), new OnSetDataListener() {
                            @Override
                            public void onSetData() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.locate_delete_success, Toast.LENGTH_SHORT).show();
                                d.destroy();
                                finish();
                            }

                            @Override
                            public void onFailed() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                d.destroy();
                                finish();
                            }

                            @Override
                            public void onCanceled() {
                                Toast.makeText(RefactorLocateActivity.this, R.string.access_denied, Toast.LENGTH_SHORT).show();
                                d.destroy();
                                finish();
                            }
                        });
                        deleteEventByIdUseCase.execute();
                    }
                });
                dialog.create(binding.fragmentContainerView);
            }
        });


        // добавление картинки
        binding.btAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        // адаптер картинок
        adapter = new ImageListAdapter(this, this, images);
        adapter.setOnItemRemoveListener(new ImageListAdapter.OnItemRemoveListener() {
            @Override
            public void onRemove(int position) {
                images.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        binding.rvImages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvImages.setAdapter(adapter);




        SupportMapFragment mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapView.getMapAsync(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 1){
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            iv.setImageURI(data.getData());
            Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            images.add(bitmap);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        // установка маркера на карту
        if (locate != null)
            googleMap.addMarker(new MarkerOptions().position(new LatLng(locate.getLatitude(), locate.getLongitude())));
        // установка камеры на координаты
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locate.getLatitude(), locate.getLongitude()), 11));

    }
}