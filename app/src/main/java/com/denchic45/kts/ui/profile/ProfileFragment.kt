package com.denchic45.kts.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.PickImageContract
import com.canhub.cropper.options
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentProfileBinding
import com.denchic45.kts.ui.BaseFragment
import com.denchic45.kts.ui.avatar.FullImageActivity
import com.denchic45.kts.ui.group.GroupFragment
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController
import dagger.android.support.AndroidSupportInjection
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding>(R.layout.fragment_profile) {

    override val binding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)
    override val viewModel: ProfileViewModel by viewModels { viewModelFactory }
    private lateinit var menu: Menu
    private var actionBar: ActionBar? = null

    private var galleryResult = registerForActivityResult(PickImageContract()) {
        cropImage.launch(
            options(it) {
                setScaleType(CropImageView.ScaleType.FIT_CENTER)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                setAspectRatio(1, 1)
                setMaxZoom(4)
                setAutoZoomEnabled(true)
                setMultiTouchEnabled(true)
                setCenterMoveEnabled(true)
                setShowCropOverlay(true)
                setAllowFlipping(true)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
            }
        )
    }

    private val cropImage =
        registerForActivityResult(CropImageContract()) {
            it.uriContent?.let { uri ->
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver, uri
                    )
                    viewModel.onImageLoad(getBytesFromBitmap(bitmap))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
        viewModel.onCreateOptions()
        inflater.inflate(R.menu.options_profile, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppBarController.findController(requireActivity()).setExpanded(true, true)

        with(binding) {
            viewModel.showAvatar.observe(viewLifecycleOwner) { s ->
                Glide.with(this@ProfileFragment)
                    .load(s)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivAvatar)
            }
            llGroup.setOnClickListener { viewModel.onGroupInfoClick() }
            ivAvatar.setOnClickListener { viewModel.onAvatarClick() }
            viewModel.showFullName.observe(
                viewLifecycleOwner
            ) { s: String? -> tvFullName.text = s }
            viewModel.showRole.observe(viewLifecycleOwner) { id: Int? ->
                tvRole.setText(
                    id!!
                )
            }
            viewModel.showGroupInfo.observe(
                viewLifecycleOwner
            ) { s: String? -> tvGroupInfo.text = s }
            viewModel.showPhoneNum.observe(
                viewLifecycleOwner
            ) { s: String? -> tvPhoneNum.text = s }
            viewModel.showEmail.observe(viewLifecycleOwner) { s: String? -> tvEmail.text = s }
            viewModel.infoVisibility.observe(
                viewLifecycleOwner
            ) { visible: Boolean -> llInfo.visibility = if (visible) View.VISIBLE else View.GONE }
            viewModel.groupInfoVisibility.observe(
                viewLifecycleOwner
            ) { visible: Boolean ->
                llGroup.visibility = if (visible) View.VISIBLE else View.GONE
            }
            viewModel.openGroup.observe(viewLifecycleOwner) { id ->

                findNavController(view).navigate(
                    R.id.action_profileFragment_to_group,
                    Bundle().apply { putString(GroupFragment.GROUP_ID, id) }
                )
            }
            viewModel.optionVisibility.observe(
                viewLifecycleOwner
            ) { idWithVisibility: Pair<Int, Boolean> ->
                menu.findItem(
                    idWithVisibility.first
                ).isVisible = idWithVisibility.second
            }
            viewModel.openFullImage.observe(viewLifecycleOwner) { url: String ->
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    androidx.core.util.Pair.create(
                        ivAvatar, ViewCompat.getTransitionName(
                            ivAvatar
                        )
                    )
                )
                val intent = Intent(requireActivity(), FullAvatarActivity::class.java)
                intent.putExtra(FullImageActivity.IMAGE_URL, url)
                startActivity(intent, options.toBundle())
            }
            viewModel.openGallery.observe(viewLifecycleOwner) {
                val intent = Intent(
                    Intent.ACTION_PICK
                )
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                galleryResult.launch(false)
            }
        }


    }

    //    private fun createViewModelFactory(): ViewModelProvider.Factory {
//        return object : ViewModelProvider.Factory {
//            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//                return ProfileViewModel(
//                    requireActivity().application,
//                    arguments!!.getString(USER_UUID)!!
//                ) as T
//            }
//        }
//    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onResume() {
        super.onResume()
        actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.title = null
//        bnv = requireActivity().findViewById(R.id.bottom_nav_view)
//        bnv.visibility = View.GONE
    }

    companion object {
        const val USER_ID = "USER_UUID"
    }
}