package com.ssafy.daero.ui.root.search

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.daero.R
import com.ssafy.daero.application.App
import com.ssafy.daero.application.App.Companion.keyword
import com.ssafy.daero.application.App.Companion.userSeq
import com.ssafy.daero.base.BaseFragment
import com.ssafy.daero.databinding.*
import com.ssafy.daero.ui.adapter.search.SearchArticleMoreAdapter
import com.ssafy.daero.ui.root.sns.*
import com.ssafy.daero.ui.setting.BlockUserViewModel
import com.ssafy.daero.utils.constant.*
import com.ssafy.daero.utils.view.toast

class SearchContentMoreFragment : BaseFragment<FragmentSearchContentMoreBinding>(R.layout.fragment_search_content_more), ArticleListener {
    private val TAG = "SearchContentMore_DaeRo"
    private val searchContentMoreViewModel : SearchContentMoreViewModel by viewModels()
    private val articleViewModel: ArticleViewModel by viewModels()
    private val blockUserViewModel: BlockUserViewModel by viewModels()
    private lateinit var searchArticleMoreAdapter: SearchArticleMoreAdapter

    override fun init() {
        initView()
        initData()
        initAdapter()
        observeData()
        setOnClickListeners()
    }

    private fun initView(){
        binding.toolbarTitle.text = "\"$keyword\" 검색 결과"
    }

    private fun initData(){
        searchContentMoreViewModel.searchContentMore(keyword!!)
    }

    private fun initAdapter(){
        searchArticleMoreAdapter = SearchArticleMoreAdapter(
            onArticleClickListener,
            onUserClickListener,
            onCommentClickListener,
            onLikeClickListener,
            onLikeTextClickListener,
            onMenuClickListener
        )

        binding.recyclerSearchContentMore.adapter = searchArticleMoreAdapter
    }

    private fun observeData(){
        searchContentMoreViewModel.resultContentSearch.observe(viewLifecycleOwner){
            Log.d(TAG, "observeData: 여기")
            searchArticleMoreAdapter.submitData(lifecycle, it)
        }

        searchContentMoreViewModel.responseState.observe(viewLifecycleOwner){ state ->
            when(state){
                FAIL -> binding.textSearchContentMoreNoData.visibility = View.VISIBLE
            }
        }

        articleViewModel.likeState.observe(viewLifecycleOwner){
            when(it){
                SUCCESS -> {
                    articleViewModel.likeState.value = DEFAULT
                }
                FAIL -> {
                    articleViewModel.likeState.value = DEFAULT
                }
            }
        }
    }

    private fun setOnClickListeners(){
        binding.imgSearchContentMoreBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private val onArticleClickListener: (Int) -> Unit = { articleSeq ->
        findNavController().navigate(
            R.id.action_searchContentMoreFragment_to_articleFragment,
            bundleOf(ARTICLE_SEQ to articleSeq)
        )

    }

    private val onUserClickListener: (Int) -> Unit = { userSeq ->
        findNavController().navigate(
            R.id.action_searchContentMoreFragment_to_otherPageFragment,
            bundleOf(USER_SEQ to userSeq)
        )
    }

    private val onCommentClickListener: (Int, Int) -> Unit = { articleSeq, comments ->
        CommentBottomSheetFragment(articleSeq, comments, onUserClickListener)
            .show(childFragmentManager, COMMENT_BOTTOM_SHEET)
    }

    private val onLikeClickListener: (Int, Boolean) -> Unit = { articleSeq, likeYn ->
        when(likeYn){
            true -> articleViewModel.likeDelete(App.prefs.userSeq, articleSeq)
            false -> articleViewModel.likeAdd(App.prefs.userSeq, articleSeq)
        }
    }

    private val onLikeTextClickListener: (Int, Int) -> Unit = { articleSeq, likes ->
        LikeBottomSheetFragment(articleSeq, likes, onUserClickListener)
            .show(childFragmentManager, LIKE_BOTTOM_SHEET)
    }

    private val onMenuClickListener: (Int, Int) -> Unit = { articleSeq, userSeq ->
        ArticleMenuBottomSheetFragment(articleSeq, userSeq, 1,this@SearchContentMoreFragment).show(
            childFragmentManager,
            ARTICLE_MENU_BOTTOM_SHEET
        )
    }

    override fun articleDelete(articleSeq: Int){
        articleViewModel.articleDelete(articleSeq)
        articleViewModel.responseState.observe(viewLifecycleOwner){
            when(it){
                SUCCESS -> {
                    toast("해당 게시글을 삭제했습니다.")
                    searchContentMoreViewModel.searchContentMore(keyword!!)
                    articleViewModel.deleteState.value = DEFAULT
                }
                FAIL -> {
                    toast("게시글을 삭제했습니다.")
                    articleViewModel.deleteState.value = DEFAULT
                }
            }
        }
    }

    override fun blockArticle(articleSeq: Int){
        blockUserViewModel.blockArticle(articleSeq)
        blockUserViewModel.responseState.observe(viewLifecycleOwner){
            when(it){
                SUCCESS -> {
                    toast("해당 유저를 차단했습니다.")
                    searchArticleMoreAdapter.refresh()
                    blockUserViewModel.responseState.value = DEFAULT
                }
                FAIL -> {
                    toast("유저 차단을 실패했습니다.")
                    blockUserViewModel.responseState.value = DEFAULT
                }

            }
        }
    }

    override fun setPublic() {
        TODO("Not yet implemented")
    }

    override fun articleOpen(articleSeq: Int) {
        TODO("Not yet implemented")
    }

    override fun articleClose(articleSeq: Int) {
        articleViewModel.articleClose(articleSeq)
        articleViewModel.exposeState.observe(viewLifecycleOwner) {
            when (it) {
                SUCCESS -> {
                    toast("해당 게시글을 비공개하였습니다.")
                    articleViewModel.exposeState.value = DEFAULT
                }
                FAIL -> {
                    toast("게시글 공개 처리를 실패했습니다.")
                    articleViewModel.exposeState.value = DEFAULT
                }
            }
        }
    }
}