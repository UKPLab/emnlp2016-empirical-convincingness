from __future__ import print_function

import sys

import data_loader
import keras.backend as keras_backend
import numpy as np
from attention_lstm import AttentionLSTM
from keras.engine import Input
from keras.layers import merge, Lambda, Activation, Convolution1D
from keras.layers.core import Dense, Dropout
from keras.layers.embeddings import Embedding
from keras.layers.recurrent import LSTM
from keras.models import Model
from keras.models import Sequential
from keras.preprocessing import sequence as sequence_module
from nltk.metrics.confusionmatrix import ConfusionMatrix
from theano.scalar import float32


def get_label_from_vector(vector):
    all_labels = ["o5_1", "o5_2", "o5_3", "o6_1", "o6_2", "o6_3", "o7_1", "o7_2", "o7_3", "o7_4"]
    max_value_index = np.argmax(np.array(vector))
    return all_labels[max_value_index]


def get_model(name, X_train, y_train, embeddings, batch_size, nb_epoch, max_len, max_features, nb_classes):
    print('Building model', name)

    # get correct loss
    loss_function = 'categorical_crossentropy'

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
        output = Dense(nb_classes, activation='softmax')(after_dropout)

        model = Model(input=sequence, output=output)

        # try using different optimizers and different optimizer configs
        model.compile('adam', loss_function, metrics=['accuracy'])
        # model.compile('adam', 'hinge', metrics=['hinge'])

        model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=nb_epoch, validation_split=0.1, verbose=0)

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
        output = Dense(nb_classes, activation='softmax')(after_dropout)

        model = Model(input=sequence, output=output)

        # try using different optimizers and different optimizer configs
        model.compile('adam', loss_function, metrics=['accuracy'])

        model.fit(X_train, y_train, batch_size=batch_size, nb_epoch=nb_epoch, validation_split=0.1, verbose=0)

        return model

    if name == 'MLP':
        model = Sequential()
        model.add(Dense(512, input_shape=(max_len,)))
        model.add(Activation('relu'))
        model.add(Dropout(0.5))
        model.add(Dense(nb_classes))
        model.add(Activation('softmax'))
        model.compile(loss=loss_function, optimizer='adam', metrics=['accuracy'])

        model.fit(X_train, y_train, nb_epoch=nb_epoch, batch_size=batch_size, validation_split=0.1, verbose=0)

        return model


def __main__():
    np.random.seed(1337)  # for reproducibility
    max_features = 20000
    max_len = 300  # cut texts after this number of words (among top max_features most common words)
    batch_size = 32
    nb_epoch = 5  # 5 epochs are meaningful to prevent over-fitting...
    nb_classes = 3

    # split_into_binary = False

    print('Loading data...')

    # switch to my data
    argv = sys.argv[1:]
    input_folder = argv[0]
    folds, word_index_to_embeddings_map = data_loader.load_my_data(input_folder, nb_words=max_features,
                                                                   reduced_label_set=True)

    all_folds_gold = []
    all_folds_predicted = []

    all_output_id_gold_pred_lines = []

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

        y_matrix_test = np.array(y_matrix_test)
        y_matrix_train = np.array(y_matrix_train)

        # convert class vectors to binary class matrices

        model = get_model('MLP', x_matrix_train, y_matrix_train, embeddings, batch_size, nb_epoch, max_len,
                          max_features, nb_classes)

        print('Prediction')
        model_predict = model.predict(x_matrix_test, batch_size=batch_size)
        predicted_labels = np.round(np.array(model_predict))

        # collect wrong predictions
        wrong_predictions_ids = []

        for i, (a, b) in enumerate(zip(y_matrix_test, predicted_labels)):
            # print("Gold", a, "Predicted", b)
            label_gold = get_label_from_vector(a)
            label_predicted = get_label_from_vector(b)
            all_folds_gold.append(label_gold)
            all_folds_predicted.append(label_predicted)

            instance_id = ids_test[i]

            if label_gold != label_predicted:
                wrong_predictions_ids.append(instance_id)

            all_output_id_gold_pred_lines.append(str(instance_id) + '\t' + label_gold + '\t' + label_predicted)

        # acc = accuracy(y_matrix_test, predicted_labels)
        # print('Test accuracy:', acc)

        print('Wrong predictions:', wrong_predictions_ids)

    cm = ConfusionMatrix(all_folds_gold, all_folds_predicted)
    print(cm.pretty_format())

    f = open(argv[1], 'w')
    for item in all_output_id_gold_pred_lines:
        f.write("%s\n" % item)


__main__()
