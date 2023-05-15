import os
from os import listdir
from os.path import join, dirname
import numpy as np
import pandas as pd

class TMClassifier:
    samples_file = "data_samples.csv"
    samples_dict = {'time': [], 'android.sensor.accelerometer': [], 'android.sensor.gyroscope': [], 'sound': []}

    classes = ['Bus', 'Car', 'Still', 'Train', 'Walking']
    sensors = ['android.sensor.accelerometer', 'android.sensor.gyroscope', 'sound']

    def __init__(self):
        print("init py")
#         files = listdir(dirname(__file__))
#         for f in files:
#             print(f)
        saved_model_dir = "saved_model/keras_model/"
        filename = join(dirname(__file__), saved_model_dir)
#         print(filename)
#         classifier_model = tf.keras.models.load_model(saved_model_dir)
#         print(classifier_model is not None)
# #         with open("rf_model.joblib", 'rb') as f:
# #             print("boh")
#             self.__classifier_model = joblib.load(filename)
#         self.__classifier_model = pickle.load(open(filename), 'rb')


    def store_accelerator_sample(self, timestamp, magnitude):
        self.samples_dict['time'].append(timestamp)
        self.samples_dict[self.sensors[0]].append(magnitude)
        self.samples_dict[self.sensors[1]].append(None)
        self.samples_dict[self.sensors[2]].append(None)

    def store_gyroscope_sample(self, timestamp, magnitude):
        self.samples_dict['time'].append(timestamp)
        self.samples_dict[self.sensors[0]].append(None)
        self.samples_dict[self.sensors[1]].append(magnitude)
        self.samples_dict[self.sensors[2]].append(None)

    def store_microphone_sample(self, time_s, magnitude):
        self.samples_dict['time'].append(time_s)
        self.samples_dict[self.sensors[0]].append(None)
        self.samples_dict[self.sensors[1]].append(None)
        self.samples_dict[self.sensors[2]].append(magnitude)

    def print_samples(self):
        print(self.samples_dict)

#     def write_file_sample(sample: str):
#         filename = join(os.environ["HOME"], "filename_sam.txt")
#         print(filename)
#     #     os.remove(filename)
#         with open(filename, 'a') as f:
#             f.write(sample)
#             return "ok: " + filename
#         return 'ko'
#
#     def read_file_sample():
#        filename = join(os.environ["HOME"], "filename_sam.txt")
#        print(filename)
#        with open(filename, 'r') as f:
#            for line in f:
#                 print(line)


classifier = TMClassifier()