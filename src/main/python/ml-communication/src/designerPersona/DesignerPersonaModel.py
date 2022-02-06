import csv
import pandas as pd
import numpy as np

from PIL import Image
import io
import os
import os.path
from os import listdir
from os import path

from sklearn.cluster import KMeans
from sklearn.decomposition import PCA, TruncatedSVD
from sklearn.preprocessing import StandardScaler
from sklearn import metrics
import joblib

import matplotlib.pyplot as plt
import matplotlib as mpl


class DesignerPersonaModel:
    def __init__(self):
        self.scaler = None
        self.pca = None
        self.model = None
        self.base_dataset = None

        self.label_change = {0:0,
                        8:1,
                        5:8,
                        3:2,
                        6:7,
                        7:3,
                        11:6,
                        2:9,
                        1:4,
                        10:5,
                        4:10,
                        9:11}

    def load_scaler(self, scaler_path):
        self.scaler = joblib.load(scaler_path)
        print("scaler loaded")
        return self.scaler

    def load_pca(self, pca_path):
        self.pca = joblib.load(pca_path)
        return self.pca

    def load_base_dataset(self, base_dataset):
        self.base_dataset = joblib.load(base_dataset)
        return self.base_dataset

    def load_model(self, model_path):
        self.model = joblib.load(model_path)
        return self.model

    def predict(self, data):
        predicted = self.model.predict(data)
        predicted = np.array([self.label_change[i] for i in predicted])
        return predicted

    def __predict(self, data):
        return self.model.predict(data)

    def transform_predict(self, data):
        transformed_data = self.scaler.transform(data)
        transformed_data = self.pca.transform(transformed_data)
        predicted = self.model.predict(transformed_data)
        predicted = np.array([self.label_change[i] for i in predicted])
        return predicted

    def print_background(self):

        offset = 0.4
        pcaTest = self.base_dataset

        print(pcaTest.shape)

        colors = ['red', 'green', 'blue', 'cyan', 'orange', 'lightseagreen', 'royalblue', 'gold', 'purple', 'black', 'peru','yellowgreen']
        cmap = mpl.colors.ListedColormap(mpl.cm.get_cmap("Set3").colors[:12])

        ##THE DATA FOR THE PLOT!

        test_data = self.base_dataset

        # Step size of the mesh. Decrease to increase the quality of the VQ.
        h = .4    # point in the mesh [x_min, x_max]x[y_min, y_max].

        # Plot the decision boundary. For that, we will assign a color to each
        x_min, x_max = test_data[:, 0].min() - offset, test_data[:, 0].max() + offset
        y_min, y_max = test_data[:, 1].min() - offset, test_data[:, 1].max() + offset
        xx, yy = np.meshgrid(np.arange(x_min, x_max, h), np.arange(y_min, y_max, h))
        raveled_nump = np.c_[xx.ravel(), yy.ravel()]

        # Obtain labels for each point in mesh. Use last trained model.
        Z = self.predict(raveled_nump)

        # Put the result into a color plot
        Z = Z.reshape(xx.shape)

        final_figure = plt.figure(1, figsize=(15, 15))
        plt.clf()
        plt.imshow(Z, interpolation='nearest',
                   extent=(xx.min(), xx.max(), yy.min(), yy.max()),
                   cmap=cmap,
                   aspect='auto', origin='lower')

        plt.xlim(x_min, x_max)
        plt.ylim(y_min, y_max)
        plt.xticks(())
        plt.yticks(())
        plt.colorbar().set_ticks([])

        plt.show()