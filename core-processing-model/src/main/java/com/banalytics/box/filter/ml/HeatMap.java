package com.banalytics.box.filter.ml;

public class HeatMap {
    private final int width;
    private final int height;
    private final int cols;
    private final int rows;

    private final int cellWidth;
    private final int cellHeight;

    int[][] heatMatrix;

    public HeatMap(int width, int height, int cols, int rows) {
        this.width = width;
        this.height = height;
        this.cols = cols;
        this.rows = rows;
        this.cellWidth = width / cols;
        this.cellHeight = height / rows;

        heatMatrix = new int[rows][cols];
    }

    public void heat(int x, int y, int width, int height, int heatSpeed) {
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        int x1Index = x / cellWidth;
        x1Index = Math.min(x1Index, cols - 1);
        int y1Index = y / cellHeight;
        y1Index = Math.min(y1Index, rows - 1);
        int x2Index = (x + width) / cellWidth;
        x2Index = Math.min(x2Index, cols - 1);
        int y2Index = (y + height) / cellHeight;
        y2Index = Math.min(y2Index, rows - 1);

        for (int hx = x1Index; hx <= x2Index; hx++) {
            for (int hy = y1Index; hy <= y2Index; hy++) {
                heatMatrix[hy][hx] += heatSpeed;
            }
        }
    }

    public void coolDown() {
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                if (heatMatrix[j][i] > 0) {
                    heatMatrix[j][i]--;
                }
            }
        }
    }

    public boolean hasHeatAreas(int threshold) {
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                if (heatMatrix[j][i] > threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    public void print() {
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                System.out.print(heatMatrix[j][i] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        HeatMap hm = new HeatMap(800, 600, 8, 6);

        hm.heat(0, 0, 10, 10, 5);
        hm.heat(80, 60, 100, 100, 5);
        hm.heat(0, 0, 800, 600, 5);
        hm.heat(650, 450, 150, 150, 5);
    }
}
