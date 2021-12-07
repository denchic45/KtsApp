package com.denchic45.kts.di.modules

import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.ui.avatar.FullImageActivity
import com.denchic45.kts.ui.course.CourseFragment
import com.denchic45.kts.ui.courseEditor.CourseEditorActivity
import com.denchic45.kts.ui.courseEditor.CourseEditorFragment
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.group.courses.GroupCoursesFragment
import com.denchic45.kts.ui.group.editor.GroupEditorActivity
import com.denchic45.kts.ui.group.editor.GroupEditorFragment
import com.denchic45.kts.ui.profile.ProfileFragment
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.denchic45.kts.ui.specialtyEditor.SpecialtyEditorDialog
import com.denchic45.kts.ui.subjectEditor.SubjectEditorDialog
import com.denchic45.kts.ui.timetable.TimetableFragment
import com.denchic45.kts.ui.userEditor.UserEditorActivity
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object IntentModule {

    @Named(CourseEditorActivity.COURSE_UUID)
    @Provides
    fun provideCourseUuid(fragment: CourseEditorFragment): String? {
        return fragment.requireArguments().getString(CourseEditorActivity.COURSE_UUID)
    }

    @Named("GroupCourses ${GroupPreference.GROUP_UUID}")
    @Provides
    fun provideGroupUuidFromGroupCoursesFragment(groupCoursesFragment: GroupCoursesFragment): String {
        return groupCoursesFragment.requireArguments().getString(GroupFragment.GROUP_UUID)!!
    }

    @Named("GroupEditor ${GroupEditorActivity.GROUP_UUID}")
    @Provides
    fun provideGroupUuidFromGroupEditorFragment(fragment: GroupEditorFragment): String? {
        return fragment.requireActivity().intent.getStringExtra(GroupEditorActivity.GROUP_UUID)
    }

    @Named(SubjectEditorDialog.SUBJECT_UUID)
    @Provides
    fun provideSubjectUuid(dialog: SubjectEditorDialog): String? {
        return dialog.requireActivity().intent.getStringExtra(SubjectEditorDialog.SUBJECT_UUID)
    }

    @Named(TimetableFragment.GROUP_UUID)
    @Provides
    fun provideAffiliation(timetableFragment: TimetableFragment):String? {
        return timetableFragment.arguments?.getString(TimetableFragment.GROUP_UUID)
    }
    @Named(FullImageActivity.IMAGE_URL)
    @Provides
    fun providePhotoUrlFromFullAvatarActivity(fullAvatarActivity: FullAvatarActivity):String {
        return fullAvatarActivity.intent.getStringExtra(FullImageActivity.IMAGE_URL)!!
    }

    @Named(ProfileFragment.USER_UUID)
    @Provides
    fun provideUserUuidFromProfileFragment(profileFragment: ProfileFragment):String {
        return profileFragment.requireArguments().getString(ProfileFragment.USER_UUID)!!
    }

    @Named("Group ${GroupPreference.GROUP_UUID}")
    @Provides
    fun provideGroupUuidFromGroupFragment(groupFragment: GroupFragment):String? {
        return groupFragment.arguments?.getString(GroupFragment.GROUP_UUID)
    }

    @Named("UserEditor ${UserEditorActivity.USER_UUID}")
    @Provides
    fun provideUserUuidFromUserEditorActivity(userEditorActivity: UserEditorActivity):String? {
        return userEditorActivity.intent.getStringExtra(UserEditorActivity.USER_UUID)
    }

    @Named(UserEditorActivity.USER_ROLE)
    @Provides
    fun provideUserRole(userEditorActivity: UserEditorActivity):String {
        return userEditorActivity.intent.getStringExtra(UserEditorActivity.USER_ROLE)!!
    }


    @Named(UserEditorActivity.USER_GROUP_UUID)
    @Provides
    fun provideUserGroupUuid(userEditorActivity: UserEditorActivity):String? {
        return userEditorActivity.intent.getStringExtra(UserEditorActivity.USER_GROUP_UUID)
    }

  @Named(SpecialtyEditorDialog.SPECIALTY_UUID)
    @Provides
    fun provideSpecialtyUuid(specialtyEditorDialog: SpecialtyEditorDialog):String? {
        return specialtyEditorDialog.arguments?.getString(SpecialtyEditorDialog.SPECIALTY_UUID)
    }

    @Named(CourseFragment.COURSE_UUID)
    @Provides
    fun provideCourseUuidFromCourseFragment(courseFragment: CourseFragment):String {
        return courseFragment.requireArguments().getString(CourseFragment.COURSE_UUID)!!
    }

}