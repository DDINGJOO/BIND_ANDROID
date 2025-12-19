package com.teambind.bind_android.presentation.start.profilesetting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.teambind.bind_android.R
import com.teambind.bind_android.data.model.Genre
import com.teambind.bind_android.data.model.Instrument
import com.teambind.bind_android.databinding.DialogGenreInstrumentSelectBinding
import com.teambind.bind_android.databinding.ItemSelectionChipBinding

class GenreInstrumentSelectDialog : BottomSheetDialogFragment() {

    private var _binding: DialogGenreInstrumentSelectBinding? = null
    private val binding get() = _binding!!

    private var selectedGenres = mutableSetOf<Int>()
    private var selectedInstruments = mutableSetOf<Int>()
    private var initialTabIndex = 0

    var onSelectionCompleted: ((Set<Int>, Set<Int>) -> Unit)? = null

    override fun getTheme(): Int = R.style.Theme_BIND_ANDROID_BottomSheetDialog

    companion object {
        fun newInstance(
            selectedGenres: Set<Int>,
            selectedInstruments: Set<Int>,
            initialTabIndex: Int = 0
        ): GenreInstrumentSelectDialog {
            return GenreInstrumentSelectDialog().apply {
                this.selectedGenres = selectedGenres.toMutableSet()
                this.selectedInstruments = selectedInstruments.toMutableSet()
                this.initialTabIndex = initialTabIndex
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogGenreInstrumentSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupListeners()
    }

    private fun setupViewPager() {
        val pagerAdapter = SelectionPagerAdapter()
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "장르 (${selectedGenres.size})"
                1 -> "악기 (${selectedInstruments.size})"
                else -> ""
            }
        }.attach()

        // 초기 탭 설정
        binding.viewPager.setCurrentItem(initialTabIndex, false)
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            onSelectionCompleted?.invoke(selectedGenres.toSet(), selectedInstruments.toSet())
            dismiss()
        }
    }

    private fun updateTabTitles() {
        binding.tabLayout.getTabAt(0)?.text = "장르 (${selectedGenres.size})"
        binding.tabLayout.getTabAt(1)?.text = "악기 (${selectedInstruments.size})"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ViewPager Adapter
    inner class SelectionPagerAdapter : RecyclerView.Adapter<SelectionPagerAdapter.PageViewHolder>() {

        inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.page_selection_list, parent, false)
            return PageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            val layoutManager = FlexboxLayoutManager(holder.itemView.context).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
                justifyContent = JustifyContent.FLEX_START
            }
            holder.recyclerView.layoutManager = layoutManager

            when (position) {
                0 -> {
                    holder.recyclerView.adapter = GenreAdapter()
                }
                1 -> {
                    holder.recyclerView.adapter = InstrumentAdapter()
                }
            }
        }

        override fun getItemCount(): Int = 2
    }

    // Genre Adapter
    inner class GenreAdapter : RecyclerView.Adapter<GenreAdapter.ChipViewHolder>() {

        private val genres = Genre.entries.toList()

        inner class ChipViewHolder(private val binding: ItemSelectionChipBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(genre: Genre) {
                binding.tvChip.text = genre.displayName
                binding.tvChip.isSelected = selectedGenres.contains(genre.code)

                binding.root.setOnClickListener {
                    if (selectedGenres.contains(genre.code)) {
                        selectedGenres.remove(genre.code)
                    } else {
                        if (selectedGenres.size >= 3) {
                            Toast.makeText(context, "최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        selectedGenres.add(genre.code)
                    }
                    notifyItemChanged(adapterPosition)
                    updateTabTitles()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
            val binding = ItemSelectionChipBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ChipViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
            holder.bind(genres[position])
        }

        override fun getItemCount(): Int = genres.size
    }

    // Instrument Adapter
    inner class InstrumentAdapter : RecyclerView.Adapter<InstrumentAdapter.ChipViewHolder>() {

        private val instruments = Instrument.entries.toList()

        inner class ChipViewHolder(private val binding: ItemSelectionChipBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(instrument: Instrument) {
                binding.tvChip.text = instrument.displayName
                binding.tvChip.isSelected = selectedInstruments.contains(instrument.code)

                binding.root.setOnClickListener {
                    if (selectedInstruments.contains(instrument.code)) {
                        selectedInstruments.remove(instrument.code)
                    } else {
                        if (selectedInstruments.size >= 3) {
                            Toast.makeText(context, "최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        selectedInstruments.add(instrument.code)
                    }
                    notifyItemChanged(adapterPosition)
                    updateTabTitles()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
            val binding = ItemSelectionChipBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ChipViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
            holder.bind(instruments[position])
        }

        override fun getItemCount(): Int = instruments.size
    }
}
