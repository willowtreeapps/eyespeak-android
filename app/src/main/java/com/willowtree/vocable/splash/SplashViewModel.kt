package com.willowtree.vocable.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.willowtree.vocable.BaseViewModel
import com.willowtree.vocable.presets.PresetCategories
import com.willowtree.vocable.presets.PresetsRepository
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.room.CategoryPhraseCrossRef
import kotlinx.coroutines.launch
import org.koin.core.get
import org.koin.core.inject
import java.util.*

class SplashViewModel : BaseViewModel() {

    private val presetsRepository: PresetsRepository by inject()

    private val liveExitSplash = MutableLiveData<Boolean>()
    val exitSplash: LiveData<Boolean> = liveExitSplash

    init {
        populateDatabase()
    }

    private fun populateDatabase() {
        backgroundScope.launch {
            presetsRepository.populateDatabase()

            moveMySayings()

            liveExitSplash.postValue(true)
        }
    }

    private suspend fun moveMySayings() {
        // if the user has My Sayings phrases, we need to migrate them to a custom category
        if (presetsRepository.getPhrasesForCategory(PresetCategories.USER_FAVORITES.id).isNotEmpty()) {
            val allCategories = presetsRepository.getAllCategories()

            // Get the index of the first hidden category to find the sort order of new category
            var firstHiddenIndex = allCategories.indexOfFirst { it.hidden }
            if (firstHiddenIndex == -1) {
                firstHiddenIndex = allCategories.size
            }

            // Create a new custom category called "My Sayings" that is user-generated
            val newCategoryId = UUID.randomUUID().toString()
            val newCategory = Category(
                newCategoryId,
                System.currentTimeMillis(),
                true,
                null,
                mapOf(Pair(Locale.getDefault().toString(), "My Sayings")),
                false,
                firstHiddenIndex
            )

            presetsRepository.addCategory(newCategory)

            // Get the phrases from the old My Sayings category and add cross refs with the new category
            val mySayingsPhrases = presetsRepository.getPhrasesForCategory(PresetCategories.USER_FAVORITES.id)
            mySayingsPhrases.forEach {
                presetsRepository.addCrossRef(CategoryPhraseCrossRef(newCategoryId, it.phraseId))
            }

            // Get the old My Sayings category and delete it
            val mySayingsCategory = presetsRepository.getCategoryById(PresetCategories.USER_FAVORITES.id)
            presetsRepository.deleteCategory(mySayingsCategory)

            // Get the old My Sayings cross refs and delete them
            val mySayingsCrossRefs = presetsRepository.getCrossRefsForCategoryId(PresetCategories.USER_FAVORITES.id)
            mySayingsCrossRefs.forEach { presetsRepository.deleteCrossRef(it) }
        }
    }
}
