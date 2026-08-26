import jdk.internal.value.ValueClass;

public class Main2 {


    public static void main(String[] args) {
        System.out.println("Testing With Valhalla Features");
        long start = System.currentTimeMillis();
        int length = 100_000_000;
        Point3D[] myArray = (Point3D[]) ValueClass.newNullRestrictedNonAtomicArray(Point3D.class, length, new Point3D(0f, 0f, 0f));
        System.out.println("Print last value of array : "+myArray[length-1]);
        Runtime.getRuntime().gc();
        printMemoryUsage();
        System.out.println("Array length : "+myArray.length);
        System.out.println("Total time "+(System.currentTimeMillis() - start)+ " ms");

    }

    private static void printMemoryUsage() {
        System.out.println("MB: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024*1024));
    }

    static value class Point3D {
        private float x ;
        private  float y ;
        private  float z ;
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
