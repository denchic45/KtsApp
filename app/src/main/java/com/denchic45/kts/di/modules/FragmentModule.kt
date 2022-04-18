package com.denchic45.kts.di.modules

import com.denchic45.kts.ui.adminPanel.AdminPanelFragment
import com.denchic45.kts.ui.adminPanel.finder.FinderFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.TimetableEditorFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfGroupSubject.ChoiceOfGroupSubjectFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.lessonEditor.LessonEditorFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.simpleEventEditor.SimpleEventEditorFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.finder.TimetableFinderFragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.loader.TimetableLoaderFragment
import com.denchic45.kts.ui.confirm.ConfirmDialog
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.course.content.ContentFragment
import com.denchic45.kts.ui.course.taskInfo.TaskInfoFragment
import com.denchic45.kts.ui.course.sectionPicker.SectionPickerFragment
import com.denchic45.kts.ui.course.sections.CourseSectionEditorFragment
import com.denchic45.kts.ui.course.submission.SubmissionDialog
import com.denchic45.kts.ui.course.submissions.SubmissionsFragment
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.kts.ui.creator.CreatorDialog
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.group.users.GroupMembersFragment
import com.denchic45.kts.ui.iconPicker.IconPickerDialog
import com.denchic45.kts.ui.login.auth.AuthFragment
import com.denchic45.kts.ui.login.groupChooser.GroupChooserFragment
import com.denchic45.kts.ui.login.resetPassword.ResetPasswordFragment
import com.denchic45.kts.ui.login.verifyPhoneNum.VerifyPhoneNumFragment
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.settings.SettingsFragment
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.tasks.TasksFragment
import com.denchic45.kts.ui.tasks.completed.CompletedTasksFragment
import com.denchic45.kts.ui.tasks.overdue.OverdueTasksFragment
import com.denchic45.kts.ui.tasks.upcoming.UpcomingTasksFragment
import com.denchic45.kts.ui.teacherChooser.TeacherChooserFragment
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
    fun contributeGroupUsersFragment(): GroupMembersFragment

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
    fun contributeTeacherChooserFragment(): TeacherChooserFragment

    @ContributesAndroidInjector
    fun contributeChoiceOfGroupFragment(): GroupChooserFragment

    @ContributesAndroidInjector
    fun contributeVerifyPhoneNumFragment(): VerifyPhoneNumFragment

    @ContributesAndroidInjector
    fun contributeAdminPanelFragment(): AdminPanelFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    fun contributeResetPasswordFragment(): ResetPasswordFragment

    @ContributesAndroidInjector
    fun contributeAuthFragment(): AuthFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeCourseFragment(): CourseFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeTaskEditorFragment(): TaskEditorFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeSectionPickerFragment(): SectionPickerFragment

    @ContributesAndroidInjector
    fun contributeCreatorDialog(): CreatorDialog

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeContentFragment(): ContentFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeTaskFragment(): TaskInfoFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeSubmissionsFragment(): SubmissionsFragment

    @ContributesAndroidInjector
    fun contributeConfirmFragment(): ConfirmDialog

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeSubmissionFragment(): SubmissionDialog

    @ContributesAndroidInjector
    fun contributeTasksFragment(): TasksFragment

    @ContributesAndroidInjector
    fun contributeUpcomingTasksFragment(): UpcomingTasksFragment

    @ContributesAndroidInjector
    fun contributeOverdueTasksFragment(): OverdueTasksFragment

    @ContributesAndroidInjector
    fun contributeCompletedTasksFragment(): CompletedTasksFragment

    @ContributesAndroidInjector(modules = [IntentModule::class])
    fun contributeCourseSectionsFragment(): CourseSectionEditorFragment

    @ContributesAndroidInjector
    fun contributeIconPickerDialog(): IconPickerDialog
}