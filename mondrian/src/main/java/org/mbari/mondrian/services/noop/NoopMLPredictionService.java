package org.mbari.mondrian.services.noop;

import org.mbari.mondrian.domain.MachineLearningLocalization;
import org.mbari.mondrian.services.MLPredictionService;

import java.util.List;

public class NoopMLPredictionService implements MLPredictionService {
    @Override
    public List<MachineLearningLocalization> predict(byte[] jpegBytes) {
        return List.of();
    }
}
