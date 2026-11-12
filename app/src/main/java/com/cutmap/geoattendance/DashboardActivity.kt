package com.cutmap.geoattendance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var distanceText: TextView
    private lateinit var addressText: TextView
    private lateinit var productBarcodeText: TextView
    private lateinit var productDetailsLayout: LinearLayout
    private lateinit var productNameText: TextView
    private lateinit var productBrandText: TextView
    private lateinit var productQuantityText: TextView
    private lateinit var productNutriscoreText: TextView
    private lateinit var productImageView: ImageView
    private lateinit var historyRecyclerView: RecyclerView

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private var currentLocation: Location? = null
    private var currentDistance: Float = 0f
    private var currentAddress: String = "Not retrieved"
    private var scannedBarcodeValue: String = ""

    private val attendanceList = mutableListOf<Attendance>()
    private lateinit var attendanceAdapter: AttendanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: ""
        databaseRef = FirebaseDatabase.getInstance().reference.child("Attendance").child(uid)

        // Initialize UI components
        statusText = findViewById(R.id.statusText)
        distanceText = findViewById(R.id.distanceText)
        addressText = findViewById(R.id.addressText)
        productBarcodeText = findViewById(R.id.productBarcodeText)
        productDetailsLayout = findViewById(R.id.productDetailsLayout)
        productNameText = findViewById(R.id.productNameText)
        productBrandText = findViewById(R.id.productBrandText)
        productQuantityText = findViewById(R.id.productQuantityText)
        productNutriscoreText = findViewById(R.id.productNutriscoreText)
        productImageView = findViewById(R.id.productImageView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        // Setup RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        attendanceAdapter = AttendanceAdapter(attendanceList)
        historyRecyclerView.adapter = attendanceAdapter

        // Button Listeners
        findViewById<Button>(R.id.checkLocationBtn).setOnClickListener {
            checkLocationAndAddress()
        }

        findViewById<Button>(R.id.scanBtn).setOnClickListener {
            scanBarcodeWithGoogleCodeScanner()
        }

        findViewById<Button>(R.id.attendanceBtn).setOnClickListener {
            markAttendance()
        }

        findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        requestLocationPermissions()
        loadAttendanceHistory()
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location permission denied. Attendance disabled.", Toast.LENGTH_LONG).show()
                statusText.text = "Permission Denied"
                statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.campus_red))
            }
        }
    }

    /**
     * Checks student's current location using Fused Location Provider,
     * calculates distance from CampusConfig coordinates using Location.distanceBetween(),
     * and performs Reverse Geocoding to retrieve the address.
     */
    private fun checkLocationAndAddress() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions()
            return
        }

        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Unable to obtain location. Ensure GPS is enabled.", Toast.LENGTH_LONG).show()
                    statusText.text = "Location Unavailable"
                    statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.campus_red))
                    return@addOnSuccessListener
                }

                currentLocation = location

                // Calculate distance using Location.distanceBetween()
                val results = FloatArray(1)
                Location.distanceBetween(
                    CampusConfig.CAMPUS_LATITUDE,
                    CampusConfig.CAMPUS_LONGITUDE,
                    location.latitude,
                    location.longitude,
                    results
                )
                currentDistance = results[0]
                distanceText.text = "Distance: ${"%.2f".format(currentDistance)} metres"

                // Reverse Geocoding to get actual address
                fetchAddress(location.latitude, location.longitude)

                // Check 100-metre radius rule
                if (currentDistance <= CampusConfig.CAMPUS_RADIUS_METRES) {
                    statusText.text = "INSIDE CAMPUS (${"%.1f".format(currentDistance)}m)"
                    statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.campus_green))
                    Toast.makeText(this, "You are inside the campus geofence!", Toast.LENGTH_SHORT).show()
                } else {
                    statusText.text = "OUTSIDE CAMPUS (${"%.1f".format(currentDistance)}m)"
                    statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.campus_red))
                    Toast.makeText(this, "You are outside the 100-metre campus radius!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchAddress(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addressObj = addresses[0]
                val sb = StringBuilder()
                for (i in 0..addressObj.maxAddressLineIndex) {
                    sb.append(addressObj.getAddressLine(i)).append(", ")
                }
                currentAddress = sb.toString().trimEnd(',', ' ')
                addressText.text = "Address: $currentAddress"
            } else {
                currentAddress = "Lat: $lat, Lng: $lng (Address not found)"
                addressText.text = "Address: $currentAddress"
            }
        } catch (e: Exception) {
            currentAddress = "Lat: $lat, Lng: $lng"
            addressText.text = "Address: $currentAddress"
        }
    }

    /**
     * Implements Google Code Scanner using GmsBarcodeScanning API without requiring camera permission.
     */
    private fun scanBarcodeWithGoogleCodeScanner() {
        val scanner = GmsBarcodeScanning.getClient(this)
        scanner.startScan()
            .addOnSuccessListener { barcode: Barcode ->
                val rawValue = barcode.rawValue
                if (!rawValue.isNullOrEmpty()) {
                    scannedBarcodeValue = rawValue
                    productBarcodeText.text = "Scanned Barcode: $rawValue"
                    fetchProductInfoFromOpenFoodFacts(rawValue)
                } else {
                    Toast.makeText(this, "Scanned barcode is empty", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(this, "Barcode scanning failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Fetches product info from Open Food Facts API using Retrofit.
     */
    private fun fetchProductInfoFromOpenFoodFacts(barcode: String) {
        ApiService.instance.getProduct(barcode).enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val productResponse = response.body()!!
                    if (productResponse.status == 1 && productResponse.product != null) {
                        val product = productResponse.product
                        productDetailsLayout.visibility = View.VISIBLE

                        productNameText.text = "Product: ${product.productName ?: "Unknown"}"
                        productBrandText.text = "Brand: ${product.brands ?: "Unknown"}"
                        productQuantityText.text = "Quantity: ${product.quantity ?: "N/A"}"
                        productNutriscoreText.text = "Nutri-Score: ${(product.nutriscoreGrade ?: "N/A").uppercase()}"

                        val imageUrl = product.imageUrl
                        if (!imageUrl.isNullOrEmpty()) {
                            productImageView.visibility = View.VISIBLE
                            productImageView.load(imageUrl) {
                                crossfade(true)
                            }
                        } else {
                            productImageView.visibility = View.GONE
                        }

                        Toast.makeText(this@DashboardActivity, "Product info retrieved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        productDetailsLayout.visibility = View.GONE
                        Toast.makeText(this@DashboardActivity, "Product not found in Open Food Facts database", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to fetch product information", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "API Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Marks attendance with geofencing validation (<= 100m) and Firebase Realtime Database transaction
     * to prevent multiple entries on the same date.
     */
    private fun markAttendance() {
        if (currentLocation == null) {
            Toast.makeText(this, "Please check location first!", Toast.LENGTH_LONG).show()
            return
        }

        if (currentDistance > CampusConfig.CAMPUS_RADIUS_METRES) {
            Toast.makeText(this, "Cannot mark attendance! You are outside the 100m campus radius.", Toast.LENGTH_LONG).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        val dailyRef = databaseRef.child(currentDate)

        val attendanceRecord = Attendance(
            date = currentDate,
            time = currentTime,
            latitude = currentLocation!!.latitude,
            longitude = currentLocation!!.longitude,
            distance = currentDistance,
            address = currentAddress,
            status = "Present",
            scannedBarcode = scannedBarcodeValue
        )

        // Using Firebase Transaction to prevent duplicate attendance on the same date atomically
        dailyRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                if (mutableData.value != null) {
                    // Already marked for today
                    return Transaction.abort()
                }
                mutableData.value = attendanceRecord
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Toast.makeText(this@DashboardActivity, "Database Error: ${error.message}", Toast.LENGTH_LONG).show()
                } else if (committed) {
                    Toast.makeText(this@DashboardActivity, "Attendance Marked Successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "Attendance already marked for today!", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loadAttendanceHistory() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                attendanceList.clear()
                for (dateSnapshot in snapshot.children) {
                    val attendance = dateSnapshot.getValue(Attendance::class.java)
                    if (attendance != null) {
                        attendanceList.add(attendance)
                    }
                }
                // Newest attendance first
                attendanceList.reverse()
                attendanceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
