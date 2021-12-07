//package com.denchic45.kts.ui.specialtyEditor;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//public class SpecialtyEditorViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String specialtyUuid;
//
//    public SpecialtyEditorViewModelFactory(@NonNull Application application, String specialtyUuid) {
//        this.application = application;
//        this.specialtyUuid = specialtyUuid;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == SpecialtyEditorViewModel.class) {
//            return (T) new SpecialtyEditorViewModel(application, specialtyUuid);
//        }
//        return null;
//    }
//
//}
