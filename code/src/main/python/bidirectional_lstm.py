"""Bi-Directional LSTM
"""

from __future__ import print_function

import sys

import data_loader
import keras.backend as keras_backend
import numpy as np
import sklearn
from attention_lstm import AttentionLSTM
from keras.engine import Input
from keras.layers import merge, Lambda, Activation, Convolution1D
from keras.layers.core import Dense, Dropout
from keras.layers.embeddings import Embedding
from keras.layers.recurrent import LSTM
from keras.models import Model
from keras.models import Sequential
from keras.preprocessing import sequence as sequence_module
from theano.scalar import float32


def get_model(name, X_train, y_train, embeddings, batch_size, nb_epoch, max_len, max_features, nb_classes=17):
    print('Building model', name)

    # get correct loss
    loss_function = 'binary_crossentropy'

    if name == 'LSTM+ATT':
        # this is the placeholder tensor for the input sequences
        sequence = Input(shape=(max_len,), dtype='int32')
        # this embedding layer will transform the sequences of integers
        # into vectors of size 128
        embedded = Embedding(embeddings.shape[0], embeddings.shape[1], input_length=max_len, weights=[embeddings])(
            sequence)

        # 4 convolution layers (each 1000 filters)
        cnn = [Convolution1D(filter_length=filters, nb_filter=1000, border_mode='same') for filters in [2, 3, 5, 7]]

        # concatenate
        question = merge([cnn(embedded) for cnn in cnn], mode='concat')

        # create attention vector from max-pooled convoluted
        maxpool = Lambda(lambda x: keras_backend.max(x, axis=1, keepdims=False), output_shape=lambda x: (x[0], x[2]))
        attention_vector = maxpool(question)

        forwards = AttentionLSTM(64, attention_vector)(embedded)
        backwards = AttentionLSTM(64, attention_vector, go_backwards=True)(embedded)

        # concatenate the outputs of the 2 LSTMs
        answer_rnn = merge([forwards, backwards], mode='concat', concat_axis=-1)

        after_dropout = Dropout(0.5)(answer_rnn)
        # we have 17 classes
        output = Dense(nb_classes, activation='sigmoid')(after_dropout)

        model = Model(input=sequence, output=output)

        # try using different optimizers and different optimizer configs
        model.compile('adam', loss_function, metrics=[loss_function])
        # model.compile('adam', 'hinge', metrics=['hinge'])

        print("Layers: ", model.layers)
        for layer in model.layers:
            if isinstance(layer, AttentionLSTM):
                print(type(layer.attention_vec))
                # print('Attention vector shape:', layer.attention_vec.shape) -- doesn't print anything... piece of sh*t

        model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=nb_epoch, validation_split=0.1, verbose=1)

        return model

    if name == 'LSTM':
        # this is the placeholder tensor for the input sequences
        sequence = Input(shape=(max_len,), dtype='int32')
        # this embedding layer will transform the sequences of integers
        # into vectors of size 128
        embedded = Embedding(embeddings.shape[0], embeddings.shape[1], input_length=max_len, weights=[embeddings])(
            sequence)

        # apply forwards and backward LSTM
        forwards = LSTM(64)(embedded)
        backwards = LSTM(64, go_backwards=True)(embedded)

        # concatenate the outputs of the 2 LSTMs
        answer_rnn = merge([forwards, backwards], mode='concat', concat_axis=-1)

        after_dropout = Dropout(0.5)(answer_rnn)
        # we have 17 classes
        output = Dense(nb_classes, activation='sigmoid')(after_dropout)

        model = Model(input=sequence, output=output)

        # try using different optimizers and different optimizer configs
        model.compile('adam', loss_function, metrics=[loss_function])

        model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=nb_epoch, validation_split=0.1, verbose=0)

        return model

    if name == 'MLP':
        model = Sequential()
        model.add(Dense(512, input_shape=(max_len,)))
        model.add(Activation('relu'))
        model.add(Dropout(0.5))
        model.add(Dense(nb_classes))
        model.add(Activation('softmax'))
        model.compile(loss=loss_function, optimizer='adam', metrics=[loss_function])

        model.fit(X_train, y_train, nb_epoch=nb_epoch, batch_size=batch_size, validation_split=0.1, verbose=0)

        return model


