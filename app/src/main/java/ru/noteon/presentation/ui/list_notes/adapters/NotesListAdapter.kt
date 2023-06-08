package ru.noteon.presentation.ui.list_notes.adapters

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.*
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sophimp.are.inner.Html
import ru.noteon.databinding.NoteItemBinding
import ru.noteon.domain.model.NoteModel
import java.text.SimpleDateFormat
import java.util.*

class NotesListAdapter(
    private val onPinClick: (NoteModel) -> Unit,
    private val onNoteClick: (NoteModel) -> Unit
): RecyclerView.Adapter<NotesListAdapter.NoteViewHolder>() {
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    fun submitList(notes: List<NoteModel>) {
        differ.submitList(notes)
    }

    fun getNoteID(position: Int): String {
        return differ.currentList.elementAt(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NoteViewHolder(
        NoteItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = differ.currentList.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(differ.currentList[position], onPinClick, onNoteClick)
    }

    inner class NoteViewHolder(
        private val binding: NoteItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            note: NoteModel,
            onPinClick: (NoteModel) -> Unit,
            onNoteClick: (NoteModel) -> Unit,
        ) {
            with(binding) {
                // roflan code style
                if (note.title.trim().isEmpty()){
                    noteTitle.text = "Новая заметка"
                } else {
                    noteTitle.text = note.title
                }

                if (note.body.trim().isEmpty()){
                    noteBody.text = "Нет дополнительного текста"
                } else {
                  //  val body = SpannableStringBuilder(
                  //      Html.fromHtml(note.body, FROM_HTML_MODE_COMPACT)
                  //  ).toString()
                    noteBody.text = note.body
                }

                noteDate.text = getDate(note.created)
                notePinState.isChecked = note.isPinned
                notePinState.setOnClickListener { onPinClick(note) }
                root.setOnClickListener { onNoteClick(note) }
            }
        }
    }

    private fun getDate(time: Long): String {
        val currentTime = System.currentTimeMillis()

        val result = if (currentTime - time < DateUtils.DAY_IN_MILLIS) {
            SimpleDateFormat("HH:mm", Locale("ru"))
        } else if (currentTime - time < DateUtils.YEAR_IN_MILLIS)  {
            SimpleDateFormat("d MMM", Locale("ru"))
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale("ru"))
        }
        return result.format(Date(time))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NoteModel>() {
            override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel) =
                oldItem == newItem
        }
    }
}