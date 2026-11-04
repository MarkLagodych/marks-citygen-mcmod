package org.markscitygen.lib;

import java.util.ArrayList;

public final class Parcel {
    public ArrayList<Segment> segments = new ArrayList<>();

    public record Segment(int pathId, int startPointId, int endPointId) {}
}
