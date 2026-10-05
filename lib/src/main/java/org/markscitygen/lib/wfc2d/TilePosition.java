package org.markscitygen.lib.wfc2d;

public record TilePosition(int row, int col) {
    TilePosition above() {
        return new TilePosition(row - 1, col);
    }

    TilePosition below() {
        return new TilePosition(row + 1, col);
    }

    TilePosition left() {
        return new TilePosition(row, col - 1);
    }

    TilePosition right() {
        return new TilePosition(row, col + 1);
    }

    TilePosition neighbour(NeighbourDirection direction) {
        return switch (direction) {
            case ABOVE -> above();
            case RIGHT -> right();
            case BELOW -> below();
            case LEFT -> left();
        };
    }

    boolean isInBounds(MapSize mapSize) {
        return row >= 0 && col >= 0 && row < mapSize.rows() && col < mapSize.cols();
    }
}
