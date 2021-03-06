package com.denchic45.kts.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.PickImageContract
import com.canhub.cropper.options
import com.denchic45.kts.R
import com.denchic45.kts.databinding.FragmentProfileBinding
import com.denchic45.kts.ui.base.BaseFragment
import com.denchic45.kts.ui.base.HasNavArgs
import com.denchic45.kts.ui.avatar.FullImageActivity
import com.denchic45.kts.ui.profile.fullAvatar.FullAvatarActivity
import com.example.appbarcontroller.appbarcontroller.AppBarController
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding>(
        R.layout.fragment_profile,
        R.menu.options_profile
    ), HasNavArgs<ProfileFragmentArgs> {

    override val navArgs: ProfileFragmentArgs by navArgs()

    override val binding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)
    override val viewModel: ProfileViewModel by viewModels { viewModelFactory }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppBarController.findController(requireActivity()).setExpanded(
            expand = true,
            animate = true
        )

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
            ) { s -> tvFullName.text = s }
            viewModel.showRole.observe(viewLifecycleOwner) { id ->
                tvRole.setText(id)
            }
            viewModel.showGroupInfo.observe(
                viewLifecycleOwner
            ) { s -> tvGroupInfo.text = s }

            viewModel.showEmail.observe(viewLifecycleOwner) { s: String? -> tvEmail.text = s }
            viewModel.infoVisibility.observe(
                viewLifecycleOwner
            ) { visible: Boolean -> llInfo.visibility = if (visible) View.VISIBLE else View.GONE }
            viewModel.groupInfoVisibility.observe(
                viewLifecycleOwner
            ) { visible: Boolean ->
                llGroup.visibility = if (visible) View.VISIBLE else View.GONE
            }

            viewModel.openFullImage.observe(viewLifecycleOwner) { url: String ->
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    androidx.core.util.Pair.create(
                        ivAvatar, ViewCompat.getTransitionName(ivAvatar)
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

    companion object {
        const val USER_ID = "USER_UUID"
    }
}