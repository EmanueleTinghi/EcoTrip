import os
from os.path import join
import numpy as np
import pandas as pd

class TMClassifier:
    samples_file = "data_samples.csv"
    samples_dict = {'time': [], 'android.sensor.accelerometer': [], 'android.sensor.gyroscope': [], 'sound': []}

    classes = ['Bus', 'Car', 'Still', 'Train', 'Walking']
    sensors = ['android.sensor.accelerometer', 'android.sensor.gyroscope', 'sound']

        def __init__(self):
            self.__classifier_model = pickle.load(open("random_forest.sav", 'rb'))
