package it.unipi.dii.masss_project

import android.content.Context
import weka.classifiers.meta.AdaBoostM1
import weka.core.Instances
import weka.core.SerializationHelper

class TransportationModeClassifier(applicationContext: Context) {

    private val modelPath = "ADABoostJ48.model"     // path from assets folder
    private var classifier: AdaBoostM1

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
    }

    fun classify(data: Instances): Double {
        return try {
            classifier.classifyInstance(data[0])
        } catch (e: Exception) {
            e.printStackTrace()
            1.0     // default corresponding to "still"
        }
    }
}