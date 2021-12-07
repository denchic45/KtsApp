//package com.denchic45.kts.ui.subjectEditor;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//public class SubjectEditorViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String subjectUuid;
//
//    public SubjectEditorViewModelFactory(@NonNull Application application, String subjectUuid) {
//        this.application = application;
//        this.subjectUuid = subjectUuid;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == SubjectEditorViewModel.class) {
//            return (T) new SubjectEditorViewModel(application, subjectUuid);
//        }
//        return null;
//    }
//
//}
