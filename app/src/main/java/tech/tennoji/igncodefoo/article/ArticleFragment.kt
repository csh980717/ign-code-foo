package tech.tennoji.igncodefoo.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import tech.tennoji.igncodefoo.R
import tech.tennoji.igncodefoo.databinding.FragmentArticleBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_START_INDEX = "startIndex"
private const val ARG_COUNT = "count"

/**
 * A simple [Fragment] subclass.
 * Use the [ArticleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArticleFragment : Fragment() {
    private var param1: Int? = null
    private var param2: Int? = null

    private val viewModel: ArticleViewModel by lazy {
        val factory = ArticleViewModelFactory()
        ViewModelProvider(this, factory).get(ArticleViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_START_INDEX)
            param2 = it.getInt(ARG_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentArticleBinding>(
            inflater, R.layout.fragment_article, container, false
        )
        binding.lifecycleOwner = this
        val adapter = ArticleAdapter(ArticleListener { slug -> viewModel.onCardClick(slug) })
        val recyclerView = binding.articleRecyclerView
        recyclerView.adapter = adapter
        viewModel.articleList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.openLink.observe(viewLifecycleOwner) {
            it?.let {
                val bundle = Bundle()
                bundle.putString("url", "https://ign.com/articles/$it")
                findNavController().navigate(R.id.action_mainFragment_to_webViewFragment, bundle)
                viewModel.onOpenLinkComplete()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(context, "Network error", Toast.LENGTH_LONG).show()
                viewModel.onErrorToastComplete()
            }
        }
        viewModel.getArticleData(param1!!, param2!!)
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param startIndex Parameter 1.
         * @param count Parameter 2.
         * @return A new instance of fragment ArticleFragment.
         */
        @JvmStatic
        fun newInstance(startIndex: Int = 0, count: Int = 10) =
            ArticleFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_START_INDEX, startIndex)
                    putInt(ARG_COUNT, count)
                }
            }
    }
}