package it.unipi.dii.masss_project

import android.content.Context
import android.util.Log
import weka.classifiers.meta.AdaBoostM1
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

    private val modelPath = "ADABoostJ48.model"     // path from assets folder
    private var classifier: AdaBoostM1

    private var data: Instances
    private var cls: Attribute

    private val accelerometerSamples = mutableListOf<Double>()
    private val gyroscopeSamples = mutableListOf<Double>()
    private val magneticFieldSamples = mutableListOf<Double>()

    private val lockAccelerometer = ReentrantLock()
    private val lockGyroscope = ReentrantLock()
    private val lockMagneticField = ReentrantLock()

    private lateinit var timer: Timer

    private var resultClassification = mutableMapOf("car" to 0, "bus" to 0, "train" to 0, "walking" to 0)

    init {
        println("model path $modelPath")

        classifier = AdaBoostM1()
        try {
            classifier = SerializationHelper.read(
                applicationContext.assets.open(modelPath)
            ) as AdaBoostM1
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val labels = ArrayList<String>()

        labels.add("car")
        labels.add("walking")
        labels.add("bus")
        labels.add("train")

        cls = Attribute("class", labels)

        val attr1 = Attribute("android.sensor.accelerometer_mean")
        val attr2 = Attribute("android.sensor.accelerometer_min")
        val attr3 = Attribute("android.sensor.accelerometer_max")
        val attr4 = Attribute("android.sensor.accelerometer_std")
        val attr5 = Attribute("android.sensor.gyroscope_mean")
        val attr6 = Attribute("android.sensor.gyroscope_min")
        val attr7 = Attribute("android.sensor.gyroscope_max")
        val attr8 = Attribute("android.sensor.gyroscope_std")
        val attr9 = Attribute("android.sensor.magnetic_field_mean")
        val attr10 = Attribute("android.sensor.magnetic_field_min")
        val attr11 = Attribute("android.sensor.magnetic_field_max")
        val attr12 = Attribute("android.sensor.magnetic_field_std")

        val attributes = ArrayList<Attribute>()
        attributes.add(attr1)
        attributes.add(attr2)
        attributes.add(attr3)
        attributes.add(attr4)
        attributes.add(attr5)
        attributes.add(attr6)
        attributes.add(attr7)
        attributes.add(attr8)
        attributes.add(attr9)
        attributes.add(attr10)
        attributes.add(attr11)
        attributes.add(attr12)
        attributes.add(cls)

        data = Instances("toClassify", attributes, 0)

        data.setClassIndex(data.numAttributes() - 1)

    }

    fun classify(): String {
        println("classify()")
        val values = DoubleArray(data.numAttributes())
        lockAccelerometer.lock()
        try {
//            extractFeatures(accelerometerSamples)
            extractFeatures(accelerometerSamples, values, 0)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
//            extractFeatures(gyroscopeSamples)
            extractFeatures(gyroscopeSamples, values, 1)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
//            extractFeatures(magneticFieldSamples)
            extractFeatures(magneticFieldSamples, values, 2)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }

        val instance = DenseInstance(12)
        instance.copy(values)
        //Add instance to classify
        data.add(instance)

        val classification = classifier.classifyInstance(data[0])


        //Remove features of the classified instance, store the result and delete data safely
        data.removeAt(0)

        // Convert the double value back into a string
        val predictedString: String = cls.value(classification.toInt()).lowercase()

        Log.d("Classified", predictedString)

        return predictedString
    }

    private fun extractFeatures(sampleList: MutableList<Double>, instance: DoubleArray , index: Int) {
        val mean = sampleList.average()
        val min = sampleList.min()
        val max = sampleList.max()
        val squaredDifferences = sampleList.map { (it - mean).pow(2) }
        val meanOfSquaredDifferences = squaredDifferences.average()
        val stDev= sqrt(meanOfSquaredDifferences)

        instance[index * 4] = mean
        instance[index * 4 + 1] = min
        instance[index * 4 + 2] = max
        instance[index * 4 +  3] = stDev
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
                val ret = classify()
                resultClassification[ret] = resultClassification[ret]?.plus(1) ?: 1
            }
        }, 5000, 5000)
    }

    fun stopCollection(): String {
        timer.cancel()
        Log.d("timer", "stopTimer")
        println("result classification ${resultClassification.maxByOrNull { it.value}?.key}")
        return resultClassification.maxByOrNull { it.value}?.key ?: "None"
    }

}

