//package com.denchic45.kts.ui.group.courses;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//public class GroupCoursesViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String groupUuid;
//
//    public GroupCoursesViewModelFactory(@NonNull Application application, String groupUuid) {
//        this.application = application;
//        this.groupUuid = groupUuid;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == GroupCoursesViewModel.class) {
//            return (T) new GroupCoursesViewModel(application, groupUuid);
//        }
//        return null;
//    }
//}
