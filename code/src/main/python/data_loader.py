from __future__ import absolute_import
from __future__ import print_function

from copy import copy

import numpy as np
import vocabulary_embeddings_extractor
from os import listdir


def load_single_file(directory, file_name, word_to_indices_map, nb_words=None, reduced_label_set=False):
    """
    Loads a single file and returns a tuple of x vectors and y labels
    :param directory: dir
    :param file_name: file name
    :param word_to_indices_map: words to their indices
    :param nb_words: maximum word index to be kept; otherwise treated as OOV
    :return: tuple of lists of integers
    """
    f = open(directory + file_name, 'r')
    lines = f.readlines()
    # remove first line with comments
    del lines[0]

    x_vectors = []
    y_labels = []
    id_vector = []

    for line in lines:
        # print line
        arg_id, label, a1, a2 = line.split('\t')
        # print(arg_id, label, a1, a2)

        id_vector.append(arg_id)

        a1_tokens = vocabulary_embeddings_extractor.tokenize(a1)
        a2_tokens = vocabulary_embeddings_extractor.tokenize(a2)

        # print(a1_tokens)
        # print(a2_tokens)

        # now convert tokens to indices; set to 2 for OOV
        a1_indices = [word_to_indices_map.get(word, 2) for word in a1_tokens]
        a2_indices = [word_to_indices_map.get(word, 2) for word in a2_tokens]

        # join them into one vector, start with 1 for start_of_sequence, add also 1 in between
        x = [1] + a1_indices + [1] + a2_indices
        # print(x)

        # map class to vector
        all_labels = ["o5_1", "o5_2", "o5_3", "o6_1", "o6_2", "o6_3", "o7_1", "o7_2", "o7_3", "o7_4", "o8_1", "o8_4",
                      "o8_5", "o9_1", "o9_2", "o9_3", "o9_4"]
        if reduced_label_set:
            all_labels = ["o5", "o6", "o7"]

        # zeros vector y
        y = np.zeros(len(all_labels))
        # split label by comma
        print(all_labels)
        for l in label.split(','):
            print(l)
            sup_label = l.split('_')[0]
            print(sup_label)
            index_in_labels = all_labels.index(sup_label)
            # and set to one
            y[index_in_labels] = 1

        # print('Y vector: ', y, 'for class', label)

        x_vectors.append(x)
        y_labels.append(y)

    # replace all word indices larger than nb_words with OOV
    if nb_words:
        x_vectors = [[2 if word_index >= nb_words else word_index for word_index in x] for x in x_vectors]

    train_instances = x_vectors
    train_labels = y_labels

    return train_instances, train_labels, id_vector


def load_my_data(directory, test_split=0.2, nb_words=None, reduced_label_set=False):
    files = listdir(directory)
    # print(files)

    # folds
    folds = dict()
    for file_name in files:
        training_file_names = copy(files)
        # remove current file
        training_file_names.remove(file_name)
        folds[file_name] = {"training": training_file_names, "test": file_name}

    # print(folds)

    word_to_indices_map, word_index_to_embeddings_map = vocabulary_embeddings_extractor.load_all()

    # results: map with fold_name (= file_name) and two tuples: (train_x, train_y), (test_x, test_y)
    output_folds_with_train_test_data = dict()

    # load all data first
    all_loaded_files = dict()
    for file_name in folds.keys():
        # print(file_name)
        test_instances, test_labels, ids = load_single_file(directory, file_name, word_to_indices_map, nb_words,
                                                            reduced_label_set)
        all_loaded_files[file_name] = test_instances, test_labels, ids
    print("Loaded", len(all_loaded_files), "files")

    # parse each csv file in the directory
    for file_name in folds.keys():
        # print(file_name)

        # add new fold
        output_folds_with_train_test_data[file_name] = dict()

        # fill fold with train data
        current_fold = output_folds_with_train_test_data[file_name]

        test_instances, test_labels, ids, = all_loaded_files.get(file_name)

        # add tuple
        current_fold["test"] = test_instances, test_labels, ids

        # now collect all training instances
        all_training_instances = []
        all_training_labels = []
        all_training_ids = []
        for training_file_name in folds.get(file_name)["training"]:
            training_instances, training_labels, ids = all_loaded_files.get(training_file_name)
            all_training_instances.extend(training_instances)
            all_training_labels.extend(training_labels)
            all_training_ids.extend(ids)

        current_fold["training"] = all_training_instances, all_training_labels, all_training_ids

    # now we should have all data loaded

    return output_folds_with_train_test_data, word_index_to_embeddings_map


def __main__():
    np.random.seed(1337)  # for reproducibility

    max_words = 1000
    batch_size = 32
    nb_epoch = 10

    print('Loading data...')
    folds, word_index_to_embeddings_map = load_my_data("~/data2/convincingness-emnlp/step14-gold-csv/")

    # print statistics
    for fold in folds.keys():
        print("Fold name ", fold)
        training_instances, training_labels = folds.get(fold)["training"]
        test_instances, test_labels = folds.get(fold)["test"]

        print("Training instances ", len(training_instances), " training labels ", len(training_labels))
        print("Test instances ", len(test_instances), " test labels ", len(test_labels))

# __main__()
