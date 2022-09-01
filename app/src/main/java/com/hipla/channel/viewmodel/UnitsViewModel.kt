import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.*
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ApiError
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.extension.toFloorInfo
import com.hipla.channel.extension.toFlowConfig
import com.hipla.channel.repo.HiplaRepo
import com.hipla.channel.viewmodel.BaseViewModel
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class UnitsViewModel : BaseViewModel() {

    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private val pageSize: Int = 1000
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    val unitMasterList = mutableListOf<UnitInfo>()
    var unitListLiveData = MutableLiveData<List<UnitInfo>>()
    var error = MutableLiveData<String>()
    lateinit var flowConfig: FlowConfig
    private var salesUserId: String? = null

    lateinit var selectedFloorInfo: FloorInfo


    fun extractArguments(arguments: Bundle?) {
        arguments?.let {
            salesUserId = it.getString(KEY_SALES_USER_ID)
            flowConfig = it.getString(KEY_FLOW_CONFIG)?.toFlowConfig()!!
            selectedFloorInfo = it.getString(KEY_FLOOR)!!.toFloorInfo()!!
            Timber.tag(LogConstant.CUSTOMER_INFO).d("floor selected: ${selectedFloorInfo.siteId}")
            Timber.tag(LogConstant.CUSTOMER_INFO).d("flow config: $flowConfig")
        }
    }

    fun fetchUnits(url : String) {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.SALES_LIST)
                    .d("downloading unit list for page ${currentPageAtomic.get()}")
                with(
                    hiplaRepo.fetchUnits(
                        url = url,
                        currentPage = currentPageAtomic.get(),
                        pageSize = pageSize,
                        pageName = AppConfig.PAGE_UNITS,
                        appCode = flowConfig.appCode,
                    )
                ) {
                    ifSuccessful { unitPageResponse ->
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("downloading unit list successful with size ${unitPageResponse.unitList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(unitPageResponse.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.SALES_LIST)
                            .d("totalPage : ${unitPageResponse.pagination.totalPage}")
                        if (unitPageResponse.unitList.isNullOrEmpty().not()) {
                            val unitListForSelectedFloor = unitPageResponse.unitList
                            unitMasterList.addAll(unitListForSelectedFloor!!)
                            unitListLiveData.postValue(unitListForSelectedFloor!!)
                        }
                        else{
                            error.postValue("error")
                        }

                        Timber.tag(LogConstant.SALES_LIST)
                            .d("loading unit list successful ${unitPageResponse.unitList?.size}")
                    }
                    ifError {
                        error.postValue("error")


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