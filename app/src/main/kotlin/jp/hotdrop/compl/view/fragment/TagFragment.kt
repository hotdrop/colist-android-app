package jp.hotdrop.compl.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.view.MotionEventCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import com.google.android.flexbox.FlexboxLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.hotdrop.compl.R
import jp.hotdrop.compl.dao.TagDao
import jp.hotdrop.compl.databinding.FragmentTagBinding
import jp.hotdrop.compl.databinding.ItemTagBinding
import jp.hotdrop.compl.model.Tag
import jp.hotdrop.compl.view.ArrayRecyclerAdapter
import jp.hotdrop.compl.view.BindingHolder
import jp.hotdrop.compl.view.parts.ColorSpinner
import jp.hotdrop.compl.viewmodel.TagViewModel
import javax.inject.Inject

class TagFragment: BaseFragment() {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable
    @Inject
    lateinit var tagDao: TagDao

    private lateinit var binding: FragmentTagBinding
    private lateinit var adapter: FlexItemAdapter
    private lateinit var helper: ItemTouchHelper

    companion object {
        @JvmStatic val TAG: String = TagFragment::class.java.simpleName
        fun newInstance() = TagFragment()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        getComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTagBinding.inflate(inflater, container, false)
        loadData()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tagDao.updateAllOrder(adapter.getModels())
    }

