//package com.denchic45.kts.di.components;
//
//import com.denchic45.kts.di.modules.ChoiceOfCuratorModule;
//import com.denchic45.kts.di.modules.ChoiceOfGroupModule;
//import com.denchic45.kts.di.modules.ChoiceOfGroupSubjectModule;
//import com.denchic45.kts.di.modules.IconPickerModule;
//import com.denchic45.kts.di.modules.LessonEditorModule;
//import com.denchic45.kts.di.modules.TimetableEditorModule;
//import com.denchic45.kts.di.scopes.ChoiceOfCuratorScope;
//import com.denchic45.kts.di.scopes.ChoiceOfGroupSubjectScope;
//import com.denchic45.kts.di.scopes.FragmentScope;
//import com.denchic45.kts.di.scopes.LessonEditorScope;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.TimetableEditorViewModel;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfGroupSubject.ChoiceOfGroupSubjectViewModel;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfSubject.ChoiceOfSubjectViewModel;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.finder.TimetableFinderViewModel;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.lessonEditor.LessonEditorViewModel;
//import com.denchic45.kts.ui.adminPanel.timtableEditor.loader.TimetableLoaderViewModel;
//import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorInteractor;
//import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorViewModel;
//import com.denchic45.kts.ui.group.editor.GroupEditorViewModel;
//import com.denchic45.kts.ui.group.users.GroupUsersViewModel;
//import com.denchic45.kts.ui.iconPicker.IconPickerViewModel;
//import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupViewModel;
//import com.denchic45.kts.ui.subjectEditor.SubjectEditorViewModel;
//
//import dagger.Subcomponent;
//
//@Subcomponent(modules = {LessonEditorModule.class})
//@ChoiceOfGroupSubjectScope
//@LessonEditorScope
//@ChoiceOfCuratorScope
//@FragmentScope
//public interface LessonEditorComponent extends BaseDaggerComponent {
//
//    void inject(LessonEditorViewModel lessonEditorViewModel);
//
//    void inject(TimetableLoaderViewModel timetableLoaderViewModel);
//
//    void inject(IconPickerViewModel iconPickerViewModel);
//
//    @Subcomponent.Builder
//    interface Builder {
//
//        Builder lessonEditorModule(LessonEditorModule module);
//
//        Builder iconPickerModule(IconPickerModule module);
//
//        LessonEditorComponent build();
//    }
//}
