package it.unipi.dii.masss_project

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.firebase.firestore.util.FileUtil
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Base64
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow
import kotlin.math.sqrt

class SensorsCollector(applicationContext: Context) {
    private val assetManager: AssetManager
    private val modelPath = "RF83.model"    // path from assets folder
    private val interpreter: MappedByteBuffer

    private val sensorsFeatures = mutableListOf<Double>()

    private val accelerometerSamples = mutableListOf<Double>()
    private val gyroscopeSamples = mutableListOf<Double>()
    private val magneticFieldSamples = mutableListOf<Double>()

    private val lockAccelerometer = ReentrantLock()
    private val lockGyroscope = ReentrantLock()
    private val lockMagneticField = ReentrantLock()

    private val timer = Timer()

    init {
        assetManager = applicationContext.assets
        println("model path $modelPath")
        interpreter = loadModelFile(assetManager, modelPath)
    }
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classify(): String? {
        println("classify()")
        println("features: $sensorsFeatures")
        println("accelerometer: $accelerometerSamples")
        println("gyroscope: $gyroscopeSamples")
        println("magneticField: $magneticFieldSamples")
        lockAccelerometer.lock()
        try {
            sensorsFeatures.addAll(extractFeatures(accelerometerSamples))
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
            sensorsFeatures.addAll(extractFeatures(gyroscopeSamples))
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
            sensorsFeatures.addAll(extractFeatures(magneticFieldSamples))
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }

        val result = interpreter.run { inputCast() }
        val byteArray = ByteArray(result.remaining())
        result.get(byteArray)
        val label = Base64.getEncoder().encodeToString(byteArray)
        println("label trip: $label")
        return label
    }

    private fun inputCast(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(sensorsFeatures.size * 8)
        byteBuffer.order(ByteOrder.nativeOrder())
        for (value in sensorsFeatures) {
            byteBuffer.putDouble(value)
        }
        sensorsFeatures.clear()
        return byteBuffer
    }
    private fun extractFeatures(sampleList: MutableList<Double>): MutableList<Double> {
        val mean = sampleList.average()
        val min = sampleList.min()
        val max = sampleList.max()
        val squaredDifferences = sampleList.map { (it - mean).pow(2) }
        val meanOfSquaredDifferences = squaredDifferences.average()
        val stDev= sqrt(meanOfSquaredDifferences)
//        sensorsFeatures.addAll()
        return mutableListOf(mean, min, max, stDev)
    }

    fun storeAcceleratorSample(magnitude: Double) {
        Log.d("collector", "storeAccelerometer")
        lockAccelerometer.lock()
        try {
            accelerometerSamples.add(magnitude)
        } finally {
            lockAccelerometer.unlock()
        }
    }

    fun storeGyroscopeSample(magnitude: Double) {
        Log.d("collector", "storeGyroscope")
        lockGyroscope.lock()
        try {
            gyroscopeSamples.add(magnitude)
        } finally {
            lockGyroscope.unlock()
        }
    }

    fun storeMagneticFieldSample(magnitude: Double) {
        Log.d("collector", "storeMagneticField")
        lockMagneticField.lock()
        try {
            magneticFieldSamples.add(magnitude)
        } finally {
            lockMagneticField.unlock()
        }
    }

    fun startCollection() {
        Log.d("timer", "startT")
        timer.schedule(object : TimerTask() {
            override fun run() {
                // Do something after a certain period of time
                classify()
                println("classify samples")
            }
        }, 5000, 5000)
    }

    fun stopCollection() {
        timer.cancel()
        Log.d("timer", "stopT")

    }

    /* fun main() {
    val numbers = arrayOf(1.0, 2.0, 3.0, 4.0, 5.0)
    val mean = numbers.average()
    val squaredDifferences = numbers.map { (it - mean) * (it - mean) }
    val meanOfSquaredDifferences = squaredDifferences.average()
    val standardDeviation = Math.sqrt(meanOfSquaredDifferences)
    println("Standard deviation: $standardDeviation")
}
*/
    /* val input = ...
    val byteBuffer = ByteBuffer.allocateDirect(input.size * 4)
    byteBuffer.order(ByteOrder.nativeOrder())
    for (value in input) {
    byteBuffer.putFloat(value)
    }*/
}

