package jp.hotdrop.compl.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import jp.hotdrop.compl.dao.CompanyDao
import jp.hotdrop.compl.databinding.FragmentCompanyBinding
import jp.hotdrop.compl.model.Company
import jp.hotdrop.compl.view.StackedPageListener
import jp.hotdrop.compl.view.activity.ActivityNavigator
import jp.hotdrop.compl.viewmodel.CompanyViewModel
import javax.inject.Inject


class CompanyFragment : BaseFragment(), StackedPageListener {

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private lateinit var binding: FragmentCompanyBinding
    private lateinit var adapter: Adapter
    private var tabName = ""
    private var isRefresh = false

    companion object {
        @JvmStatic val TAG = CompanyFragment::class.java.simpleName!!
        fun newInstance() = CompanyFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCompanyBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        loadData()
        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        getComponent().inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        val refreshMode = data.getIntExtra(REFRESH_MODE, REFRESH_NONE)
        if(refreshMode == REFRESH_ALL) {
            isRefresh = true
            loadData()
            activity.intent.removeExtra(REFRESH_MODE)
        }
    }

    /**
     * Tabの切り替えとupdateFragmentでの更新時に無条件で呼ばれる。
     * そのため、グループなど修正した場合はここで検知してリフレッシュする。
     */
    override fun onResume() {
        super.onResume()
        // TODO REFRESH_NONEはちゃんとしたものにする
        val refreshMode = activity.intent.getIntExtra(REFRESH_MODE, REFRESH_NONE)
        if(refreshMode == REFRESH_ALL) {
            isRefresh = true
            loadData()
            activity.intent.removeExtra(REFRESH_MODE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Stopの場合、clearで一旦addしているオブジェクトを全てDisposeする。
        compositeDisposable.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Destroy時は以降addされることはないので完全にDisposeする。
        compositeDisposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onTop() {
        loadData()
    }

    private fun loadData() {
        showProgress()
        val disposable = CompanyDao.findAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { list -> onLoadSuccess(list) },
                        { throwable -> onLoadFailure(throwable) }
                )
        compositeDisposable.add(disposable)
    }

    private fun onLoadSuccess(companies: List<Company>) {
        // タブごとにリストを生成してフラグメントを作成する
        val companyByGroup = linkedMapOf<String, MutableList<CompanyViewModel>>()
        companies.forEach { company ->
            val key = company.getGroup().name
            when {
                companyByGroup.containsKey(key) -> companyByGroup[key]!!.add(CompanyViewModel(company))
                else -> companyByGroup.put(key, mutableListOf(CompanyViewModel(company)))
            }
        }

        adapter = Adapter(fragmentManager)

        if(companyByGroup.size > 0) {
            companyByGroup.forEach { title, list -> addFragment(title, list) }
            binding.listEmptyView.visibility = View.GONE
        } else {
            binding.listEmptyView.visibility = View.VISIBLE
        }

        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.addOnTabSelectedListener(SelectedTabListener(binding.viewPager))
        binding.fab.setOnClickListener { ActivityNavigator.showCompanyRegister(this, tabName, REQ_CODE_COMPANY_REGISTER) }

        if(isRefresh) {
            // もともと選択していたタブを選択状態にする
            binding.viewPager.currentItem = adapter.getPagePosition(tabName)
        }

        hideProgress()
    }

    private fun onLoadFailure(e: Throwable) {
        Toast.makeText(activity, "failed load companies." + e.message, Toast.LENGTH_LONG).show()
    }

    private fun showProgress() {
        binding.progressBarContainer.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBarContainer.visibility = View.GONE
    }

    private fun addFragment(title: String, itemList: MutableList<CompanyViewModel>) {
        val fragment = CompanyTabFragment.newInstance(itemList)
        adapter.add(title, fragment)
    }

    /**
     * アダプター
     */
    private inner class Adapter(fm: FragmentManager): FragmentStatePagerAdapter(fm) {

        private val fragments = mutableListOf<CompanyTabFragment>()
        private val titles = mutableListOf<String>()

        override fun getItem(position: Int): Fragment? {
            if(position >= 0 && position < fragments.size) {
                return fragments[position]
            }
            return null
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }

        fun add(title: String, fragment: CompanyTabFragment) {
            fragments.add(fragment)
            titles.add(title)
            notifyDataSetChanged()
        }

        fun getPagePosition(title: String?): Int {
            title ?: return 0
            return titles.takeWhile { it != title }.count()
        }
    }

    private inner class SelectedTabListener(viewPager: ViewPager): TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

        override fun onTabSelected(tab: TabLayout.Tab?) {
            super.onTabSelected(tab)
            tab ?: return
            tabName = adapter.getPageTitle(tab.position).toString()
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            super.onTabReselected(tab)
            tab ?: return
            (adapter.getItem(tab.position) as CompanyTabFragment).scrollUpToTop()
        }
    }
}
