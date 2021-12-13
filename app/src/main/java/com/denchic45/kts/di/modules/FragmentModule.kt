package com.denchic45.kts.di.modules

import com.denchic45.kts.ui.adminPanel.finder.FinderFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.TimetableEditorFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfGroupSubject.ChoiceOfGroupSubjectFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.EventEditorFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.lessonEditor.LessonEditorFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.simpleEventEditor.SimpleEventEditorFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.finder.TimetableFinderFragment
import com.denchic45.kts.ui.adminPanel.timtableEditor.loader.TimetableLoaderFragment
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.group.choiceOfCurator.ChoiceOfCuratorFragment
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.group.users.GroupUsersFragment
import com.denchic45.kts.ui.login.auth.AuthFragment
import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupFragment
import com.denchic45.kts.ui.login.resetPassword.ResetPasswordFragment
import com.denchic45.kts.ui.login.verifyPhoneNum.VerifyPhoneNumFragment
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.settings.SettingsFragment
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.timetable.TimetableFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface FragmentModule {

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeSubjectEditorDialog(): SubjectEditorDialog

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeTimetableFragment(): TimetableFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeCourseEditorFragment(): CourseEditorFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeGroupFragment(): GroupFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeGroupUsersFragment(): GroupUsersFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeGroupCoursesFragment(): GroupCoursesFragment

    @ContributesAndroidInjector
    fun contributeLessonEditorFragment(): LessonEditorFragment

    @ContributesAndroidInjector
    fun contributeSimpleEventEditorFragment(): SimpleEventEditorFragment

    @ContributesAndroidInjector
    fun contributeGroupTimetableEditorFragment(): TimetableEditorFragment

    @ContributesAndroidInjector
    fun contributeGroupTimetableLoaderFragment(): TimetableLoaderFragment

    @ContributesAndroidInjector
    fun contributeGroupTimetableFinderFragment(): TimetableFinderFragment

    @ContributesAndroidInjector
    fun contributeEventEditorFragment(): EventEditorFragment

    @ContributesAndroidInjector(modules = [IntentModule::class, RawModule::class])
    fun contributeGroupEditorFragment(): GroupEditorFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeSpecialtyEditorDialog(): SpecialtyEditorDialog

    @ContributesAndroidInjector(modules = [IntentModule::class, RawModule::class])
    fun contributeFinderFragment(): FinderFragment

    @ContributesAndroidInjector
    fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    fun contributeChoiceOfGroupSubjectFragment(): ChoiceOfGroupSubjectFragment

    @ContributesAndroidInjector
    fun contributeChoiceOfCuratorFragment(): ChoiceOfCuratorFragment

    @ContributesAndroidInjector
    fun contributeChoiceOfGroupFragment(): ChoiceOfGroupFragment

    @ContributesAndroidInjector
    fun contributeVerifyPhoneNumFragment(): VerifyPhoneNumFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    fun contributeResetPasswordFragment(): ResetPasswordFragment

    @ContributesAndroidInjector
    fun contributeAuthFragment(): AuthFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeACourseFragment(): CourseFragment
}