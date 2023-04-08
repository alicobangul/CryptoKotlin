package com.basesoftware.cryptokotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.basesoftware.cryptokotlin.adapter.CryptoAdapter
import com.basesoftware.cryptokotlin.databinding.ActivityMainBinding
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.basesoftware.cryptokotlin.roomdb.CryptoDao
import com.basesoftware.cryptokotlin.roomdb.CryptoDatabase
import com.basesoftware.cryptokotlin.service.CryptoAPI
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ## CryptoJava - KOTLIN Language
 *
 * * RxJAVA
 * * Coroutines
 * * Room
 * * Retrofit
 * * ViewBinding
 * * DataBinding
 * * AsyncListDiffer
 */

class MainActivity : AppCompatActivity() {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var arrayCrypto : ArrayList<CryptoRecyclerModel>
    private lateinit var cryptoAdapter: CryptoAdapter
    private val BASE_URL = "https://raw.githubusercontent.com/"

    private lateinit var cryptoAPI : CryptoAPI
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var cryptoDao: CryptoDao
    private lateinit var thread : Thread

    private var isRxJava = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AlertDialog.Builder(this).apply {
            setMessage("Kullanmak istediğiniz kütüphaneyi seçiniz") // AlertDialog mesajı verildi
                .setNegativeButton("RXJAVA") { _, _ -> isRxJava = true } // Buton text ve click aksiyonu eklendi
                .setPositiveButton("COROUTINES") { _, _ -> isRxJava = false } // Buton text ve click aksiyonu eklendi
                .setCancelable(false) // AlertDialog kapatılamaz
                .setOnDismissListener {

                    initialize() // Değişkenler başlatılıyor

                    controlDb() // Veritabanı kontrolü sağlanıyor

                }
                .show()
        }

    }

    private fun initialize() {

        // Database tanımlandı
        val cryptoDatabase = Room
            .databaseBuilder(applicationContext, CryptoDatabase::class.java, "Crypto") // Room builder
            .allowMainThreadQueries() // Main Thread sorgusu devre dışı bırakıldı
            .build()

        cryptoDao = cryptoDatabase.cryptoDao() // Dao tanımlandı

        arrayCrypto = arrayListOf() // Recyclerview adaptörüne verilecek liste oluşturuldu

        cryptoAdapter = CryptoAdapter() // Recyclerview adaptörü oluşturuldu

        compositeDisposable = CompositeDisposable() // CompositeDisposable oluşturuldu

        val gson = GsonBuilder().setLenient().create() // GsonBuilder oluşturuldu

        val retrofit = when(isRxJava) {

            // Eğer RxJAVA kullanılacak ise addCallAdapterFactory ekleniyor
            true -> Retrofit.Builder().baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            // Eğer RxJAVA kullanılmayacak ise addCallAdapterFactory eklenmiyor
            else -> Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        }

        cryptoAPI = retrofit.create(CryptoAPI::class.java) // Retrofit oluşturuldu

        binding.recyclerCrypto.apply {
            setHasFixedSize(true) // Recyclerview ölçüleri değişmeyecek optimizasyon yap
            layoutManager = LinearLayoutManager(this@MainActivity) // Viewholder için layout tipi seçildi
            adapter = cryptoAdapter // Adaptörü bağla
        }

        binding.swipeCrypto.apply {

            isEnabled = false // Swipe'ı kapat henüz veriler gelmedi

            setOnRefreshListener {

                isEnabled = false // Swipe kapatıldı

                isRefreshing = false // Swipe refresh animasyonu kapat

                when(arrayCrypto.isEmpty()) {

                    true -> controlApi() // Herhangi bir veri yok ilk deneme API olacak

                    else -> when(cryptoAdapter.getItem(0).isApiData) {

                        true -> Snackbar.make(binding.root, "API verisi şuan kullanımda", Snackbar.LENGTH_SHORT).show().also { isEnabled = true  }

                        else -> controlApi() // Eğer veritabanı verisi mevcut ise api verisi kontrol et

                    }

                }


            }

        }
    }



    private fun controlDb() {

        if(isRxJava) compositeDisposable.clear() // Disposable temizlendi

        when(isRxJava) {

            true -> compositeDisposable.add(cryptoDao.getAllDataRxJava() // Veritabanından liste döndürecek fonksiyon
                .subscribeOn(Schedulers.io()) // I/O thread üzerinde yürütülecek
                .observeOn(AndroidSchedulers.mainThread()) // Main Thread üzerinde gözlemlenecek
                .subscribe(this::controlDbData) // Database'den dönen veri burada kontrol edilecek
            )

            else -> CoroutineScope(Dispatchers.IO).launch {
                val dataList = cryptoDao.getAllDataCoroutines() // Database verisi çağırıldı
                withContext(Dispatchers.Main) { controlDbData(dataList) } // Gelen veri kontrol ediliyor
            }

        }

    }

    private fun controlDbData(dataList : List<CryptoModel>) {
        // Coroutines -> MainThread

        if(isRxJava) compositeDisposable.clear() // Disposable temizlendi

        Snackbar
            .make(binding.root, if(dataList.isEmpty()) "Veritabanı boş, API verisi kontrol ediliyor" else "Veritabanındaki veriler getiriliyor", Snackbar.LENGTH_SHORT)
            .addCallback(object : Snackbar.Callback(){
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    when(dataList.isEmpty()) {
                        true -> controlApi() // Api verisi kontrol ediliyor
                        else -> showData(dataList, false) // Veritabanı verisi gösterilecek
                    }
                }
            })
            .show()

    }

    private fun controlApi() {
        // Coroutines -> MainThread

        if(isRxJava) compositeDisposable.clear() // Disposable temizlendi

        when(isRxJava) {

            true -> compositeDisposable.add(cryptoAPI.getCryptoDataRxJava() // API'den liste döndürecek fonksiyon
                .subscribeOn(Schedulers.io()) // I/O thread üzerinde yürütülecek
                .observeOn(AndroidSchedulers.mainThread()) // Main Thread üzerinde gözlemlenecek
                .onErrorResumeNext(Observable.just(arrayListOf())) // Hata oluştuğunda
                .subscribe(this::controlApiData) // API'den dönen veri burada kontrol edilecek
            )

            else -> CoroutineScope(Dispatchers.IO).launch {

                val response = cryptoAPI.getCryptoDataCoroutines() // API verisi çağırılıyor [Response]

                withContext(Dispatchers.Main) { if(response.isSuccessful) response.body()?.let { controlApiData(it) } } // API Data kontrolü için ver

            }

        }

    }

    private fun controlApiData(dataList : List<CryptoModel>) {
        // Coroutines -> Main Thread

        if(isRxJava) compositeDisposable.clear() // Disposable temizlendi

        when(dataList.isEmpty()) {

            true -> Snackbar.make(binding.root, "API verisi alınamadı, refresh deneyin", Snackbar.LENGTH_SHORT).show().also { binding.swipeCrypto.isEnabled = true } // UI

            // Veritabanına yazılsın mı ?
            else -> Snackbar
                    .make(binding.root, "API verisi alındı, veritabanına yazılsın mı?", Snackbar.LENGTH_SHORT)
                    .setAction("EVET") { saveDbData(dataList) } // Veriyi kaydet
                    .show().also { showData(dataList, true) } // Verileri göster
        }

    }



    private fun showData(dataList : List<CryptoModel>, isFromApi : Boolean) {
        // Coroutines -> MainThread

        arrayCrypto.clear() // Şuanki listeyi temizle

        for (i in dataList.indices) {

            arrayCrypto.add( CryptoRecyclerModel(dataList[i].currency, dataList[i].price, isFromApi) ) // Adaptör için kaynağı belli olan veri modeli oluştur

            // AsyncListDiffer'a yeni liste gönderildi & Swipe izni verildi
            if(i == (dataList.size - 1)) cryptoAdapter.updateData(arrayCrypto).also { binding.swipeCrypto.isEnabled = true }

        }

    }

    private fun saveDbData(dataList : List<CryptoModel>) {

        when(isRxJava) {

            true -> {
                // İşlemler için yeni bir thread
                thread = Thread {

                    for (i in dataList.indices) {

                        cryptoDao.insertRxJava(dataList[i])
                            .subscribeOn(Schedulers.io()) // I/O Thread üzerinde
                            .observeOn(AndroidSchedulers.mainThread()) // Main thread gözlem
                            .subscribe() // Takip et

                        // Thread beklemeyi kes
                        if(i == (dataList.size - 1)) Snackbar.make(binding.root, "API verileri veritabanına yazıldı", Toast.LENGTH_SHORT).show().also { thread.interrupt() }

                    }

                }

                thread.start() // Thread'i başlat

            }

            else -> CoroutineScope(Dispatchers.IO).launch {

                for (i in dataList.indices) {
                    
                    cryptoDao.insertCoroutines(dataList[i]) // Veri ekleme fonksiyonu çağır

                    if (i == (dataList.size - 1)) {

                        withContext(Dispatchers.Main) { Snackbar.make(binding.root, "API verileri veritabanına yazıldı", Toast.LENGTH_SHORT).show() } // UI bilgilendirme

                    }

                }
            }

        }

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}