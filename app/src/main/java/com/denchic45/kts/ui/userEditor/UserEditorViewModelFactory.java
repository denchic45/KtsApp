//package com.denchic45.kts.ui.userEditor;
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
//public class UserEditorViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//    @NonNull
//    private final Application application;
//    private final String uuid;
//    private final String userType;
//    private final String groupUuid;
//    private final List<ListItem> genders;
//
//    public UserEditorViewModelFactory(@NonNull Application application, String uuid, String userType, String groupUuid, List<ListItem> genders) {
//        this.application = application;
//        this.uuid = uuid;
//        this.userType = userType;
//        this.groupUuid = groupUuid;
//        this.genders = genders;
//    }
//
//    @NonNull
//    @Override
//    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//        if (modelClass == UserEditorViewModel.class) {
//            return (T) new UserEditorViewModel(application, uuid, userType, groupUuid, genders);
//        }
//        return null;
//    }
//}
