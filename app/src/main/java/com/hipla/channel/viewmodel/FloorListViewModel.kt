import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.KEY_FLOW_CONFIG
import com.hipla.channel.common.KEY_SALES_USER_ID
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.API_ERROR
import com.hipla.channel.entity.AppEvent
import com.hipla.channel.entity.FloorInfo
import com.hipla.channel.entity.FlowConfig
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
    private val pageSize: Int = 1000
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    val floorMasterList = mutableListOf<FloorInfo>()
    var floorListLiveData = MutableLiveData<List<FloorInfo>>()
    lateinit var flowConfig: FlowConfig
    private var salesUserId: String? = null
    private var floorIdToFloorInfoMap = mutableMapOf<Int, FloorInfo>()

    fun extractArguments(arguments: Bundle?) {
        arguments?.let {
            salesUserId = it.getString(KEY_SALES_USER_ID)
            flowConfig = it.getString(KEY_FLOW_CONFIG)?.toFlowConfig()!!
            Timber.tag(LogConstant.CUSTOMER_INFO).d("flow config: $flowConfig")
        }
    }

    fun fetchFloors() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.SALES_LIST)
                    .d("downloading floor list for page ${currentPageAtomic.get()}")
                with(
                    hiplaRepo.fetchFloors(
                        currentPage = currentPageAtomic.get(),
                        pageSize = pageSize,
                        pageName = AppConfig.PAGE_UNITS,
                        appCode = flowConfig.appCode,
                    )
                ) {
                    ifSuccessful {
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("downloading floor list successful with size ${it.floorList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(it.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("totalPage : ${it.pagination.totalPage}")
                        if (it.floorList.isNullOrEmpty().not()) {
                            val tempFloorList = mutableListOf<FloorInfo>()
                            it.floorList!!.forEach { floorInfo ->
                                if (floorIdToFloorInfoMap.containsKey(floorInfo.floorId).not()) {
                                    floorIdToFloorInfoMap[floorInfo.floorId] = floorInfo
                                    floorMasterList.add(floorInfo)
                                    tempFloorList.add(floorInfo)
                                }
                            }
                            floorListLiveData.postValue(tempFloorList)
                        }
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("loading unit list successful ${it.floorList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.SALES_LIST).e("floor api error")
                        (it.throwable as? ApiError)?.run {
                            Timber.tag(LogConstant.SALES_LIST)
                                .e("error floor list size ${this.errorList?.size}")
                            if (this.errorList?.isNotEmpty() == true) {
                                appEvent.tryEmit(
                                    AppEvent(
                                        id = API_ERROR,
                                        message = this.errorList.first().msg ?: "Connection error"
                                    )
                                )
                                Timber.tag(LogConstant.SALES_LIST).e("error")
                            }
                            Timber.tag(LogConstant.SALES_LIST).e("downloading floor list failed")
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