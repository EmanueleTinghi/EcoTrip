package it.unipi.dii.masss_project

import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow
import kotlin.math.sqrt

class SensorsCollector {

    private val accelerometerSamples = mutableListOf<Double>()
    private val gyroscopeSamples = mutableListOf<Double>()
    private val magneticFieldSamples = mutableListOf<Double>()

    private val lockAccelerometer = ReentrantLock()
    private val lockGyroscope = ReentrantLock()
    private val lockMagneticField = ReentrantLock()

    fun getFeaturesSamples(numFeature: Int): DoubleArray {
        val values = DoubleArray(numFeature)
        lockAccelerometer.lock()
        try {
            extractFeatures(accelerometerSamples, values, 0)
            accelerometerSamples.clear()
        } finally {
            lockAccelerometer.unlock()
        }

        lockGyroscope.lock()
        try {
            extractFeatures(gyroscopeSamples, values, 1)
            gyroscopeSamples.clear()
        } finally {
            lockGyroscope.unlock()
        }

        lockMagneticField.lock()
        try {
            extractFeatures(magneticFieldSamples, values, 2)
            magneticFieldSamples.clear()
        } finally {
            lockMagneticField.unlock()
        }
        return values
    }

    /** compute features of sampleList of the data collected from each sensor */
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

}

