//package com.denchic45.kts.ui.adminPanel.finder;
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
//public class FinderViewModelFactory extends ViewModelProvider.NewInstanceFactory {
//
//        @NonNull
//        private final Application application;
//        private final List<ListItem> userOptions;
//        private final List<ListItem> groupOptions;
//    private final List<ListItem> subjectOptions;
//
//        public FinderViewModelFactory(@NonNull Application application, List<ListItem> userOptions, List<ListItem> groupOptions, List<ListItem> subjectOptions) {
//            this.application = application;
//            this.userOptions = userOptions;
//            this.groupOptions = groupOptions;
//            this.subjectOptions = subjectOptions;
//        }
//
//        @NonNull
//        @Override
//        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
//            if (modelClass == FinderViewModel.class) {
//                return (T) new FinderViewModel(application, userOptions, groupOptions, subjectOptions);
//            }
//            return null;
//        }
//    }