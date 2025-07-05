package com.nightlynexus.bataway

import android.app.PendingIntent
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.paging.PositionalDataSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nightlynexus.bataway.AdNotificationAdapter.ViewHolder
import com.squareup.sqldelight.android.paging.QueryDataSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executor
import javax.inject.Inject

internal class AdNotificationListView(
  context: Context,
  attributes: AttributeSet
) : RecyclerView(context, attributes) {
  @Inject lateinit var adNotificationQueries: AdNotificationQueries

  @Inject @AdNotificationContentIntents
  lateinit var adNotificationContentIntents: Map<String, PendingIntent>
  private val liveData: LiveData<PagedList<AdNotificationDisplay>>
  private val observer: Observer<PagedList<AdNotificationDisplay>>
  private val adNotificationAdapter: AdNotificationAdapter
  private lateinit var scope: CoroutineScope

  // Consider "now" check from time of this list view's creation.
  private val dateFormatter = DateFormatter(
    TimeZone.getDefault(),
    resources.configuration.locales[0]!!,
    DateFormat.is24HourFormat(context)
  )

  init {
    context.appComponent.inject(this)
    val inflater = LayoutInflater.from(context)
    adNotificationAdapter = AdNotificationAdapter(inflater, dateFormatter).apply {
      registerAdapterDataObserver(object : AdapterDataObserver() {
        override fun onItemRangeInserted(
          positionStart: Int,
          itemCount: Int
        ) {
          if (!canScrollVertically(-1) && positionStart == 0) {
            scrollToPosition(0)
          }
        }
      })
    }
    adapter = adNotificationAdapter
    layoutManager = LinearLayoutManager(context)
    addItemDecoration(
      SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.ad_notification_spacing))
    )
    val dataSourceFactory = AdNotificationDisplayDataSourceFactory(
      QueryDataSourceFactory(
        queryProvider = adNotificationQueries::adNotifications,
        countQuery = adNotificationQueries.count(),
        transacter = adNotificationQueries
      ),
      adNotificationContentIntents
    )
    liveData = LivePagedListBuilder(
      dataSourceFactory,
      PagedList.Config.Builder()
        .setEnablePlaceholders(true)
        .setInitialLoadSizeHint(25)
        .setPageSize(15)
        .build()
    )
      .setFetchExecutor(FetchExecutor())
      .build()
    observer = Observer {
      adNotificationAdapter.submitList(it)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    scope = MainScope()
    liveData.observeForever(observer)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    liveData.removeObserver(observer)
    // Make the QueryDataSource remove itself as a listener from the Query.
    liveData.value!!.dataSource.invalidate()
    scope.cancel()
  }

  override fun onSaveInstanceState(): SavedState {
    return SavedState(
      liveData.value!!.snapshot(),
      super.onSaveInstanceState()
    )
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    state as SavedState
    val adNotificationDisplays = state.adNotificationDisplays
    val config = PagedList.Config.Builder()
      // AsyncPagedListDiffer cannot mix contiguous and non-contiguous lists.
      .setEnablePlaceholders(true)
      .setInitialLoadSizeHint(adNotificationDisplays.size)
      .setPageSize(1)
      .setPrefetchDistance(1)
      .build()
    val pagedList = PagedList.Builder(StaticDataSource(adNotificationDisplays), config)
      .setNotifyExecutor(MainExecutor())
      .setFetchExecutor(MainExecutor())
      .build()
    adNotificationAdapter.submitList(pagedList)
    super.onRestoreInstanceState(state.savedState)
  }

  private inner class FetchExecutor : Executor {
    override fun execute(command: Runnable) {
      scope.launch(Dispatchers.IO) {
        command.run()
      }
    }
  }

  private inner class MainExecutor : Executor {
    override fun execute(command: Runnable) {
      scope.launch(Dispatchers.Main) {
        command.run()
      }
    }
  }

  class SavedState(
    val adNotificationDisplays: List<AdNotificationDisplay?>,
    val savedState: Parcelable?
  ) : Parcelable {
    override fun writeToParcel(
      parcel: Parcel,
      flags: Int
    ) {
      parcel.writeParcelable(savedState, flags)
      parcel.writeInt(adNotificationDisplays.size)
      for (i in adNotificationDisplays.indices) {
        val adNotificationDisplay = adNotificationDisplays[i]
        if (adNotificationDisplay == null) {
          parcel.writeInt(0)
        } else {
          parcel.writeInt(1)
          adNotificationDisplay.writeToParcel(parcel, flags)
        }
      }
    }

    override fun describeContents(): Int {
      return 0
    }

    companion object CREATOR : Creator<SavedState> {
      override fun createFromParcel(parcel: Parcel): SavedState {
        val savedState = parcel.readParcelable<Parcelable>(SavedState::class.java.classLoader)
        val size = parcel.readInt()
        val adNotificationDisplays = ArrayList<AdNotificationDisplay?>(size)
        for (i in 0 until size) {
          adNotificationDisplays += if (parcel.readInt() == 0) {
            null
          } else {
            AdNotificationDisplay.createFromParcel(parcel)
          }
        }
        return SavedState(adNotificationDisplays, savedState)
      }

      override fun newArray(size: Int): Array<SavedState?> {
        return arrayOfNulls(size)
      }
    }
  }
}

