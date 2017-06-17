package jp.hotdrop.compl.viewmodel

import android.content.Context
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.hotdrop.compl.dao.CompanyDao
import jp.hotdrop.compl.dao.TagDao
import javax.inject.Inject

class TagsAssociateViewModel @Inject constructor(val context: Context): ViewModel() {

    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var companyDao: CompanyDao
    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private var viewModels: ObservableList<TagAssociateViewModel> = ObservableArrayList()
    private lateinit var callback: Callback
    private val NOT_INIT_COMPANY_ID = -1
    private var companyId = NOT_INIT_COMPANY_ID

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun loadData(companyId: Int) {
        val disposable = tagDao.findAll()
                .map { tags ->
                    tags.map {
                        val isAssociatedWith = companyDao.hasAssociateTag(companyId, it.id)
                        TagAssociateViewModel(it, isAssociatedWith, context)
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onSuccess(it, companyId) },
                        { callback.showError(it) }
                )
        compositeDisposable.add(disposable)
    }

    private fun onSuccess(tagAssociateViewModels: List<TagAssociateViewModel>, companyId: Int) {
        this.companyId = companyId
        viewModels.clear()
        viewModels.addAll(tagAssociateViewModels)
    }

    fun getViewModels(): ObservableList<TagAssociateViewModel> {
        return viewModels
    }

    fun update() {
        if(companyId != NOT_INIT_COMPANY_ID) {
            companyDao.associateTagByCompany(companyId, viewModels.filter{ it.isAssociated }.map{ it.tag }.toList())
        }
    }

    fun destroy() {
        compositeDisposable.clear()
    }

    interface Callback {
        fun showError(throwable: Throwable)
    }
}