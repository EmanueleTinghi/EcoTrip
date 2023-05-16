package it.unipi.dii.masss_project

import android.content.Context
import android.util.Log
import weka.classifiers.trees.J48
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.FastVector
import weka.core.Instance
import weka.core.Instances
import weka.core.SerializationHelper
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow
import kotlin.math.sqrt


class SensorsCollector(applicationContext: Context) {
    private val modelPath = "J48.model"//"RF83.model"    // path from assets folder
    private lateinit var classifier: J48

    private val sensorsFeatures = mutableListOf<Double>()

    private lateinit var data: Instances

    private val accelerometerSamples = mutableListOf<Double>()
    private val gyroscopeSamples = mutableListOf<Double>()
    private val magneticFieldSamples = mutableListOf<Double>()

    private val lockAccelerometer = ReentrantLock()
    private val lockGyroscope = ReentrantLock()
    private val lockMagneticField = ReentrantLock()

    private val timer = Timer()

    init {
        classifier = J48()
        try {
            classifier = SerializationHelper.read(
                applicationContext.assets.open("J48.model")
            ) as J48
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var labels = FastVector<String>()

        labels.addElement("Car")
        labels.addElement("Walking")
        labels.addElement("Bus")
        labels.addElement("Train")
        val cls = Attribute("class", labels)

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

        val attributes: FastVector<Attribute> = FastVector<Attribute>()
        attributes.addElement(attr1)
        attributes.addElement(attr2)
        attributes.addElement(attr3)
        attributes.addElement(attr4)
        attributes.addElement(attr5)
        attributes.addElement(attr6)
        attributes.addElement(attr7)
        attributes.addElement(attr8)
        attributes.addElement(attr9)
        attributes.addElement(attr10)
        attributes.addElement(attr11)
        attributes.addElement(attr12)
        attributes.addElement(cls)

        data = Instances("toClassify", attributes, 0)

        data.setClassIndex(data.numAttributes() - 1)

    }

    fun classify(): String? {
        println("classify()")
        val values = DoubleArray(data.numAttributes())
        lockAccelerometer.lock()
        try {
            Log.d("collector", "extract accelerometer")
            extractFeatures(accelerometerSamples, values, 0)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
            Log.d("collector", "extract gyroscope")
            extractFeatures(gyroscopeSamples, values, 1)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
            Log.d("collector", "extract magnetic field")
            extractFeatures(magneticFieldSamples, values, 2)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }
        val instance: DenseInstance = DenseInstance(12)
        instance.copy(values)

        data.add(instance)

        val ciao = classifier.classifyInstance(data[0])
        Log.d("Classified as", ciao.toString())
        return "We";
    }


    private fun extractFeatures(sampleList: MutableList<Double>, instance: DoubleArray , index: Int){  //}: MutableList<Double> {
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

