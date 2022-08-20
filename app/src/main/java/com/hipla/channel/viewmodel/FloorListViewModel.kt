import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.API_ERROR
import com.hipla.channel.entity.AppEvent
import com.hipla.channel.entity.FlowConfig
import com.hipla.channel.entity.UnitInfo
import com.hipla.channel.entity.api.ApiError
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.extension.toFlowConfig
import com.hipla.channel.repo.HiplaRepo
import com.hipla.channel.viewmodel.BaseViewModel
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FloorListViewModel : BaseViewModel() {

    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private val pageSize: Int = AppConfig.PAGE_DOWNLOAD_SIZE
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    val unitMasterList = mutableListOf<UnitInfo>()
    var unitListLiveData = MutableLiveData<List<UnitInfo>>()
    lateinit var flowConfig: FlowConfig
    private var salesUserId: String? = null

    fun extractArguments(arguments: Bundle?) {
        arguments?.let {
            salesUserId = it.getString(KEY_SALES_USER_ID)
            flowConfig = it.getString(KEY_FLOW_CONFIG)?.toFlowConfig()!!
            Timber.tag(LogConstant.CUSTOMER_INFO).d("Flow config: $flowConfig")
        }
    }

    fun fetchUnits() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.SALES_LIST)
                    .d("downloading unit list for page ${currentPageAtomic.get()}")
                with(
                    hiplaRepo.fetchUnits(
                        currentPage = currentPageAtomic.get(),
                        pageSize = pageSize,
                        pageName = AppConfig.PAGE_UNITS,
                        appCode = flowConfig.appCode,
                    )
                ) {
                    ifSuccessful {
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("downloading unit list successful with size ${it.unitList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(it.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("totalPage : ${it.pagination.totalPage}")
                        if (it.unitList.isNullOrEmpty().not()) {
                            unitMasterList.addAll(it.unitList!!)
                            unitListLiveData.postValue(it.unitList!!)
                        }
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("loading unit list successful ${it.unitList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.SALES_LIST).e("unit api error")
                        (it.throwable as? ApiError)?.run {
                            Timber.tag(LogConstant.SALES_LIST)
                                .e("error unit list size ${this.errorList?.size}")
                            if (this.errorList?.isNotEmpty() == true) {
                                appEvent.tryEmit(
                                    AppEvent(
                                        id = API_ERROR,
                                        message = this.errorList.first().msg ?: "Connection error"
                                    )
                                )
                                Timber.tag(LogConstant.SALES_LIST).e("error")
                            }
                            Timber.tag(LogConstant.SALES_LIST).e("downloading unit list failed")
                        }
                    }
                    isDownloading.set(false)
                }
            }
        }
    }

    private fun canDownload(): Boolean {
        Timber.tag(LogConstant.SALES_LIST)
            .d("current page: ${currentPageAtomic.get()}  is downloading: ${isDownloading.get()}")
        val canDownload =
            isDownloading.get().not() && currentPageAtomic.get() <= totalPageAtomic.get()
        Timber.tag(LogConstant.SALES_LIST).d("can download $canDownload")
        return canDownload
    }
}