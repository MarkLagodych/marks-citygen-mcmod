package org.markscitygen.lib.wfc2d;

import java.util.*;
import java.util.random.RandomGenerator;

public class WFC2D {
    // Set of all tile types that appear in the sample
    HashSet<Integer> allTiles = new HashSet<>();

    // Tile type -> Number of times that tile type appears in the sample
    HashMap<Integer, Integer> tileFrequencies = new HashMap<>();

    // NeighbourDirection -> Tile type -> Set of valid neighbour tile types in that direction
    EnumMap<NeighbourDirection, HashMap<Integer, HashSet<Integer>>> validNeighboursAtDirection =
            new EnumMap<>(NeighbourDirection.class);

    {
        for (var dir : NeighbourDirection.values()) {
            validNeighboursAtDirection.put(dir, new HashMap<>());
        }
    }

    void addValidNeighbours(int tile, int neighbourTile, NeighbourDirection dir) {
        validNeighboursAtDirection
                .get(dir)
                .computeIfAbsent(tile, _ -> new HashSet<>())
                .add(neighbourTile);
    }

    HashSet<Integer> getValidNeighbours(int tile, NeighbourDirection dir) {
        return validNeighboursAtDirection.get(dir).getOrDefault(tile, new HashSet<>());
    }

    public WFC2D(int[][] sample) {
        addAllTiles(sample);
        computeTileFrequencies(sample);
        computeValidPairs(sample);
    }

    void addAllTiles(int[][] sample) {
        for (int[] row : sample) {
            for (int tileType : row) {
                allTiles.add(tileType);
            }
        }
    }

    void computeValidPairs(int[][] sample) {
        var size = new MapSize(sample.length, sample[0].length);

        for (int row = 0; row < size.rows() - 1; row++) {
            for (int col = 0; col < size.cols() - 1; col++) {
                int tile = sample[row][col];
                int rightTile = sample[row][col + 1];
                int bottomTile = sample[row + 1][col];

                addValidNeighbours(tile, rightTile, NeighbourDirection.RIGHT);
                addValidNeighbours(tile, bottomTile, NeighbourDirection.BELOW);

                addValidNeighbours(rightTile, tile, NeighbourDirection.LEFT);
                addValidNeighbours(bottomTile, tile, NeighbourDirection.ABOVE);
            }
        }
    }

    void computeTileFrequencies(int[][] sample) {
        for (int[] row : sample) {
            for (int tileType : row) {
                int freq = tileFrequencies.getOrDefault(tileType, 0);
                freq++;
                tileFrequencies.put(tileType, freq);
            }
        }
    }

    public int[][] generate(MapSize size) {
        final int MAX_ATTEMPTS = 10;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                var state = new CollapseState(size);
                state.collapse();
                return state.getResult();
            } catch (RuntimeException e) {
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
            }
        }

        throw new RuntimeException(
                "Failed to generate a valid grid after " + MAX_ATTEMPTS + " tries");
    }

    class CollapseState {
        record UncollapsedItem(TilePosition position, float entropy) {}

        RandomGenerator random = new Random();

        final MapSize size;

        UncollapsedItem[][] uncollapsedMap;

        PriorityQueue<UncollapsedItem> uncollapsedQueue =
                new PriorityQueue<>((a, b) -> Float.compare(a.entropy, b.entropy));

        Integer[][] collapsedTileTypes;

        public CollapseState(MapSize size) {
            this.size = size;
            this.uncollapsedMap = new UncollapsedItem[size.rows()][size.cols()];
            this.collapsedTileTypes = new Integer[size.rows()][size.cols()];
        }

        UncollapsedItem getUncollapsedItemAt(TilePosition p) {
            return uncollapsedMap[p.row()][p.col()];
        }

        void setUncollapsedItemAt(TilePosition p, UncollapsedItem item) {
            uncollapsedMap[p.row()][p.col()] = item;
        }

        Integer getCollapsedTileTypeAt(TilePosition p) {
            return collapsedTileTypes[p.row()][p.col()];
        }

        void setCollapsedTileTypeAt(TilePosition p, Integer tileType) {
            collapsedTileTypes[p.row()][p.col()] = tileType;
        }

        public void collapse() {
            uncollapsedQueue.add(new UncollapsedItem(new TilePosition(0, 0), 0.0f));

            while (!uncollapsedQueue.isEmpty()) {
                var pos = uncollapsedQueue.poll().position;

                collapseTileAt(pos);

                addNeighboursToCollapse(pos);
            }
        }

        HashSet<Integer> getValidTileTypesAt(TilePosition pos) {
            HashSet<Integer> validTileTypes = new HashSet<>(allTiles);

            for (var dir : NeighbourDirection.values()) {
                var neighbourPos = pos.neighbour(dir);
                if (!neighbourPos.isInBounds(size)) {
                    continue;
                }

                Integer neighbourTileType = getCollapsedTileTypeAt(neighbourPos);
                if (neighbourTileType == null) {
                    continue;
                }

                var validNeighbours = getValidNeighbours(neighbourTileType, dir.opposite());

                validTileTypes.retainAll(validNeighbours);
            }

            return validTileTypes;
        }

        void collapseTileAt(TilePosition pos) {
            var validTileTypes = getValidTileTypesAt(pos);
            if (validTileTypes.isEmpty()) {
                throw new RuntimeException("No valid tile types available");
            }

            int totalFrequencyOfValidTiles =
                    validTileTypes.stream()
                            .map(tileType -> tileFrequencies.get(tileType))
                            .reduce(0, Integer::sum);

            int randomValue = random.nextInt(totalFrequencyOfValidTiles);
            for (int tileType : validTileTypes) {
                randomValue -= tileFrequencies.get(tileType);
                if (randomValue <= 0) {
                    setCollapsedTileTypeAt(pos, tileType);
                    return;
                }
            }

            throw new RuntimeException("Failed to select a tile type");
        }

        void addNeighboursToCollapse(TilePosition pos) {
            for (var dir : NeighbourDirection.values()) {
                var neighbourPos = pos.neighbour(dir);
                if (!neighbourPos.isInBounds(size)) {
                    continue;
                }

                if (getCollapsedTileTypeAt(neighbourPos) != null) {
                    continue;
                }

                var oldUncollapsedItem = getUncollapsedItemAt(neighbourPos);
                if (oldUncollapsedItem != null) {
                    uncollapsedQueue.remove(oldUncollapsedItem);
                }

                var newUncollapsedItem =
                        new UncollapsedItem(neighbourPos, getEntropyAt(neighbourPos));

                uncollapsedQueue.add(newUncollapsedItem);
                setUncollapsedItemAt(neighbourPos, newUncollapsedItem);
            }
        }

        float getEntropyAt(TilePosition pos) {
            var validTiles = getValidTileTypesAt(pos);

            int totalFrequency =
                    validTiles.stream()
                            .map(tileType -> tileFrequencies.get(tileType))
                            .reduce(0, Integer::sum);

            float entropy = 0.0f;

            for (int tileType : validTiles) {
                float probability = (float) tileFrequencies.get(tileType) / totalFrequency;
                entropy -= probability * (float) Math.log(probability);
            }

            return entropy;
        }

        public int[][] getResult() {
            int[][] result = new int[size.rows()][size.cols()];
            for (int row = 0; row < size.rows(); row++) {
                for (int col = 0; col < size.cols(); col++) {
                    result[row][col] = collapsedTileTypes[row][col];
                }
            }
            return result;
        }
    }
}
