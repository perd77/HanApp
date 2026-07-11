package com.mmcl.hanapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mmcl.hanapp.data.remote.dto.CreateItemRequest
import com.mmcl.hanapp.data.repository.ItemRepository
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.ActivityPostItemBinding
import com.mmcl.hanapp.util.InputValidator
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.mmcl.hanapp.data.repository.StorageRepository

// Screen for creating a FOUND or LOST post. Opened from the FAB.
// Validates all fields client-side, then submits to Supabase with the
// logged-in user's real ID attached (required by RLS).
class PostItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostItemBinding
    private lateinit var session: SessionManager
    private val repository = ItemRepository()
    private val storageRepository = StorageRepository()

    // Tracks which toggle is currently selected; defaults to FOUND.
    private var selectedPostType = "FOUND"

    // Holds the picked photo's URI for preview only — not uploaded until submit.
    private var selectedPhotoUri: Uri? = null

    // Key used to pass back which post type was created, so MainActivity
    // knows whether to open Discovered (FOUND) or Finding (LOST) afterward.
    companion object {
        const val EXTRA_POST_TYPE = "extra_post_type"
    }

    // Launches Android's built-in Photo Picker (no storage permission needed).
    // On success, shows the chosen image as a preview in the form.
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            showPhotoPreview(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setupDropdowns()
        setupToggle()
        binding.buttonClose.setOnClickListener { finish() }
        binding.buttonSubmit.setOnClickListener { attemptSubmit() }
        binding.photoPickerContainer.setOnClickListener { openPhotoPicker() }
        binding.buttonRemovePhoto.setOnClickListener { clearPhotoPreview() }
    }

    // Populates the category/location dropdowns from string-array resources.
    private fun setupDropdowns() {
        val categoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.category_options)
        )
        binding.dropdownCategory.setAdapter(categoryAdapter)

        val locationAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.location_options)
        )
        binding.dropdownLocation.setAdapter(locationAdapter)
    }

    // FOUND is selected by default; tapping either button updates the type.
    private fun setupToggle() {
        binding.toggleGroupType.check(binding.buttonTypeFound.id)
        binding.toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            selectedPostType = if (checkedId == binding.buttonTypeFound.id) "FOUND" else "LOST"
        }
    }

    // Validates every field; on success, submits the post.
    private fun attemptSubmit() {
        val name = binding.editTextName.text?.toString().orEmpty()
        val description = binding.editTextDescription.text?.toString().orEmpty()
        val category = binding.dropdownCategory.text?.toString().orEmpty()
        val location = binding.dropdownLocation.text?.toString().orEmpty()

        var hasError = false

        val nameResult = InputValidator.validateItemName(name)
        if (nameResult is InputValidator.Result.Invalid) {
            binding.inputLayoutName.error = nameResult.reason; hasError = true
        } else binding.inputLayoutName.error = null

        val descResult = InputValidator.validateDescription(description)
        if (descResult is InputValidator.Result.Invalid) {
            binding.inputLayoutDescription.error = descResult.reason; hasError = true
        } else binding.inputLayoutDescription.error = null

        if (category.isEmpty()) {
            binding.inputLayoutCategory.error = "Please select a category"; hasError = true
        } else binding.inputLayoutCategory.error = null

        if (location.isEmpty()) {
            binding.inputLayoutLocation.error = "Please select a location"; hasError = true
        } else binding.inputLayoutLocation.error = null

        if (hasError) return

        submitPost(name, description, category, location)
    }

    // Uploads the photo first (if one was selected), then creates the item
    // with the resulting public URL attached — or with no photo if none was picked.
    private fun submitPost(name: String, description: String, category: String, location: String) {
        val userId = session.getUserId()
        val username = session.getUsername()

        if (userId == null || username == null) {
            binding.inputLayoutName.error = "Session expired. Please log in again."
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            // Step 1: upload the photo, if one was selected.
            val photoUrl: String? = selectedPhotoUri?.let { uri ->
                when (val uploadResult = storageRepository.uploadPhoto(this@PostItemActivity, uri, userId)) {
                    is NetworkResult.Success -> uploadResult.data
                    is NetworkResult.Error -> {
                        setLoading(false)
                        binding.inputLayoutName.error = uploadResult.message
                        return@launch // stop here; don't create the item if the photo failed
                    }
                    is NetworkResult.Loading -> null
                }
            }

            // Step 2: create the item, including the photo URL if we got one.
            val request = CreateItemRequest(
                name = name.trim(),
                description = description.trim(),
                category = category,
                locationTag = location,
                postedBy = username,
                postType = selectedPostType,
                userId = userId,
                photoPath = photoUrl
            )

            when (val result = repository.createItem(request)) {
                is NetworkResult.Success -> {
                    // Pass back which type was posted, so MainActivity knows whether
                    // to open Discovered (FOUND) or Finding (LOST) afterward.
                    val resultIntent = Intent().apply {
                        putExtra(EXTRA_POST_TYPE, selectedPostType)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is NetworkResult.Error -> {
                    setLoading(false)
                    binding.inputLayoutName.error = result.message
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressSubmit.visibility = if (loading) View.VISIBLE else View.GONE
        binding.buttonSubmit.isEnabled = !loading
    }

    // Opens the system photo picker, restricted to images only.
    private fun openPhotoPicker() {
        pickPhotoLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Swaps the placeholder for the selected image and shows the remove button.
    private fun showPhotoPreview(uri: Uri) {
        binding.photoPlaceholder.visibility = View.GONE
        binding.imagePhotoPreview.visibility = View.VISIBLE
        binding.imagePhotoPreview.setImageURI(uri)
        binding.buttonRemovePhoto.visibility = View.VISIBLE
    }

    // Clears the selection and reverts to the placeholder state.
    private fun clearPhotoPreview() {
        selectedPhotoUri = null
        binding.imagePhotoPreview.setImageURI(null)
        binding.imagePhotoPreview.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.GONE
        binding.photoPlaceholder.visibility = View.VISIBLE
    }
}