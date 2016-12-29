/*
 * Downloaded from: http://introcs.cs.princeton.edu/java/22library/Matrix.java.html
 * and adapted to my needs :)
 */
package mybabyanubis;

public class Matrix {

    public int[][] matrix;
    private final String name;

    public Matrix(int[][] matrix, int n, String name) { //square matrix
        this.matrix = new int[n][n];
        for (int i = 0; i < this.matrix.length; i++) {
            this.matrix[i] = matrix[i].clone();
        }
        this.name = name;
    }

    // return n-by-n identity matrix I
    public int[][] identity(int n) {
        int[][] I = new int[n][n];
        for (int i = 0; i < n; i++) {
            I[i][i] = 1;
        }
        return I;
    }

    // return x^T y
    public  int dot(int[] x, int[] y) {
        if (x.length != y.length) {
            throw new RuntimeException("Illegal vector dimensions.");
        }
        int sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i] * y[i];
        }
        return sum;
    }

    // return C = A^T
    public int[][] transpose(int[][] A) {
        int m = A.length;
        int n = A[0].length;
        int[][] C = new int[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        return C;
    }

      // return C = A^T
    public void transposeItself(int[][] A) {
        int m = A.length;
        int n = A[0].length;
        int[][] C = new int[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[j][i] = A[i][j];
            }
        }
        //copy C to A
        for (int y = 0; y < C.length; y++)
	for (int x = 0; x < C[y].length; x++)
		A[y][x] = C[y][x];

    }
    
  
    
    // return C = A + B
    public int[][] add(int[][] A, int[][] B) {
        int m = A.length;
        int n = A[0].length;
        int[][] C = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    // return C = A - B
    public int[][] subtract(int[][] A, int[][] B) {
        int m = A.length;
        int n = A[0].length;
        int[][] C = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    // return C = A * B
    static public int[][] multiply(int[][] A, int[][] B) {
        int mA = A.length;
        int nA = A[0].length;
        int mB = B.length;
        int nB = B[0].length;
        if (nA != mB) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        int[][] C = new int[mA][nB];
        for (int i = 0; i < mA; i++) {
            for (int j = 0; j < nB; j++) {
                for (int k = 0; k < nA; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    // matrix-vector multiplication (y = A * x)
    public int[] multiply(int[][] A, int[] x) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != n) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        int[] y = new int[m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                y[i] += A[i][j] * x[j];
            }
        }
        return y;
    }

    // vector-matrix multiplication (y = x^T A)
    public int[] multiply(int[] x, int[][] A) {
        int m = A.length;
        int n = A[0].length;
        if (x.length != m) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        int[] y = new int[n];
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                y[j] += A[i][j] * x[i];
            }
        }
        return y;
    }

    public void print() {
        System.out.println("\nMatrix "+name);
        for (int[] matrix1 : matrix) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(matrix1[j] + " ");
            }
            System.out.print("\n");
        }
    }
    
    public void printHex() {
        System.out.println("\nPrinting matrix in hex: "+name);
        for (int[] matrix1 : matrix) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.print(Integer.toHexString(matrix1[j]) + " ");
            }
            System.out.print("\n");
        }
    }
}