data class AdNotificationDisplay(
  val id: Long,
  val title: String,
  val message: String,
  val timestamp: Long,
  val contentIntent: PendingIntent?
) : Parcelable {
  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeLong(id)
    parcel.writeString(title)
    parcel.writeString(message)
    parcel.writeLong(timestamp)
    if (contentIntent == null) {
      parcel.writeInt(0)
    } else {
      parcel.writeInt(1)
      contentIntent.writeToParcel(parcel, flags)
    }
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<AdNotificationDisplay> {
    override fun createFromParcel(parcel: Parcel): AdNotificationDisplay {
      val id = parcel.readLong()
      val title = parcel.readString()!!
      val message = parcel.readString()!!
      val timestamp = parcel.readLong()
      val contentIntent = if (parcel.readInt() == 0) {
        null
      } else {
        PendingIntent.CREATOR.createFromParcel(parcel)
      }
      return AdNotificationDisplay(id, title, message, timestamp, contentIntent)
    }

    override fun newArray(size: Int): Array<AdNotificationDisplay?> {
      return arrayOfNulls(size)
    }
  }
}

private class AdNotificationAdapter(
  private val inflater: LayoutInflater,
  private val dateFormatter: DateFormatter
) : PagedListAdapter<AdNotificationDisplay, ViewHolder>(AdNotificationDiffCallback) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ) = ViewHolder(
    inflater.inflate(R.layout.ad_notification, parent, false) as AdNotificationView
  )

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val adNotification = getItem(position)
    if (adNotification == null) {
      holder.root.setPlaceholder()
    } else {
      holder.root.setAdNotification(adNotification, dateFormatter)
    }
  }

  class ViewHolder(val root: AdNotificationView) : RecyclerView.ViewHolder(root)

  private object AdNotificationDiffCallback : DiffUtil.ItemCallback<AdNotificationDisplay>() {
    override fun areItemsTheSame(
      oldItem: AdNotificationDisplay,
      newItem: AdNotificationDisplay
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
      oldItem: AdNotificationDisplay,
      newItem: AdNotificationDisplay
    ) = oldItem == newItem
  }
}

private fun AdNotification.asAdNotificationDisplay(adNotificationContentIntents: Map<String, PendingIntent>): AdNotificationDisplay {
  return AdNotificationDisplay(id, title, message, timestamp, adNotificationContentIntents[key])
}

