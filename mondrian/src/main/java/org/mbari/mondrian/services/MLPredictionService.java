package org.mbari.mondrian.services;

import org.mbari.mondrian.domain.MachineLearningLocalization;

import java.util.List;

public interface MLPredictionService {

    List<MachineLearningLocalization> predict(byte[] jpegBytes);
}
