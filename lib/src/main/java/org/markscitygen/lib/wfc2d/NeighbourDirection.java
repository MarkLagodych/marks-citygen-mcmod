package org.markscitygen.lib.wfc2d;

public enum NeighbourDirection {
    ABOVE(0),
    RIGHT(1),
    BELOW(2),
    LEFT(3);

    final int value;

    NeighbourDirection(int value) {
        this.value = value;
    }

    public NeighbourDirection opposite() {
        int numValues = values().length;

        return values()[(this.value + numValues / 2) % numValues];
    }
}
