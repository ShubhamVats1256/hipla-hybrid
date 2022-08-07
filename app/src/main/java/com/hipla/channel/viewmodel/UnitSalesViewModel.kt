import androidx.lifecycle.MutableLiveData
import com.hipla.channel.common.AppConfig
import com.hipla.channel.common.LogConstant
import com.hipla.channel.entity.*
import com.hipla.channel.entity.api.ApiError
import com.hipla.channel.entity.api.ifError
import com.hipla.channel.entity.api.ifSuccessful
import com.hipla.channel.repo.HiplaRepo
import com.hipla.channel.viewmodel.BaseViewModel
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class UnitSalesViewModel : BaseViewModel() {

    private val hiplaRepo: HiplaRepo by KoinJavaComponent.inject(HiplaRepo::class.java)
    private val pageSize: Int = AppConfig.PAGE_DOWNLOAD_SIZE
    private var currentPageAtomic: AtomicInteger = AtomicInteger(1)
    private var totalPageAtomic: AtomicInteger = AtomicInteger(1)
    private var isDownloading: AtomicBoolean = AtomicBoolean(false)
    private val unitList = mutableListOf<UnitInfo>()
    var unitListLiveData = MutableLiveData<List<UnitInfo>>()

    fun fetchUnits() {
        if (canDownload()) {
            launchIO {
                Timber.tag(LogConstant.FLOW_APP)
                    .d("downloading unit list for page ${currentPageAtomic.get()}")
                with(
                    hiplaRepo.fetchUnits(
                        currentPageAtomic.get(),
                        pageSize,
                        AppConfig.PAGE_INVENTORY.lowercase()
                    )
                ) {
                    ifSuccessful {
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("downloading unit list successful with size ${it.unitList?.size} for page ${currentPageAtomic.get()}")
                        totalPageAtomic = AtomicInteger(it.pagination.totalPage)
                        currentPageAtomic.getAndIncrement()
                        Timber.tag(LogConstant.FLOW_APP).d("totalPage : ${it.pagination.totalPage}")
                        if (it.unitList.isNullOrEmpty().not()) {
                            unitList.addAll(it.unitList!!)
                            unitListLiveData.postValue(unitList)
                        }
                        Timber.tag(LogConstant.FLOW_APP)
                            .d("loading sales unit list successful ${it.unitList?.size}")
                    }
                    ifError {
                        Timber.tag(LogConstant.FLOW_APP).e("unit api error")
                        (it.throwable as? ApiError)?.run {
                            Timber.tag(LogConstant.FLOW_APP)
                                .e("error list size ${this.errorList?.size}")
                            if (this.errorList?.isNotEmpty() == true) {
                                appEvent.tryEmit(
                                    AppEvent(
                                        id = API_ERROR,
                                        message = this.errorList.first().msg ?: "Connection error"
                                    )
                                )
                                Timber.tag(LogConstant.FLOW_APP).e("error")
                            }
                            Timber.tag(LogConstant.FLOW_APP).e("downloading sales user list failed")
                        }
                    }
                    isDownloading.set(false)
                }
            }
        }
    }

    private fun canDownload(): Boolean {
        Timber.tag(LogConstant.FLOW_APP)
            .d("current page: ${currentPageAtomic.get()}  is downloading: ${isDownloading.get()}")
        val canDownload =
            isDownloading.get().not() && currentPageAtomic.get() <= totalPageAtomic.get()
        Timber.tag(LogConstant.FLOW_APP).d("can download $canDownload")
        return canDownload
    }
}