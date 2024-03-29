package com.example.finish1m.Presentation.ui.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.finish1m.Data.Firebase.ImageRepositoryImpl;
import com.example.finish1m.Data.Firebase.LocateRepositoryImpl;
import com.example.finish1m.Data.VK.VKImageRepositoryImpl;
import com.example.finish1m.Domain.Interfaces.Listeners.OnGetDataListener;
import com.example.finish1m.Domain.Models.Locate;
import com.example.finish1m.Domain.UseCases.GetImageByRefUseCase;
import com.example.finish1m.Domain.UseCases.GetLocateListUseCase;
import com.example.finish1m.Presentation.Adapters.MapInfoWindowAdapter;
import com.example.finish1m.Presentation.CreateNewLocateActivity;
import com.example.finish1m.Presentation.PresentationConfig;
import com.example.finish1m.Presentation.RefactorLocateActivity;
import com.example.finish1m.Presentation.ui.myEvent.MyEventsFragment;
import com.example.finish1m.R;
import com.example.finish1m.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener {

    private FragmentMapBinding binding;

    private GoogleMap googleMap;
    private LocateRepositoryImpl locateRepository;
    private ImageRepositoryImpl imageRepository;
    private VKImageRepositoryImpl vkImageRepository;
    private MapInfoWindowAdapter adapter;
    private GetLocateListUseCase getLocateListUseCase;

    private ArrayList<Locate> locates = new ArrayList<>(); // список меток
    private boolean isAdd = false;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        adapter = new MapInfoWindowAdapter(getActivity(), getContext(), locates);

        binding.swipeRefreshLayout.setOnRefreshListener(this);
        locateRepository = new LocateRepositoryImpl(getContext());
        imageRepository = new ImageRepositoryImpl(getContext());
        vkImageRepository = new VKImageRepositoryImpl(getContext());

        // получение и установка данных
        getLocateListUseCase = new GetLocateListUseCase(locateRepository, new OnGetDataListener<ArrayList<Locate>>() {
            @Override
            public void onGetData(ArrayList<Locate> data) {
                if(MapFragment.this.isAdded()) {
                    Map<Locate, ArrayList<Bitmap>> cache = new HashMap<>(); // картинки для загрузки в адаптер
                    locates.clear();
                    locates.addAll(data);
                    for (Locate l : locates) {
                        final int[] count = {0};
                        cache.put(l, new ArrayList<>());
                        if (l.getImageRefs() != null) {
                            for (int i = 0; i < l.getImageRefs().size(); i++) {
                                GetImageByRefUseCase getImageByRefUseCase = new GetImageByRefUseCase(imageRepository, vkImageRepository, l.getImageRefs().get(i), new OnGetDataListener<Bitmap>() {
                                    @Override
                                    public void onGetData(Bitmap data) {
                                        count[0]++;
                                        cache.get(l).add(data);
                                        adapter.loadCache(cache);
                                    }

                                    @Override
                                    public void onVoidData() {
                                        count[0]++;
                                        adapter.loadCache(cache);
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
                                getImageByRefUseCase.execute();
                            }
                        }
                    }
                    if (googleMap != null) {
                        googleMap.clear();
                        for (Locate l : locates) {
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())));
                        }
                    }
                }

            }

            @Override
            public void onVoidData() {
                if(MapFragment.this.isAdded()) {
                    locates.clear();
                    if (googleMap != null) {
                        googleMap.clear();
                        for (Locate l : locates) {
                            if (googleMap != null) {
                                googleMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())));
                            }
                        }
                    }
                }
            }


            @Override
            public void onFailed() {
                if(MapFragment.this.isAdded()) {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCanceled() {
                if(MapFragment.this.isAdded()) {
                    Toast.makeText(getContext(), R.string.access_denied, Toast.LENGTH_SHORT).show();
                }
            }

        });
        getLocateListUseCase.execute();

        // кнопка добавления события
        View.OnClickListener onAddListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.fabCancel.setVisibility(View.VISIBLE);
                binding.fabAddLocate.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                if (isAdd){
                    isAdd = false;
                    Toast.makeText(getActivity().getApplicationContext(), R.string.add_locate_mod_disabled, Toast.LENGTH_SHORT).show();
                }
                else {
                    isAdd = true;
                    Toast.makeText(getActivity().getApplicationContext(), R.string.add_locate_mod_enabled, Toast.LENGTH_SHORT).show();
                }

            }
        };

        binding.fabAddLocate.setOnClickListener(onAddListener);
        binding.fabCancel.setOnClickListener(onAddListener);
        SupportMapFragment mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapView.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setInfoWindowAdapter(adapter);
        // установка камеры на координаты
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.400135, 43.828324), 11));
        // добавление метки
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (isAdd) {
                    Intent intent = new Intent(getActivity(), CreateNewLocateActivity.class);
                    intent.putExtra("longitude", latLng.longitude);
                    intent.putExtra("latitude", latLng.latitude);
                    startActivity(intent);

                    isAdd = false;
                    binding.fabCancel.setVisibility(View.GONE);
                    binding.fabAddLocate.setVisibility(View.VISIBLE);
                }
            }
        });
        // нажатие на окно метки
        googleMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(@NonNull Marker marker) {
                try {
                    if(PresentationConfig.getUser().isAdmin()) {
                        for (Locate l : locates) {
                            if (new LatLng(l.getLatitude(), l.getLongitude()).equals(marker.getPosition())) {
                                Intent intent = new Intent(getActivity(), RefactorLocateActivity.class);
                                intent.putExtra("locateId", l.getId());
                                getActivity().startActivity(intent);
                            }
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onRefresh() {
        // получение и установка данных
        getLocateListUseCase = new GetLocateListUseCase(locateRepository, new OnGetDataListener<ArrayList<Locate>>() {
            @Override
            public void onGetData(ArrayList<Locate> data) {
                if(MapFragment.this.isAdded()) {
                    Map<Locate, ArrayList<Bitmap>> cache = new HashMap<>(); // картинки для загрузки в адаптер
                    locates.clear();
                    locates.addAll(data);
                    for (Locate l : locates) {
                        final int[] count = {0};
                        cache.put(l, new ArrayList<>());
                        if (l.getImageRefs() != null) {
                            for (int i = 0; i < l.getImageRefs().size(); i++) {
                                GetImageByRefUseCase getImageByRefUseCase = new GetImageByRefUseCase(imageRepository, vkImageRepository, l.getImageRefs().get(i), new OnGetDataListener<Bitmap>() {
                                    @Override
                                    public void onGetData(Bitmap data) {
                                        count[0]++;
                                        cache.get(l).add(data);
                                        adapter.loadCache(cache);
                                    }

                                    @Override
                                    public void onVoidData() {
                                        count[0]++;
                                        adapter.loadCache(cache);
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
                                getImageByRefUseCase.execute();
                            }
                        }
                    }
                    if (googleMap != null) {
                        googleMap.clear();
                        for (Locate l : locates) {
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())));
                        }
                    }
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onVoidData() {
                if(MapFragment.this.isAdded()) {
                    locates.clear();
                    if (googleMap != null) {
                        googleMap.clear();
                        for (Locate l : locates) {
                            if (googleMap != null) {
                                googleMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())));
                            }
                        }
                    }
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }


            @Override
            public void onFailed() {
                if(MapFragment.this.isAdded()) {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCanceled() {
                if(MapFragment.this.isAdded()) {
                    Toast.makeText(getContext(), R.string.access_denied, Toast.LENGTH_SHORT).show();
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }

        });
        getLocateListUseCase.execute();
    }
}