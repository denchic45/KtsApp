//package com.denchic45.kts.ui.group;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//public class GroupViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String groupUuid;
//
//    public GroupViewModelFactory(@NonNull Application application, String groupUuid) {
//        this.application = application;
//        this.groupUuid = groupUuid;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == GroupViewModel.class) {
//            return (T) new GroupViewModel(application, groupUuid);
//        }
//        return null;
//    }
//}