def __main__():
    np.random.seed(1337)  # for reproducibility
    max_features = 20000
    max_len = 300  # cut texts after this number of words (among top max_features most common words)
    batch_size = 32
    nb_epoch = 5  # 5 epochs are meaningful to prevent over-fitting...

    # split_into_binary = False

    print('Loading data...')

    # switch to my data
    argv = sys.argv[1:]
    input_folder = argv[0]
    folds, word_index_to_embeddings_map = data_loader.load_my_data(input_folder, nb_words=max_features)

    # print statistics
    for fold in folds.keys():
        print("Fold name ", fold)
        x_matrix_train, y_matrix_train, ids_train = folds.get(fold)["training"]
        x_matrix_test, y_matrix_test, ids_test = folds.get(fold)["test"]

        # print(type(ids_test))
        # print(ids_test)

        # converting embeddings to numpy 2d array: shape = (vocabulary_size, 300)
        embeddings = np.asarray([np.array(x, dtype=float32) for x in word_index_to_embeddings_map.values()])

        print(len(x_matrix_train), 'train sequences')
        print(len(x_matrix_test), 'test sequences')

        print("Pad sequences (samples x time)")
        x_matrix_train = sequence_module.pad_sequences(x_matrix_train, maxlen=max_len)
        x_matrix_test = sequence_module.pad_sequences(x_matrix_test, maxlen=max_len)
        print('x_matrix_train shape:', x_matrix_train.shape)
        print('x_matrix_test shape:', x_matrix_test.shape)
        y_matrix_train = np.array(y_matrix_train)
        y_matrix_test = np.array(y_matrix_test)

        # split data into m binary classification tasks

        # if split_into_binary:
        #     nb_classes = y_matrix_train.shape[1]
        #     print("Number of classes:", nb_classes)
        #     create empty matrix for storing results for each class
        # predicted_labels = np.zeros(y_matrix_test.shape)
        # for current_class in range(nb_classes):
        #     current_class_y_train = y_matrix_train[:, current_class]
        #     explicit reshape to matrix (instances * 1)
        #     current_class_y_train.reshape(len(current_class_y_train), 1)
        # print("Current class Y vectors:", current_class_y_train.shape)
        # print(current_class_y_train)

        # model = get_model('LSTM', x_matrix_train, current_class_y_train, embeddings, batch_size, nb_epoch,
        #                   max_len, max_features, nb_classes=1)
        # print('Prediction')
        # model_predict = model.predict(x_matrix_test, batch_size=batch_size)
        # predicted_labels[:, :current_class] = np.round(np.array(model_predict))
        # else:
        model = get_model('MLP', x_matrix_train, y_matrix_train, embeddings, batch_size, nb_epoch, max_len,
                          max_features)

        print('Prediction')
        model_predict = model.predict(x_matrix_test, batch_size=batch_size)
        predicted_labels = np.round(np.array(model_predict))

        # collect wrong predictions
        wrong_predictions_ids = []

        # hamming loss
        hamming_loss = sklearn.metrics.hamming_loss(y_matrix_test, predicted_labels)
        # one-error
        # most probable single prediction
        one_error_raw = 0.0
        for i, (a, b) in enumerate(zip(y_matrix_test, predicted_labels)):
            max_value_index = np.argmax(np.array(a))
            one_error_match = np.round(b)[max_value_index] == np.round(a)[max_value_index]
            if one_error_match:
                one_error_raw += 1.0
        # value
        one_error = one_error_raw / np.array(y_matrix_test).shape[0]

        print("One error:", one_error, fold)
        print("Hamming loss:", hamming_loss, fold)

        for i, (a, b) in enumerate(zip(y_matrix_test, predicted_labels)):
            print("Gold", a, "Predicted", b)
            # if a != b:
            #     wrong_predictions_ids.append(ids_test[i])

        # acc = accuracy(y_matrix_test, predicted_labels)
        # print('Test accuracy:', acc)

        print('Wrong predictions:', wrong_predictions_ids)


__main__()
