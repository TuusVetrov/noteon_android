package ru.noteon.presentation.ui.list_folders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.noteon.databinding.FolderItemBinding
import ru.noteon.domain.model.FolderModel

class FoldersListAdapter(
    private val onFolderClick: (FolderModel) -> Unit
): RecyclerView.Adapter<FoldersListAdapter.FolderViewHolder>() {
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    fun submitList(folders: List<FolderModel>) {
        differ.submitList(folders)
    }

    fun getFolderId(position: Int): String {
        return differ.currentList.elementAt(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FolderViewHolder(
        FolderItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(differ.currentList[position], onFolderClick)
    }

    override fun getItemCount() = differ.currentList.size

    inner class FolderViewHolder(
        private val binding: FolderItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            folder: FolderModel,
            onFolderClick: (FolderModel) -> Unit,
        ) {
            with(binding){
                folderTitle.text = folder.folderName

                root.setOnClickListener { onFolderClick(folder) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FolderModel>() {
            override fun areItemsTheSame(oldItem: FolderModel, newItem: FolderModel) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: FolderModel, newItem: FolderModel) =
                oldItem == newItem
        }
    }
}