package it.unipi.dii.masss_project

//import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import weka.classifiers.Classifier
import weka.classifiers.trees.RandomForest
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper
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
    private lateinit var rfClassifier: RandomForest //.forName(modelPath, null)

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
//        FileUtil.loadMappedFile(applicationContext, modelPath)
        assetManager = applicationContext.assets
        println("model path $modelPath")
        interpreter = loadModelFile(assetManager, modelPath)
        try {
            rfClassifier =
                (SerializationHelper.read(assetManager.open(modelPath)) as RandomForest?)!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        classifier.buildClassifier(dataset di addestramento Instance)
    }
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

//    fun classifyWeka() {
//        val classifier = weka.classifiers.Classifier.forName("weka.classifiers.trees.J48", null)
//        classifier.buildClassifier(data)
//
//        val task = object : Task<Void?>() {
//            override fun call(): Void? {
//                val result = classifier.classifyInstance(instance)
//                return null
//            }
//        }
//
//        val executor = Executors.newSingleThreadExecutor()
//        executor.submit(task)
//    }

    fun classify(): String? {
        println("classify()")
//        println("features: $sensorsFeatures")
//        println("accelerometer: $accelerometerSamples")
//        println("gyroscope: $gyroscopeSamples")
//        println("magneticField: $magneticFieldSamples")
        lockAccelerometer.lock()
        try {
//            Log.d("collector", "extract accelerometer")
//            sensorsFeatures.addAll(extractFeatures(accelerometerSamples))
            extractFeatures(accelerometerSamples)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
//            Log.d("collector", "extract gyroscope")
//            sensorsFeatures.addAll(extractFeatures(gyroscopeSamples))
            extractFeatures(gyroscopeSamples)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
//            Log.d("collector", "extract magnetic field")
//            sensorsFeatures.addAll(extractFeatures(magneticFieldSamples))
            extractFeatures(magneticFieldSamples)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }
        println("features: $sensorsFeatures")
        val input = inputCast()
        val d = input.get()
        println("d: $d")
//        val content = ByteArray(input.remaining())
//        input.get(content)
//        val string = Arrays.toString(content)
//        println("content_in: $string")
        val output = DoubleArray(5)//.allocateDirect(sensorsFeatures.size * 8)
        val result = interpreter.run {input}

        result.flip()
        val byteArray = ByteArray(result.remaining())
        result.get(byteArray)

        for (i in result.array()) {
            println("result[i]: $i")
        }


//        val buffer = ByteBuffer.allocateDirect(1024)
//        val content = ByteArray(result.remaining())
//        result.get(content)
//        val string = Arrays.toString(content)
//        println("content_a: $string")

        Log.d("collector", result.toString())
        Log.d("collector", byteArray.toString())
        val label = Base64.getEncoder().encodeToString(byteArray)
        println("label trip: $label")
        return label
    }

    private fun inputCast(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(sensorsFeatures.size * 8)
        byteBuffer.order(ByteOrder.nativeOrder())
        for (value in sensorsFeatures) {
//            println("for input $value")
            byteBuffer.putDouble(value)
//            println("byte position ${byteBuffer.position()}")
        }
        sensorsFeatures.clear()

        byteBuffer.flip()
//        val d = byteBuffer.getDouble(0)
//        println("d $d")
//        val content = ByteArray(byteBuffer.remaining())
//        byteBuffer.get(content)
//        val string = Arrays.toString(content)
//        println("content_inputcast: $string")
        return byteBuffer
    }
    private fun extractFeatures(sampleList: MutableList<Double>) {  //}: MutableList<Double> {
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
//        sensorsFeatures.addAll()
//        return mutableListOf(mean, min, max, stDev)
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
        Log.d("timer", "startT")
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
        Log.d("timer", "stopT")

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
            classifier.classifyInstance(data.lastInstance()).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "catch"
        }
        println("result_class $resultClass")
        return resultClass
    }
}

