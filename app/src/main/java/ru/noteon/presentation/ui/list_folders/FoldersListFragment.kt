package ru.noteon.presentation.ui.list_folders

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Binder
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.noteon.R
import ru.noteon.core.utils.extensions.hiltMainNavGraphViewModels
import ru.noteon.core.utils.extensions.snackBar
import ru.noteon.core.utils.extensions.toStringOrEmpty
import ru.noteon.databinding.FragmentFoldersListBinding
import ru.noteon.domain.model.FolderModel
import ru.noteon.domain.model.NoteModel
import ru.noteon.presentation.ui.list_notes.ListNotesFragment
import ru.noteon.presentation.ui.list_notes.ListNotesFragmentDirections
import ru.noteon.presentation.ui.list_notes.NotesState
import ru.noteon.presentation.ui.list_notes.NotesViewModel
import ru.noteon.presentation.ui.list_notes.adapters.NotesListAdapter
import ru.noteon.presentation.ui.list_notes.adapters.SwipeToDeleteCallback

@AndroidEntryPoint
class FoldersListFragment : Fragment() {
    private lateinit var binding: FragmentFoldersListBinding
    private val viewModel: FoldersViewModel by hiltMainNavGraphViewModels()

    private val foldersAdapter by lazy { FoldersListAdapter(::onFolderClicked) }

    private lateinit var createDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFoldersListBinding.inflate(inflater, container, false)
        initInputDialog()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initElements()
        observeState()
        setupToolbar()
    }

    private fun initElements() {
        with(binding) {
            rvFolders.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = foldersAdapter
            }

            val rightSwipeHandler = object : SwipeToDeleteCallback(
                requireContext(),
                ResourcesCompat.getColor(resources, R.color.noteon_error, context?.theme),
                ResourcesCompat.getColor(resources, R.color.noteon_on_error, context?.theme)
            ) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = rvFolders.adapter as FoldersListAdapter

                    val folderId = adapter.getFolderId(viewHolder.adapterPosition)
                    viewModel.delete(folderId)
                }
            }

            val rightItemTouchHelper = ItemTouchHelper(rightSwipeHandler)
            rightItemTouchHelper.attachToRecyclerView(rvFolders)

            fabNewFolder.setOnClickListener {
                createDialog.show()
            }

            swipeToRefreshFolders.setOnRefreshListener {
                viewModel.syncFolders()
                swipeToRefreshFolders.isRefreshing = false
            }
        }
    }

    private fun initInputDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.create_folder_dialog, null)

        val inputText: TextInputEditText = dialogView.findViewById(R.id.folderNameTextInput)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val btnCreate: Button = dialogView.findViewById(R.id.btnCreate)

        builder.setView(dialogView)

        createDialog = builder.create()

        inputText.addTextChangedListener {
            btnCreate.isClickable = it.toString().trim().isNotEmpty()
        }

        btnCancel.setOnClickListener {
            inputText.text?.clear()
            createDialog.hide()
        }

        btnCreate.setOnClickListener {
            viewModel.add(inputText.text.toString())
            inputText.text?.clear()
            createDialog.hide()
        }
    }

    private fun setupToolbar() {
        with(binding) {
            val item = mainToolbar.menu.findItem(R.id.search)
            val searchView = item.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        searchFolders(it)
                    }
                    return true
                }
            })
        }
    }

    private fun searchFolders(query:String) {
        val searchQuery = "%$query%"

        viewModel.searchFolder(searchQuery).observe(this) { list ->
            list.let {
                foldersAdapter.submitList(it)
            }
        }
    }

    private fun onFolderClicked(folder: FolderModel) {
        findNavController().navigate(
            FoldersListFragmentDirections.actionFoldersListFragmentToListNotesFragment(folder.id, folder.folderName)
        )
    }

    private fun foldersStateHandler(state: FoldersState) {
        binding.swipeToRefreshFolders.isEnabled = state.isConnectivityAvailable == true

        if (state.isUserLoggedIn == false) {
          //  logout()
        }

        foldersAdapter.submitList(state.folders)

        val errorMessage = state.error
        if (errorMessage != null) {
            val message = getString(R.string.error_loading_title) + "\n" + errorMessage
            snackBar(message)
        }

        val isConnectivityAvailable = state.isConnectivityAvailable
        if (isConnectivityAvailable != null) {
            if (isConnectivityAvailable) {
                onConnectivityAvailable()
            } else {
                onConnectivityUnavailable()
            }
        }
    }

    private fun observeState() {
        viewModel.uiState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state -> foldersStateHandler(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    @SuppressLint("ResourceType")
    private fun onConnectivityUnavailable() {
        val backgroundColor =
            ResourcesCompat.getColor(resources, R.color.noteon_error, context?.theme)

        with(binding) {
            textNetworkStatus.apply {
                text = getString(R.string.text_no_connectivity)
                setBackgroundColor(backgroundColor)
            }.also { it.visibility = View.VISIBLE }
        }
    }

    @SuppressLint("ResourceType")
    private fun onConnectivityAvailable() {
        val backgroundColor =
            ResourcesCompat.getColor(resources, R.color.noteon_primary, context?.theme)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (shouldSyncFolders()) {
                    viewModel.syncFolders()
                }

                with(binding) {
                    textNetworkStatus.apply {
                        text = getString(R.string.text_connectivity)
                        setBackgroundColor(backgroundColor)
                    }.also {
                        it.animate()
                            .alpha(1f)
                            .setStartDelay(ListNotesFragment.ANIMATION_DURATION)
                            .setDuration(ListNotesFragment.ANIMATION_DURATION)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    it.visibility = View.GONE
                                }
                            })
                    }
                }
            }
        }
    }


    private fun shouldSyncFolders() = viewModel.uiState.value.error != null

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}