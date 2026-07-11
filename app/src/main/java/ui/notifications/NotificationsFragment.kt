package com.mmcl.hanapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mmcl.hanapp.R
import com.mmcl.hanapp.data.session.SessionManager
import com.mmcl.hanapp.databinding.FragmentNotificationsBinding
import com.mmcl.hanapp.ui.adapter.IncomingClaimAdapter
import com.mmcl.hanapp.ui.adapter.OutgoingClaimAdapter
import com.mmcl.hanapp.ui.model.NotificationClaim
import com.mmcl.hanapp.ui.model.PostType
import com.mmcl.hanapp.util.NetworkResult
import kotlinx.coroutines.launch

// Notifications destination: shows claims on the user's own posts (with
// context-appropriate actions) and the status of claims the user submitted
// elsewhere (read-only).
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(SessionManager(requireContext()))
    }

    private lateinit var incomingAdapter: IncomingClaimAdapter
    private lateinit var outgoingAdapter: OutgoingClaimAdapter

    companion object {
        fun newInstance() = NotificationsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeIncoming()
        observeOutgoing()

        binding.swipeRefreshNotifications.setOnRefreshListener {
            viewModel.loadAll()
        }
    }

    private fun setupRecyclerViews() {
        incomingAdapter = IncomingClaimAdapter(
            onApprove = { claim -> confirmApprove(claim) },
            onReject = { claim -> confirmReject(claim) }
        )
        binding.recyclerIncoming.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incomingAdapter
        }

        outgoingAdapter = OutgoingClaimAdapter()
        binding.recyclerOutgoing.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = outgoingAdapter
        }
    }

    // Confirmation wording adapts to context: approving a FOUND-item claim
    // grants ownership; confirming a LOST-post response just acknowledges
    // "yes, that's mine" — different real-world meaning, different copy.
    private fun confirmApprove(claim: NotificationClaim) {
        val isLostPost = claim.itemPostType == PostType.LOST
        val title = if (isLostPost) "Confirm this is yours?" else "Approve this claim?"
        val message = if (isLostPost) {
            "This will mark the item as reunited with you and close out any other pending responses."
        } else {
            "This will mark the item as Claimed and reject any other pending claims on it."
        }

        AlertDialog.Builder(requireContext(), R.style.HanAppAlertDialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(if (isLostPost) "Yes, it's mine" else "Approve") { _, _ ->
                viewModel.approveClaim(claim.claimId) { result -> handleActionResult(result) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmReject(claim: NotificationClaim) {
        val isLostPost = claim.itemPostType == PostType.LOST
        val title = if (isLostPost) "Not your item?" else "Reject this claim?"

        AlertDialog.Builder(requireContext(), R.style.HanAppAlertDialog)
            .setTitle(title)
            .setPositiveButton(if (isLostPost) "Not mine" else "Reject") { _, _ ->
                viewModel.rejectClaim(claim.claimId) { result -> handleActionResult(result) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleActionResult(result: NetworkResult<Unit>) {
        if (result is NetworkResult.Error) {
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeIncoming() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.incomingState.collect { state ->
                    when (state) {
                        is NetworkResult.Loading -> {
                            binding.swipeRefreshNotifications.isRefreshing = true
                        }
                        is NetworkResult.Success -> {
                            binding.swipeRefreshNotifications.isRefreshing = false
                            incomingAdapter.submitList(state.data)
                            binding.textEmptyIncoming.visibility =
                                if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is NetworkResult.Error -> {
                            binding.swipeRefreshNotifications.isRefreshing = false
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeOutgoing() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.outgoingState.collect { state ->
                    when (state) {
                        is NetworkResult.Success -> {
                            outgoingAdapter.submitList(state.data)
                            binding.textEmptyOutgoing.visibility =
                                if (state.data.isEmpty()) View.VISIBLE else View.GONE
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        is NetworkResult.Loading -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}