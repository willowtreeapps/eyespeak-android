package com.willowtree.vocable.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.willowtree.vocable.BaseFragment
import com.willowtree.vocable.BindingInflater
import com.willowtree.vocable.R
import com.willowtree.vocable.databinding.CategoryEditButtonBinding
import com.willowtree.vocable.databinding.FragmentEditCategoriesListBinding
import com.willowtree.vocable.presets.PresetCategories
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.utils.LocalizedResourceUtility
import org.koin.android.ext.android.inject

class EditCategoriesListFragment : BaseFragment<FragmentEditCategoriesListBinding>() {

    companion object {
        private const val KEY_START_POSITION = "KEY_START_POSITION"
        private const val KEY_END_POSITION = "KEY_END_POSITION"

        fun newInstance(
            startPosition: Int,
            endPosition: Int
        ): EditCategoriesListFragment {
            return EditCategoriesListFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_START_POSITION, startPosition)
                    putInt(KEY_END_POSITION, endPosition)
                }
            }
        }
    }

    override val bindingInflater: BindingInflater<FragmentEditCategoriesListBinding> =
        FragmentEditCategoriesListBinding::inflate
    private val editCategoriesViewModel: EditCategoriesViewModel by inject()
    private var maxEditCategories = 1

    private var startPosition = 0
    private var endPosition = 0

    private val editButtonList = mutableListOf<CategoryEditButtonBinding>()

    private val localizedResourceUtility: LocalizedResourceUtility by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        maxEditCategories = resources.getInteger(R.integer.max_edit_categories)

        startPosition = arguments?.getInt(KEY_START_POSITION, 0) ?: 0
        endPosition = arguments?.getInt(KEY_END_POSITION, 0) ?: 0

        val numberOfButtons = endPosition - startPosition

        for (i in 0 until numberOfButtons) {
            val categoryView =
                CategoryEditButtonBinding.inflate(
                    inflater,
                    binding.categoryEditButtonContainer,
                    false
                )
            binding.categoryEditButtonContainer.addView(categoryView.root)

            editButtonList.add(categoryView)
        }

        // Add invisible views to fill out the rest of the space
        for (i in 0 until maxEditCategories - numberOfButtons) {
            val hiddenButton =
                CategoryEditButtonBinding.inflate(
                    inflater,
                    binding.categoryEditButtonContainer,
                    false
                )
            binding.categoryEditButtonContainer.addView(hiddenButton.root.apply {
                isEnabled = false
                visibility = View.INVISIBLE
            })
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToViewModel()
        editCategoriesViewModel.refreshCategories()
    }

    override fun getAllViews(): List<View> {
        return emptyList()
    }

    private fun subscribeToViewModel() {
        editCategoriesViewModel.orderCategoryList.observe(viewLifecycleOwner, Observer {
            it?.let { overallList ->
                if (startPosition >= 0 && endPosition <= overallList.size) {
                    var firstHiddenIndex = overallList.indexOfFirst { category -> category.hidden }
                    // Just default to list size if no categories are hidden
                    if (firstHiddenIndex == -1) {
                        firstHiddenIndex = overallList.size
                    }

                    overallList.subList(startPosition, endPosition)
                        .forEachIndexed { index, category ->
                            bindCategoryEditButton(
                                editButtonList[index],
                                category,
                                startPosition + index,
                                firstHiddenIndex
                            )
                        }
                }
            }
        })
    }

    private fun bindCategoryEditButton(
        editButtonBinding: CategoryEditButtonBinding,
        category: Category,
        overallIndex: Int,
        firstHiddenIndex: Int
    ) {
        with(editButtonBinding) {
            val categoryNameText = localizedResourceUtility.getTextFromCategory(category)
            if (category.hidden) {
                categoryName.text = categoryNameText
            } else {
                categoryName.text = getString(
                    R.string.edit_categories_button_number,
                    overallIndex + 1,
                    categoryNameText
                )
            }

            moveCategoryUpButton.isEnabled = !category.hidden && overallIndex > 0
            moveCategoryDownButton.isEnabled =
                !category.hidden && overallIndex + 1 < firstHiddenIndex

            moveCategoryUpButton.action = {
                editCategoriesViewModel.moveCategoryUp(category)
            }
            moveCategoryDownButton.action = {
                editCategoriesViewModel.moveCategoryDown(category)
            }

            if (category.categoryId == PresetCategories.USER_FAVORITES.id) {
                editCategorySelectButton.isEnabled = false
            }

            with(editCategorySelectButton) {
                // enable the button if the category is user-generated, or if it's My Sayings
                isEnabled =
                    category.isUserGenerated || category.categoryId == PresetCategories.USER_FAVORITES.id

                action = {
                    val action =
                        EditCategoriesFragmentDirections.actionEditCategoriesFragmentToEditCategoryOptionsFragment(
                            category
                        )
                    if (findNavController().currentDestination?.id == R.id.editCategoriesFragment) {
                        findNavController().navigate(action)
                    }
                    editCategoriesViewModel.onCategorySelected(category)
                }
            }
        }

        with(editButtonBinding.showHideCategoryButton) {
            if (!category.hidden) {
                setImageResource(R.drawable.button_hidden)
                setBackgroundResource(R.drawable.button_default_background)
            } else {
                setImageResource(R.drawable.button_shown)
                setBackgroundResource(R.drawable.category_button_background)
            }


            action = {
                category.let { category ->
                    editCategoriesViewModel.hideShowCategory(category, !category.hidden)
                }
            }
        }
    }
}