package tech.tennoji.igncodefoo.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.tennoji.igncodefoo.network.Api
import tech.tennoji.igncodefoo.network.ArticleResponse

class ArticleViewModel : ViewModel() {


    private var viewModelJob = SupervisorJob()

    private val _articleList = MutableLiveData<ArticleResponse>()
    val articleList: LiveData<ArticleResponse>
        get() = _articleList

    private val _openLink = MutableLiveData<String?>()
    val openLink: LiveData<String?>
        get() = _openLink

    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    fun getArticleData(startIndex: Int, count: Int) {
        viewModelScope.launch {
            val articleResponse =
                Api.retrofitService.getArticleListAsync(startIndex, count).await()
            val contentIdList = ArrayList<String>()
            articleResponse.data.forEach {
                contentIdList.add(it.contentId)
            }
            val contentIds = contentIdList.joinToString(",")
            val commentCountResponse = Api.retrofitService.getCommentCountAsync(contentIds).await()
            for (i in 0 until commentCountResponse.count) {
                for (j in 0 until commentCountResponse.count) {
                    if (articleResponse.data[i].contentId == commentCountResponse.content[j].id) {
                        articleResponse.data[i].commentCount = commentCountResponse.content[j].count
                    }
                }
            }
            _articleList.value = articleResponse
        }
    }

    fun onCardClick(slug: String) {
        _openLink.value = slug
    }

    fun onOpenLinkComplete() {
        _openLink.value = null
    }
}