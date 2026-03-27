package com.estef.antiphishingcoach.presentation.avatar

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.estef.antiphishingcoach.core.avatar.AvatarCatalog
import com.estef.antiphishingcoach.databinding.DialogAvatarPickerBinding
import com.estef.antiphishingcoach.presentation.common.renderAvatar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AvatarPickerDialogFragment : DialogFragment() {

    private var _binding: DialogAvatarPickerBinding? = null
    private val binding: DialogAvatarPickerBinding
        get() = _binding ?: error("Binding no disponible")

    private lateinit var requestKey: String
    private lateinit var adapter: AvatarOptionAdapter
    private var selectedAvatarId: String = AvatarCatalog.DEFAULT_AVATAR_ID

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAvatarPickerBinding.inflate(requireActivity().layoutInflater)
        requestKey = requireArguments().getString(ARG_REQUEST_KEY).orEmpty()
        selectedAvatarId = AvatarCatalog.resolveAvatarId(
            requireArguments().getString(ARG_SELECTED_AVATAR_ID)
        )

        adapter = AvatarOptionAdapter { avatarId ->
            selectedAvatarId = avatarId
            renderSelection()
        }

        binding.recyclerAvatarOptions.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerAvatarOptions.adapter = adapter
        adapter.submitOptions(AvatarCatalog.all(), selectedAvatarId)

        binding.btnAvatarPickerCancel.setOnClickListener { dismiss() }
        binding.btnAvatarPickerSave.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                requestKey,
                bundleOf(RESULT_AVATAR_ID to selectedAvatarId)
            )
            dismiss()
        }

        renderSelection()

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun renderSelection() {
        val selectedOption = AvatarCatalog.resolve(selectedAvatarId)
        binding.ivAvatarPickerPreview.renderAvatar(selectedOption.id)
        binding.tvAvatarPickerSelected.setText(selectedOption.labelRes)
        adapter.updateSelectedAvatar(selectedOption.id)
    }

    companion object {
        private const val ARG_REQUEST_KEY = "request_key"
        private const val ARG_SELECTED_AVATAR_ID = "selected_avatar_id"

        const val RESULT_AVATAR_ID = "avatar_id"
        const val TAG = "AvatarPickerDialog"

        fun newInstance(requestKey: String, selectedAvatarId: String?): AvatarPickerDialogFragment {
            return AvatarPickerDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_SELECTED_AVATAR_ID to selectedAvatarId
                )
            }
        }
    }
}
