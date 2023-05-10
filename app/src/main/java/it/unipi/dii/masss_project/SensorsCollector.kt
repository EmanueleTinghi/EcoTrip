package it.unipi.dii.masss_project

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import java.sql.Timestamp

object SensorsCollector {
    private lateinit var python: Python
    private lateinit var module: PyObject
    private lateinit var classifier: PyObject
//    val py = Python.getInstance()
//    val module = py.getModule( "TMClassifier" )
//    val classifier = module["classifier"]

    init {
        val py = Python.getInstance()
        val module = py.getModule( "TMClassifier" )
        classifier = module["classifier"]!!
    }

    fun store_accelerator_sample(timestamp: Long, magnitude: Double) {
        val storeData = classifier?.get("store_accelerator_sample")
        storeData?.call(timestamp, magnitude)
    }

    fun store_gyroscope_sample(timestamp: Long, magnitude: Double) {
        val storeData = classifier?.get("store_gyroscope_sample")
        storeData?.call(timestamp, magnitude)
    }

    fun store_microphone_sample(timestamp: Long, magnitude: Double) {
        val storeData = classifier?.get("store_microphone_sample")
        storeData?.call(timestamp, magnitude)
    }

    fun print_res() {
        classifier?.get("print_samples")?.call()
    }

}

