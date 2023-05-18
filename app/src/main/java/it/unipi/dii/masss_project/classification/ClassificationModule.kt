package it.unipi.dii.masss_project.classification

import android.content.Context
import android.util.Log
import it.unipi.dii.masss_project.sensors_manager.SensorsCollector
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import java.util.Timer
import java.util.TimerTask

class ClassificationModule (applicationContext: Context) {
    private var classifier = TransportationModeClassifier(applicationContext)
    private var resultClassification =
        mutableMapOf("car" to 0, "bus" to 0, "train" to 0, "walking" to 0, "still" to 0)

    var sensorsCollector = SensorsCollector()

    private var data: Instances
    private var cls: Attribute

    private lateinit var timer: Timer

    init {
        val labels = ArrayList<String>()

        for (label in listOf("car", "still", "walking", "bus", "train"))
            labels.add(label)

        cls = Attribute("class", labels)

        val attributeLabels = listOf("android.sensor.accelerometer_mean", "android.sensor.accelerometer_min",
            "android.sensor.accelerometer_max", "android.sensor.accelerometer_std",
            "android.sensor.gyroscope_mean", "android.sensor.gyroscope_min",
            "android.sensor.gyroscope_max", "android.sensor.gyroscope_std",
            "android.sensor.magnetic_field_mean", "android.sensor.magnetic_field_min",
            "android.sensor.magnetic_field_max", "android.sensor.magnetic_field_std")

        val attributes = ArrayList<Attribute>()
        for (attr in attributeLabels)
            attributes.add(Attribute(attr))

        attributes.add(cls)

        data = Instances("toClassify", attributes, 0)

        data.setClassIndex(data.numAttributes() - 1)
    }

    /** manage the classification phase
     * @param values: array containing the features of collected samples
     * @return the label corresponding to the prediction of the classification
     * */
    fun handleClassification(values: DoubleArray): String {
        val instance = DenseInstance(values.size)

        for ((index, elem) in values.withIndex()) {
            instance.setValue(index, elem)
        }
        data.add(instance)
        val classification = classifier.classify(data)

        data.removeAt(0)

        val predictedLabel: String = cls.value(classification.toInt())
        Log.d("Classified", predictedLabel)

        if (predictedLabel == "still")
            resultClassification[predictedLabel] = 1
        else
            resultClassification[predictedLabel] = resultClassification[predictedLabel]?.plus(1) ?: 1

        return predictedLabel
    }

    /** start periodic classification */
    fun startClassification() {
        resultClassification.forEach{ (key, _) -> resultClassification[key] = 0}
        timer = Timer()
        Log.d("timer", "startTimer")
        timer.schedule(object : TimerTask() {
            override fun run() {
                handleClassification(
                    sensorsCollector.getFeatures(data.numAttributes()-1)
                )
            }
        }, 5000, 5000)
    }

    fun stopClassification(): String {
        timer.cancel()
        Log.d("timer", "stopTimer")
        println("result classification ${resultClassification.maxByOrNull { it.value}?.key}")
        return resultClassification.maxByOrNull { it.value}?.key ?: "None"
    }
}