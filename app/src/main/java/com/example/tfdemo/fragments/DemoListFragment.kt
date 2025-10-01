package com.example.tfdemo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfdemo.databinding.FragmentDemoListBinding
import com.example.tfdemo.databinding.ItemDemoBinding
import com.example.tfdemo.R

class DemoListFragment : Fragment() {

    private lateinit var binding: FragmentDemoListBinding
    private lateinit var demoAdapter: DemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDemoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        demoAdapter = DemoAdapter { demoItem ->
            when (demoItem.id) {
                1 -> navigateToImageClassification()
                // 可以添加更多demo的导航逻辑
            }
        }

        with(binding.rvDemoList) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = demoAdapter
        }

        demoAdapter.submitList(getDemoItems())
    }

    private fun navigateToImageClassification() {
        findNavController().navigate(R.id.actionDemoListToPermissions)
    }

    private fun getDemoItems(): List<DemoItem> {
        return listOf(
            DemoItem(
                id = 1,
                title = getString(R.string.image_classification_demo_title),
                description = getString(R.string.image_classification_demo_description),
                iconResId = android.R.drawable.ic_menu_camera
            )
            // 可以添加更多demo项
        )
    }

    data class DemoItem(
        val id: Int,
        val title: String,
        val description: String,
        val iconResId: Int
    )

    class DemoAdapter(private val onItemClick: (DemoItem) -> Unit) :
        RecyclerView.Adapter<DemoAdapter.DemoViewHolder>() {

        private val demoItems = mutableListOf<DemoItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
            val binding = ItemDemoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DemoViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
            holder.bind(demoItems[position])
        }

        override fun getItemCount(): Int = demoItems.size

        fun submitList(newList: List<DemoItem>) {
            demoItems.clear()
            demoItems.addAll(newList)
            notifyDataSetChanged()
        }

        inner class DemoViewHolder(private val binding: ItemDemoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(demoItem: DemoItem) {
                binding.tvTitle.text = demoItem.title
                binding.tvDescription.text = demoItem.description
                binding.ivIcon.setImageResource(demoItem.iconResId)

                binding.root.setOnClickListener {
                    onItemClick(demoItem)
                }
            }
        }
    }
}
