package jp.hotdrop.comlis.viewmodel

import android.content.Context
import android.databinding.Bindable
import android.view.View
import io.reactivex.Single
import jp.hotdrop.comlis.BR
import jp.hotdrop.comlis.model.Tag
import jp.hotdrop.comlis.model.TagAssociateState
import jp.hotdrop.comlis.repository.category.CategoryRepository
import jp.hotdrop.comlis.repository.company.CompanyRepository
import javax.inject.Inject

class CompaniesViewModel @Inject constructor(
        private val context: Context,
        private val companyRepository: CompanyRepository,
        private val categoryRepository: CategoryRepository
): ViewModel() {

    @get:Bindable
    var companiesEmptyMessageVisibility = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.companiesEmptyMessageVisibility)
        }

    fun getData(categoryId: Int): Single<List<CompanyViewModel>> =
            companyRepository.findByCategory(categoryId)
                    .map { companies ->
                        companies.map {
                            CompanyViewModel(it, context, companyRepository, categoryRepository)
                        }
                    }

    fun getCompanyViewModel(companyId: Int) =
            companyRepository.find(companyId).let {
                CompanyViewModel(it, context, companyRepository, categoryRepository)
            }

    fun updateItemOrder(companyIds: List<Int>) {
        companyRepository.updateAllOrder(companyIds)
    }

    // Unconditionally associate. Because this view model displays only associated tags.
    fun getTagAssociateViewModel(tag: Tag) =
            TagAssociateViewModel(tag, TagAssociateState.ASSOCIATED, context)

    fun visibilityEmptyMessageOnScreen() {
        companiesEmptyMessageVisibility = View.VISIBLE
    }

    fun goneEmptyMessageOnScreen() {
        companiesEmptyMessageVisibility = View.GONE
    }
}