package com.example.tfdemo.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                2 -> navigateToMigrateMainActivity()
                3 -> navigateToCifar10MainActivity()
                4 -> navigateToGyroMainActivity()
                5 -> navigateToDownload()
                6 -> navigateToNativeCalcBenchmark()
                7 -> navigateToImageFlip()
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

    private fun navigateToMigrateMainActivity() {
        try {
            val intent = Intent().apply {
                setClassName(
                    "com.example.executorchllamademo",
                    "com.example.executorchllamademo.MainActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // 如果无法找到Activity，显示错误提示
            android.util.Log.e("DemoListFragment", "Failed to launch migrate MainActivity", e)
            Toast.makeText(
                requireContext(),
                "无法启动 LLM Demo，请确保 migrate 模块已正确配置",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // 其他异常处理
            android.util.Log.e("DemoListFragment", "Failed to launch migrate MainActivity", e)
            Toast.makeText(
                requireContext(),
                "启动 LLM Demo 时发生错误: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToCifar10MainActivity() {
        findNavController().navigate(R.id.actionDemoListToCifar)
    }

    private fun navigateToGyroMainActivity() {
        findNavController().navigate(R.id.actionDemoListToGyro)
    }

    private fun navigateToDownload() {
        findNavController().navigate(R.id.actionDemoListToDownload)
    }

    private fun getDemoItems(): List<DemoItem> {
        return listOf(
            DemoItem(
                id = 1,
                title = getString(R.string.image_classification_demo_title),
                description = getString(R.string.image_classification_demo_description),
                iconResId = android.R.drawable.ic_menu_camera
            ),
            DemoItem(
                id = 2,
                title = getString(R.string.migrate_demo_title),
                description = getString(R.string.migrate_demo_description),
                iconResId = android.R.drawable.ic_menu_edit
            ),
            DemoItem(
                id = 3,
                title = getString(R.string.cifar10_demo_title),
                description = getString(R.string.cifar10_demo_description),
                iconResId = android.R.drawable.ic_menu_manage
            ),
            DemoItem(
                id = 4,
                title = getString(R.string.gyro_demo_title),
                description = getString(R.string.gyro_demo_description),
                iconResId = android.R.drawable.ic_menu_compass
            ),
            DemoItem(
                id = 5,
                title = "Network Download",
                description = "Multi-thread download demo",
                iconResId = com.example.tfdemo.R.drawable.ic_download
            ),
            DemoItem(
                id = 6,
                title = "Native Calc Benchmark",
                description = "Java VS C++ JNI performance",
                iconResId = android.R.drawable.ic_menu_info_details
            ),
            DemoItem(
                id = 7,
                title = "Image Flip",
                description = "Horizontal flip by C++ or Java",
                iconResId = android.R.drawable.ic_menu_gallery
            )
            // 可以添加更多demo项
        )
    }

    private fun navigateToNativeCalcBenchmark() {
        findNavController().navigate(R.id.actionDemoListToNativeCalcBenchmark)
    }

    private fun navigateToImageFlip() {
        findNavController().navigate(R.id.actionDemoListToImageFlip)
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
