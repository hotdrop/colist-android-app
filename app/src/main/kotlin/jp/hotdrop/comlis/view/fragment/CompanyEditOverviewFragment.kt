package jp.hotdrop.comlis.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatEditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import jp.hotdrop.comlis.R
import jp.hotdrop.comlis.databinding.FragmentCompanyEditOverviewBinding
import jp.hotdrop.comlis.repository.category.CategoryRepository
import jp.hotdrop.comlis.view.parts.CategorySpinner
import jp.hotdrop.comlis.viewmodel.CompanyEditOverviewViewModel
import javax.inject.Inject

class CompanyEditOverviewFragment: BaseFragment() {

    @Inject
    lateinit var viewModel: CompanyEditOverviewViewModel
    @Inject
    lateinit var compositeDisposable: CompositeDisposable
    @Inject
    lateinit var categoryRepository: CategoryRepository

    private lateinit var binding: FragmentCompanyEditOverviewBinding
    private lateinit var categorySpinner: CategorySpinner
    private val companyId by lazy { arguments!!.getInt(EXTRA_COMPANY_ID) }

    companion object {
        fun create(companyId: Int) = CompanyEditOverviewFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_COMPANY_ID, companyId)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        getComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCompanyEditOverviewBinding.inflate(inflater, container, false)
        setHasOptionsMenu(false)

        binding.viewModel = viewModel

        viewModel.loadData(companyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = {
                            createObservableToEditTexts()
                            categorySpinner = CategorySpinner(binding.spinnerCategory, activity, categoryRepository).apply {
                                setSelection(viewModel.categoryId)
                            }
                            binding.updateButton.setOnClickListener{ onClickUpdate() }
                        },
                        onError = { showErrorAsToast(ErrorType.LoadFailureCompany, it) }
                )
                .addTo(compositeDisposable)

        return binding.root
    }

    private fun AppCompatEditText.createNotExistObservable() =
            RxTextView.textChangeEvents(this)
                    .filter { it.text().toString().isNotBlank() }
                    .map { !viewModel.existName(it.text().toString()) }
                    .distinctUntilChanged()

    private fun createObservableToEditTexts() {
        val nameNotBlankObservable = binding.txtName.createNotBlankObservable()
        val nameNotExistObservable = binding.txtName.createNotExistObservable()

        Observables.combineLatest(nameNotBlankObservable, nameNotExistObservable,
                { isNotBlack, isNotExist ->
                    when {
                        !isNotBlack -> setLabelNameAttentionToEmptyMessage()
                        !isNotExist -> setLabelNameAttentionToExistMessage()
                    }
                    (isNotBlack && isNotExist)
                })
                .subscribeBy(
                        onNext = {
                            if(it) enableUpdateButtonWithGoneNameAttention() else disableUpdateButtonWithNameAttention()
                        }
                )
                .addTo(compositeDisposable)
    }

    private fun setLabelNameAttentionToEmptyMessage() {
        context?.let {
            binding.labelNameAttention.text = it.getString(R.string.company_name_empty_attention)
        }
    }

    private fun setLabelNameAttentionToExistMessage() {
        context?.let {
            binding.labelNameAttention.text = it.getString(R.string.company_name_attention)
        }
    }

    private fun disableUpdateButtonWithNameAttention() {
        binding.labelNameAttention.visibility = View.VISIBLE
        binding.updateButton.disabledWithColor()
    }

    private fun enableUpdateButtonWithGoneNameAttention() {
        binding.labelNameAttention.visibility = View.GONE
        binding.updateButton.enabledWithColor(viewModel.getColorRes())
    }

    private fun onClickUpdate() {
        viewModel.update(categorySpinner.getSelection())

        activity?.run {
            val intent = Intent().apply {
                if(viewModel.isChangeCategory) putExtra(REFRESH_MODE, CHANGE_CATEGORY) else putExtra(REFRESH_MODE, UPDATE)
            }
            this.setResult(Activity.RESULT_OK, intent)
        }

        exit()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}