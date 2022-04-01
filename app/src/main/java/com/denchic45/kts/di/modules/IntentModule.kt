package com.denchic45.kts.di.modules

import com.denchic45.kts.ui.avatar.FullImageActivity
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.course.content.ContentFragment
import com.denchic45.kts.ui.course.sections.CourseSectionEditorFragment
import com.denchic45.kts.ui.course.submission.SubmissionDialog
import com.denchic45.kts.ui.course.submissions.SubmissionsFragment
import com.denchic45.kts.ui.course.taskEditor.TaskEditorFragment
import com.denchic45.kts.ui.course.taskInfo.TaskInfoFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.group.users.GroupUsersFragment
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.timetable.TimetableFragment
import com.denchic45.kts.ui.userEditor.UserEditorFragment
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object IntentModule {

    @Named(CourseEditorFragment.COURSE_ID)
    @Provides
    fun provideCourseIdToCourseEditor(fragment: CourseEditorFragment): String? {
        return fragment.requireArguments().getString(CourseEditorFragment.COURSE_ID)
    }

    @Named(GroupCoursesFragment.GROUP_ID)
    @Provides
    fun provideGroupIdFromGroupCourses(groupCoursesFragment: GroupCoursesFragment): String? {
        return groupCoursesFragment.requireArguments().getString(GroupCoursesFragment.GROUP_ID)
    }

    @Named(GroupEditorFragment.GROUP_ID)
    @Provides
    fun provideGroupIdFromGroupEditor(groupEditorFragment: GroupEditorFragment): String? {
        return groupEditorFragment.requireArguments().getString(GroupEditorFragment.GROUP_ID)
    }

    @Named(SubjectEditorDialog.SUBJECT_ID)
    @Provides
    fun provideSubjectId(dialog: SubjectEditorDialog): String? {
        return dialog.requireArguments().getString(SubjectEditorDialog.SUBJECT_ID)
    }

    @Named(TimetableFragment.GROUP_ID)
    @Provides
    fun provideAffiliation(timetableFragment: TimetableFragment): String? {
        return timetableFragment.arguments?.getString(TimetableFragment.GROUP_ID)
    }

    @Named(FullImageActivity.IMAGE_URL)
    @Provides
    fun providePhotoUrlFromFullAvatarActivity(fullAvatarActivity: FullAvatarActivity): String {
        return fullAvatarActivity.intent.getStringExtra(FullImageActivity.IMAGE_URL)!!
    }

    @Named(ProfileFragment.USER_ID)
    @Provides
    fun provideUserIdFromProfileFragment(profileFragment: ProfileFragment): String {
        return profileFragment.navArgs.userId
    }

    @Named(GroupFragment.GROUP_ID)
    @Provides
    fun provideGroupIdFromGroup(groupFragment: GroupFragment): String? {
        return groupFragment.navArgs.groupId
    }

    @Named(GroupUsersFragment.GROUP_ID)
    @Provides
    fun provideGroupIdFromGroupUsers(groupUsersFragment: GroupUsersFragment): String? {
        return (groupUsersFragment.requireParentFragment() as GroupFragment).navArgs.groupId
    }

    @Named("UserEditor ${UserEditorFragment.USER_ID}")
    @Provides
    fun provideUserIdFromUserEditor(userEditorFragment: UserEditorFragment): String? {
        return userEditorFragment.requireArguments().getString(UserEditorFragment.USER_ID)
    }

    @Named(UserEditorFragment.USER_ROLE)
    @Provides
    fun provideUserRole(userEditorFragment: UserEditorFragment): String {
        return userEditorFragment.requireArguments().getString(UserEditorFragment.USER_ROLE)!!
    }

    @Named(UserEditorFragment.USER_GROUP_ID)
    @Provides
    fun provideUserGroupId(userEditorFragment: UserEditorFragment): String? {
        return userEditorFragment.requireArguments().getString(UserEditorFragment.USER_GROUP_ID)
    }

    @Named(SpecialtyEditorDialog.SPECIALTY_ID)
    @Provides
    fun provideSpecialtyId(specialtyEditorDialog: SpecialtyEditorDialog): String? {
        return specialtyEditorDialog.requireArguments()
            .getString(SpecialtyEditorDialog.SPECIALTY_ID)
    }

    @Named(CourseFragment.COURSE_ID)
    @Provides
    fun provideCourseIdFromCourse(courseFragment: CourseFragment): String {
        return courseFragment.navArgs.courseId
    }

    @Named(TaskEditorFragment.TASK_ID)
    @Provides
    fun provideTaskIdToTaskEditor(taskEditorFragment: TaskEditorFragment): String? {
        return taskEditorFragment.requireArguments().getString(TaskEditorFragment.TASK_ID)
    }

    @Named(TaskEditorFragment.COURSE_ID)
    @Provides
    fun provideCourseIdToTaskEditor(taskEditorFragment: TaskEditorFragment): String {
        return taskEditorFragment.requireArguments()
            .getString(TaskEditorFragment.COURSE_ID)!!
    }

    @Named(TaskEditorFragment.SECTION_ID)
    @Provides
    fun provideSectionId(taskEditorFragment: TaskEditorFragment): String? {
        return taskEditorFragment.requireArguments().getString(TaskEditorFragment.SECTION_ID)
    }

    @Named(ContentFragment.TASK_ID)
    @Provides
    fun provideTaskId(contentFragment: ContentFragment): String {
        return contentFragment.requireArguments().getString(ContentFragment.TASK_ID)!!
    }

    @Named(ContentFragment.COURSE_ID)
    @Provides
    fun provideCourseId(contentFragment: ContentFragment): String {
        return contentFragment.requireArguments()
            .getString(ContentFragment.COURSE_ID)!!
    }

    @Named(TaskInfoFragment.TASK_ID)
    @Provides
    fun provideTaskIdToTaskInfo(taskInfoFragment: TaskInfoFragment): String {
        return taskInfoFragment.requireParentFragment().requireArguments()
            .getString(ContentFragment.TASK_ID)!!
    }

    @Named(TaskInfoFragment.COURSE_ID)
    @Provides
    fun provideCourseIdToTaskInfo(taskInfoFragment: TaskInfoFragment): String {
        return taskInfoFragment.requireParentFragment().requireArguments()
            .getString(ContentFragment.COURSE_ID)!!
    }

    @Named(SubmissionsFragment.TASK_ID)
    @Provides
    fun provideTaskIdToSubmissions(submissionsFragment: SubmissionsFragment): String {
        return submissionsFragment.requireParentFragment().requireArguments()
            .getString(ContentFragment.TASK_ID)!!
    }

    @Named(SubmissionDialog.TASK_ID)
    @Provides
    fun provideTaskIdToSubmission(submissionDialog: SubmissionDialog): String {
        return submissionDialog
            .requireArguments()
            .getString(SubmissionDialog.TASK_ID)!!
    }

    @Named(SubmissionDialog.STUDENT_ID)
    @Provides
    fun provideStudentIdToSubmission(submissionDialog: SubmissionDialog): String {
        return submissionDialog.requireArguments().getString(SubmissionDialog.STUDENT_ID)!!
    }

    @Named(CourseSectionEditorFragment.COURSE_ID)
    @Provides
    fun provideCourseIdToCourseSectionEditor(courseSectionEditorFragment: CourseSectionEditorFragment): String {
        return courseSectionEditorFragment.requireArguments()
            .getString(CourseSectionEditorFragment.COURSE_ID)!!
    }


}