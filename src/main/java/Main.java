public class Main {


    public static void main(String[] args) {
        int length = 100_000_000;
        System.out.println("Testing Non Valhalla - Creating array of Point3D, length="+length);
        long start = System.currentTimeMillis();
        Point3D[] myArray = new Point3D[length];
        for (int i = 0; i < myArray.length; i++) {
            myArray[i] = new Point3D(0f, 0f, 0f);
        }
        System.out.println("Print last value of array : " + myArray[length - 1]);
        Runtime.getRuntime().gc();
        printMemoryUsage();
        System.out.println("Array length : " + myArray.length);
        System.out.println("Total time " + (System.currentTimeMillis() - start) + " ms");
    }

    private static void printMemoryUsage() {
        System.out.println("MB: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
    }

    static class Point3D {
        private float x;
        private float y;
        private float z;

        public Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Point3D{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }
}
