package com.mayanurlestari.akhirproject
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mayanurlestari.akhirproject.databinding.ActivityMainBinding
import com.mayanurlestari.akhirproject.model.InternalFileRepository
import com.mayanurlestari.akhirproject.model.Note
import com.mayanurlestari.akhirproject.model.NoteRepository
import java.text.SimpleDateFormat
import java.util.*

//menginport kelas atau paket-paket yang akan telah digunakan sebelumnya
class MainActivity : AppCompatActivity() {
    private val repo: NoteRepository by lazy { InternalFileRepository(this) }

    private fun InternalFileRepository(mainActivity: MainActivity): InternalFileRepository {
        TODO("Not yet implemented")
    }

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dataalamat: TextView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
//TODO 1 : mendeklarasikan variabel yang akan digunakan untuk menampilkan data sensor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mainBinding.btnLocation.setOnClickListener {
            mainBinding.btnlogfile.setOnClickListener {
//Sensor yang digunakan dalam GPS yang berfungsi untuk akurasi dan perkiraan yang akurat dalam mengaplikasikan sensornya
                var logDataSensor = mainBinding.editTeksCatatan.text.toString()
                val timeStamp: String = SimpleDateFormat("yy-MM-dd").format(Date())
                mainBinding.editFileName.setText("datalokasi_maya-" + timeStamp + ".txt")
                val logData1 = mainBinding.tvAddress.text.toString()
                logDataSensor = "$logDataSensor$logData1"
                mainBinding.editTeksCatatan.setText(logDataSensor)
            }
            //TODO 14 : menampilkan hasil data dari log file
            mainBinding.btnshare.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                var logData1 = mainBinding.tvAddress.text.toString()
                intent.putExtra(Intent.EXTRA_TEXT, logData1)
                intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")
                val chooser = Intent.createChooser(intent, "share : ")
                startActivity(chooser)
            }
            //TODO 13 : membagikan hasil data sensor yang telah tersimpan. Membagikannya bisa via media sosial, bluetooth, dan share file
            mainBinding.btnsave.setOnClickListener {
                if (mainBinding.editFileName.text.isNotEmpty()) {
                    try {
                        repo.addNote(
                            Note(
                                mainBinding.editFileName.text.toString(),
                                mainBinding.editTeksCatatan.text.toString()
                            )
                        )
                        //TODO 10 : menyimpan semua data sensor GPS yang telah di tampilkandalam bentuk txt
                    } catch (e: Exception) {
                        Toast.makeText(this, "File Write Failed", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                    mainBinding.editFileName.text.clear()
                    mainBinding.editTeksCatatan.text.clear()
                } else {
                    Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
                }
            }


            mainBinding.Read.setOnClickListener {
                if (mainBinding.editFileName.text.isNotEmpty()) {
                    try {
                        val note = repo.getNote(mainBinding.editFileName.text.toString())
                        mainBinding.editTeksCatatan.setText(note.noteText)
                    } catch (e: Exception) {
                        Toast.makeText(this, "File Read Failed", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                        //TODO 11 : membaca data sensor yang telah ditampilkan pada layout
                    }
                } else {
                    Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
                }
            }

            mainBinding.btnDelete.setOnClickListener {
                if (mainBinding.editFileName.text.isNotEmpty()) {
                    try {
                        if (repo.deleteNote(mainBinding.editFileName.text.toString())) {
                            Toast.makeText(this, "File Deleted", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "File Could Not Be Deleted", Toast.LENGTH_LONG)
                                .show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "File Delete Failed", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                        //TODO 12 : menghapus data sensor yang telah diperoleh dan disimpan sebelumnya
                    }
                    mainBinding.editFileName.text.clear()
                    mainBinding.editTeksCatatan.text.clear()
                } else {
                    Toast.makeText(this, "Please provide a Filename", Toast.LENGTH_LONG).show()
                }
            }

            getLocation()
            //GetFusedLocationProviderClient pada saat di create akan menampilkan sebuah informasi lokasi terkini
            //Pada saat getlocation diklik, maka akan menampilak get lokasi
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                //TODO 5 : menggunakan isLocationEnable untuk menentukan aktifasi GPS di perangkat fisik
                    //Sebelum menggunakan sensor, lebih baik aktifkan terlebih dahulu GPS di perangkat fisik masing-masing
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    // TODO 7 : mendapatkan data lokasi dari perangkat (latitude, longitude, alamat, nama negara, wilayah)
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        //TODO 6 : aplikasikan geocoder untuk mendapatkan data lokasi yang sebelumnya telah diaktigkan pada perangkat fisik
                        //menampilkan nilai dari geocoder berdasarkan latitude dan longitudenya
                        mainBinding.apply {
                            tvLatitude.text = "Latitude\n${list[0].latitude}"
                            tvLongitude.text = "Longitude\n${list[0].longitude}"
                            tvCountryName.text = "Nama Negara\n${list[0].countryName}"
                            tvLocality.text = "Wilayah/Area\n${list[0].locality}"
                            tvAddress.text = "Alamat\n${list[0].getAddressLine(0)}"
                            //TODO 8 : nilai hasil dari geocoder yang telah diakses akan tersimpan pada mainBinding.apply
                            //TODO 9 : data dari geocoder akan berada pada masing-masing direktori
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        //TODO 4 : memberikan estimasi lokasi perangkat dan memberikan estimasi lokasi perangkat seakurat mungkin

        )
    }
    //TODO 3 : mendeklarasikan kebutuhan untuk lokasi latar depan saat aplikasi meminta izin ACCESS_COARSE_LOCATION atau izin ACCESS_FINE_LOCATION,
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //TODO 2 : mengakses lokasi sesuai dengan gravitasi dan provider

            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }


    }

}