    private fun loadData() {
        val disposable = tagDao.findAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { tags -> onLoadSuccess(tags) },
                        { throwable -> onLoadFailure(throwable) }
                )
        compositeDisposable.add(disposable)
    }

    private fun onLoadSuccess(tags: List<Tag>) {

        adapter = FlexItemAdapter(context)

        if(tags.isNotEmpty()) {
            adapter.addAll(tags.map { t -> TagViewModel(t, context, tagDao) })
            goneEmptyMessage()
        } else {
            visibleEmptyMessage()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = FlexboxLayoutManager()
        binding.recyclerView.adapter = adapter

        helper = ItemTouchHelper(TagItemTouchHelperCallback(adapter))
        binding.recyclerView.addItemDecoration(helper)
        helper.attachToRecyclerView(binding.recyclerView)

        binding.fabButton.setOnClickListener { showRegisterDialog() }
    }

    private fun onLoadFailure(e: Throwable) {
        Toast.makeText(activity, "failed load tags." + e.message, Toast.LENGTH_LONG).show()
    }

    private fun visibleEmptyMessage() {
        binding.listEmptyView.visibility = View.VISIBLE
    }

    private fun goneEmptyMessage() {
        binding.listEmptyView.visibility = View.GONE
    }

    /**
     * ダイアログで、入力した分類名に応じてボタンと注意書きの制御を行う拡張関数
     */
    private val REGISTER_MODE: Int = -1
    fun AppCompatEditText.changeTextListener(view: View, dialog: AlertDialog, editText: AppCompatEditText,
                                             tagId: Int = REGISTER_MODE, originName: String = "") =
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/*no op*/}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {/*no op*/ }
                override fun afterTextChanged(s: Editable?) {
                    val editTxt = editText.text.toString()
                    when {
                        editTxt == "" -> disableButton()
                        tagId == REGISTER_MODE -> {
                            if(tagDao.exist(editTxt)) {
                                disableButtonWithAttention()
                            } else {
                                enableButton()
                            }
                        }
                        else -> {
                            if(editTxt != originName && tagDao.exist(editTxt, tagId)) {
                                disableButtonWithAttention()
                            } else {
                                enableButton()
                            }
                        }
                    }
                }
                private fun disableButton() {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    view.findViewById(R.id.label_tag_attention).visibility = View.GONE
                }
                private fun disableButtonWithAttention() {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    view.findViewById(R.id.label_tag_attention).visibility = View.VISIBLE
                }
                private fun enableButton() {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    view.findViewById(R.id.label_tag_attention).visibility = View.GONE
                }
            })

    private fun showRegisterDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_tag, null)
        val editText = view.findViewById(R.id.text_tag_name) as AppCompatEditText
        val spinner = ColorSpinner(view.findViewById(R.id.spinner_color_type) as Spinner, context)
        val dialog = AlertDialog.Builder(context, R.style.DialogTheme)
                .setView(view)
                .setPositiveButton(R.string.dialog_add_button, { dialogInterface, _ ->
                    tagDao.insert(Tag().apply {
                        name = editText.text.toString()
                        colorType = spinner.getSelection()
                    })
                    val tag = tagDao.find(editText.text.toString())
                    adapter.add(TagViewModel(tag, context, tagDao))
                    dialogInterface.dismiss()
                    goneEmptyMessage()
                })
                .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        editText.changeTextListener(view, dialog, editText)
    }

    private fun showUpdateDialog(vm: TagViewModel) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_tag, null)
        val editText = view.findViewById(R.id.text_tag_name) as AppCompatEditText
        editText.setText(vm.viewName as CharSequence)
        val spinner = ColorSpinner(view.findViewById(R.id.spinner_color_type) as Spinner, context)
        spinner.setSelection(vm.tag.colorType)
        val dialog = AlertDialog.Builder(context, R.style.DialogTheme)
                .setView(view)
                .setPositiveButton(R.string.dialog_update_button, { dialogInterface, _ ->
                    vm.viewName = editText.text.toString()
                    vm.tag.colorType = spinner.getSelection()
                    tagDao.update(vm.makeTag())
                    adapter.refresh(vm)
                    dialogInterface.dismiss()
                })
                .setNegativeButton(R.string.dialog_delete_button, { dialogInterface, _ ->
                    tagDao.delete(vm.tag)
                    adapter.remove(vm)
                    if(adapter.itemCount == 0) {
                        visibleEmptyMessage()
                    }
                    dialogInterface.dismiss()
                })
                .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        editText.changeTextListener(view, dialog, editText, vm.tag.id, vm.viewName)
    }

    private fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        helper.startDrag(viewHolder)
    }

    inner class FlexItemAdapter(context: Context)
        : ArrayRecyclerAdapter<TagViewModel, BindingHolder<ItemTagBinding>>(context) {

        override fun onBindViewHolder(holder: BindingHolder<ItemTagBinding>?, position: Int) {
            holder ?: return
            val binding = holder.binding
            binding.viewModel = getItem(position)
            binding.cardView.setOnTouchListener { _, motionEvent ->
                if(MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                    onStartDrag(holder)
                }
                false
            }
            binding.cardView.setOnClickListener { showUpdateDialog(binding.viewModel) }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BindingHolder<ItemTagBinding> {
            return BindingHolder(context, parent, R.layout.item_tag)
        }

        fun refresh(vm: TagViewModel) {
            val position = adapter.getItemPosition(vm)
            if(position != -1) {
                adapter.getItem(position).change(vm)
                notifyItemChanged(position)
            }
        }

        fun add(vm: TagViewModel) {
            addItem(vm)
            notifyItemInserted(itemCount)
        }

        fun remove(vm: TagViewModel) {
            val position = adapter.getItemPosition(vm)
            if(position != -1) {
                adapter.removeItem(position)
                notifyItemRemoved(position)
            }
        }

        fun getModels(): List<Tag> {
            return list.map{ it.tag }.toList()
        }
    }

    /**
     * アイテム選択時のコールバッククラス
     */
    inner class TagItemTouchHelperCallback(val adapter: FlexItemAdapter): ItemTouchHelper.Callback() {

        val NONE_POSITION = -1
        var fromPosition = NONE_POSITION
        var toPosition = NONE_POSITION

        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            val dragFrags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            return makeMovementFlags(dragFrags, swipeFlags)
        }

        /**
         * flexbox-layoutを使用したアイテムのドラッグについて
         * 上下の場合は常に互いのアイテムをswapさせれば問題なかったがflexboxは上下でアイテムを飛び越えられるので
         * 隣接するアイテムのswapでは対応できない。
         * 従って、アイテムの位置情報dragFromとdragTをフィールドで保持し、ドラッグ完了後のclearViewで実際のリストを更新する
         */
        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            if(viewHolder == null || target == null) {
                return false
            }
            if(fromPosition == NONE_POSITION) {
                fromPosition = viewHolder.adapterPosition
            }
            toPosition = target.adapterPosition
            adapter.onItemMovedForFlexBox(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            return
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
            super.clearView(recyclerView, viewHolder)
            if(fromPosition != NONE_POSITION && toPosition != NONE_POSITION && fromPosition != toPosition) {
                adapter.onListUpdateForFlexBox(fromPosition, toPosition)
            }
            fromPosition = NONE_POSITION
            toPosition = NONE_POSITION
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

    }
}