internal class AdNotificationView(
  context: Context,
  attributes: AttributeSet
) : FrameLayout(context, attributes) {
  private val open: View
  private val timestamp: TextView
  private val title: TextView
  private val message: TextView
  private val date = Date()

  init {
    val inflater = LayoutInflater.from(context)
    inflater.inflate(R.layout.ad_notification_children, this, true)
    open = findViewById(R.id.ad_notification_open)
    timestamp = findViewById(R.id.ad_notification_timestamp)
    title = findViewById(R.id.ad_notification_title)
    message = findViewById(R.id.ad_notification_message)
  }

  fun setPlaceholder() {
    open.visibility = INVISIBLE
    timestamp.visibility = INVISIBLE
    title.visibility = INVISIBLE
    message.visibility = INVISIBLE
  }

  fun setAdNotification(
    adNotification: AdNotificationDisplay,
    dateFormatter: DateFormatter
  ) {
    val contentIntent = adNotification.contentIntent
    if (contentIntent == null) {
      open.visibility = INVISIBLE
      setOnClickListener(null)
    } else {
      open.visibility = VISIBLE
      setOnClickListener {
        contentIntent.send()
      }
    }
    timestamp.visibility = VISIBLE
    title.visibility = VISIBLE
    message.visibility = VISIBLE
    timestamp.text = dateFormatter.format(date.apply { time = adNotification.timestamp })
    title.text = adNotification.title
    message.text = adNotification.message
  }
}

class AdNotificationDisplayDataSourceFactory(
  private val delegate: QueryDataSourceFactory<AdNotification>,
  private val adNotificationContentIntents: Map<String, PendingIntent>
) :
  DataSource.Factory<Int, AdNotificationDisplay>() {
  override fun create(): PositionalDataSource<AdNotificationDisplay> {
    return AdNotificationDisplayDataSource(delegate.create(), adNotificationContentIntents)
  }

  class AdNotificationDisplayDataSource(
    private val delegate: PositionalDataSource<AdNotification>,
    private val adNotificationContentIntents: Map<String, PendingIntent>
  ) : PositionalDataSource<AdNotificationDisplay>() {
    override fun loadInitial(
      params: LoadInitialParams,
      callback: LoadInitialCallback<AdNotificationDisplay>
    ) {
      val delegate = object : PositionalDataSource.LoadInitialCallback<AdNotification>() {
        override fun onResult(
          data: List<AdNotification>,
          position: Int,
          totalCount: Int
        ) {
          callback.onResult(data.map {
            it.asAdNotificationDisplay(adNotificationContentIntents)
          }, position, totalCount)
        }

        override fun onResult(
          data: MutableList<AdNotification>,
          position: Int
        ) {
          callback.onResult(data.map {
            it.asAdNotificationDisplay(adNotificationContentIntents)
          }, position)
        }
      }
      this.delegate.loadInitial(params, delegate)
    }

    override fun loadRange(
      params: LoadRangeParams,
      callback: LoadRangeCallback<AdNotificationDisplay>
    ) {
      val delegate = object : PositionalDataSource.LoadRangeCallback<AdNotification>() {
        override fun onResult(data: List<AdNotification>) {
          callback.onResult(data.map {
            it.asAdNotificationDisplay(adNotificationContentIntents)
          })
        }
      }
      this.delegate.loadRange(params, delegate)
    }

    override fun addInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
      delegate.addInvalidatedCallback(onInvalidatedCallback)
    }

    override fun removeInvalidatedCallback(onInvalidatedCallback: InvalidatedCallback) {
      delegate.removeInvalidatedCallback(onInvalidatedCallback)
    }

    override fun invalidate() {
      delegate.invalidate()
    }

    override fun isInvalid(): Boolean {
      return delegate.isInvalid
    }
  }
}

class StaticDataSource<T>(private val items: List<T>) : PositionalDataSource<T>() {
  override fun loadInitial(
    params: LoadInitialParams,
    callback: LoadInitialCallback<T>
  ) {
    callback.onResult(items, 0, items.size)
  }

  override fun loadRange(
    params: LoadRangeParams,
    callback: LoadRangeCallback<T>
  ) {
    val start = params.startPosition
    val end = params.startPosition + params.loadSize
    callback.onResult(items.subList(start, end))
  }
}
