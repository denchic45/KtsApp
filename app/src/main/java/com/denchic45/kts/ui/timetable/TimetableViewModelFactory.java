//package com.denchic45.kts.ui.timetable;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.denchic45.kts.ui.subjectEditor.SubjectEditorViewModel;
//
//public class TimetableViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String affiliation;
//
//    public TimetableViewModelFactory(@NonNull Application application, String affiliation) {
//        this.application = application;
//        this.affiliation = affiliation;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == TimetableViewModel.class) {
//            return (T) new TimetableViewModel(application, affiliation);
//        }
//        return null;
//    }
//
//}
