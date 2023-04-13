package org.mbari.mondrian.domain;

import java.util.List;

public record Concept(String primaryName, List<String> alternateNames, String rank) {}
