package it.unipi.dii.masss_project

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import weka.classifiers.Classifier
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow
import kotlin.math.sqrt

class SensorsCollector(applicationContext: Context) {
    private val assetManager: AssetManager
    private val modelPath = "RF83.model"    // path from assets folder
    private var rfClassifier: RandomForest

    private val sensorsFeatures = mutableListOf<Double>()

    private val accelerometerSamples = mutableListOf<Double>()
    private val gyroscopeSamples = mutableListOf<Double>()
    private val magneticFieldSamples = mutableListOf<Double>()

    private val lockAccelerometer = ReentrantLock()
    private val lockGyroscope = ReentrantLock()
    private val lockMagneticField = ReentrantLock()

    private lateinit var timer: Timer

    private val featureLabels = listOf("android.sensor.accelerometer_mean", "android.sensor.accelerometer_min",
        "android.sensor.accelerometer_max", "android.sensor.accelerometer_std",
        "android.sensor.gyroscope_mean", "android.sensor.gyroscope_min",
        "android.sensor.gyroscope_max", "android.sensor.gyroscope_std",
        "android.sensor.magnetic_field_mean", "android.sensor.magnetic_field_min",
        "android.sensor.magnetic_field_max", "android.sensor.magnetic_field_std" )

    init {
        assetManager = applicationContext.assets
        println("model path $modelPath")
        rfClassifier = RandomForest()
        try {
            rfClassifier = (SerializationHelper.read(
                assetManager.open(modelPath)
            ) as RandomForest?)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun buildClassifierInstance() {
        var labels = ArrayList<String>()
        labels.add("Car")
        labels.add("Walking")
        labels.add("Bus")
        labels.add("Train")
        val cls = Attribute("class", labels)
    }

    fun classify(): String? {
        println("classify()")
        lockAccelerometer.lock()
        try {
            extractFeatures(accelerometerSamples)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
            extractFeatures(gyroscopeSamples)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
            extractFeatures(magneticFieldSamples)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }

        return null
    }

    private fun extractFeatures(sampleList: MutableList<Double>) {
        val mean = sampleList.average()
        val min = sampleList.min()
        val max = sampleList.max()
        val squaredDifferences = sampleList.map { (it - mean).pow(2) }
        val meanOfSquaredDifferences = squaredDifferences.average()
        val stDev= sqrt(meanOfSquaredDifferences)
        sensorsFeatures.add(mean)
        sensorsFeatures.add(min)
        sensorsFeatures.add(max)
        sensorsFeatures.add(stDev)
    }

    fun storeAcceleratorSample(magnitude: Double) {
//        Log.d("collector", "storeAccelerometer")
        lockAccelerometer.lock()
        try {
            accelerometerSamples.add(magnitude)
        } finally {
            lockAccelerometer.unlock()
        }
    }

    fun storeGyroscopeSample(magnitude: Double) {
//        Log.d("collector", "storeGyroscope")
        lockGyroscope.lock()
        try {
            gyroscopeSamples.add(magnitude)
        } finally {
            lockGyroscope.unlock()
        }
    }

    fun storeMagneticFieldSample(magnitude: Double) {
//        Log.d("collector", "storeMagneticField")
        lockMagneticField.lock()
        try {
            magneticFieldSamples.add(magnitude)
        } finally {
            lockMagneticField.unlock()
        }
    }

    fun startCollection() {
        timer = Timer()
        Log.d("timer", "startTimer")
        timer.schedule(object : TimerTask() {
            override fun run() {
                // Do something after a certain period of time
//                classify()
                classify(rfClassifier)
                println("classify samples")
            }
        }, 5000, 5000)
    }

    fun stopCollection() {
        timer.cancel()
        Log.d("timer", "stopTimer")

    }
    private fun classify(classifier: Classifier): String {
        lockAccelerometer.lock()
        try {
            extractFeatures(accelerometerSamples)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
            extractFeatures(gyroscopeSamples)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
            extractFeatures(magneticFieldSamples)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }

        val attributes = ArrayList<Attribute>()
        for (i in featureLabels) {
            attributes.add(Attribute(i, true))
        }
        val data = Instances("data", attributes, 2)
        val instance = DenseInstance(sensorsFeatures.size)
        for (i in sensorsFeatures.indices) {
            instance.setValue(i, sensorsFeatures[i])
        }
        sensorsFeatures.clear()

        data.add(instance)
        data.setClassIndex(data.numAttributes() - 1)
        println("data class ind: ${data.classIndex()}")
        val resultClass = try {
            classifier.classifyInstance(data.firstInstance()).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "catch"
        }
        data.clear()
        println("result_class $resultClass")
        return resultClass
    }
}

