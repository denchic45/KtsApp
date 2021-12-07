//package com.denchic45.kts.ui.group.editor;
//
//import android.app.Application;
//
//import androidx.annotation.NonNull;
//import androidx.lifecycle.ViewModel;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.denchic45.kts.data.model.domain.ListItem;
//
//import java.util.List;
//
//public class GroupEditorViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String groupUuid;
//    private final List<ListItem> courseList;
//
//    public GroupEditorViewModelFactory(@NonNull Application application, String groupUuid, List<ListItem> courseList) {
//        this.application = application;
//        this.groupUuid = groupUuid;
//        this.courseList = courseList;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == GroupEditorViewModel.class) {
//            return (T) new GroupEditorViewModel(application, groupUuid, courseList);
//        }
//        return null;
//    }
//}
