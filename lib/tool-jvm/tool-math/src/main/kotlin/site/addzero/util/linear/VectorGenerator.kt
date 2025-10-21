//package site.addzero.util.linear
//
//import java.util.function.IntFunction
//import java.util.stream.IntStream
//import java.util.stream.Stream
//
//object VectorGenerator {
//    /**
//     * //    int m = 3; // 行数
//     * //        int n = 4; // 列数
//     * //        int[] indices = {1, 5, 9}; /
//     * [1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0] / 给定的索引
//     *
//     * @param m
//     * @param n
//     * @param indices
//     * @return [double[]]
//     */
//    fun generateVector(m: Int, n: Int, indices: IntArray): DoubleArray {
//        val vector = DoubleArray(m * n)
//
//        for (index in indices) {
//            if (index >= 0 && index < m * n) {
//                vector[index - 1] = 1.0
//            } else {
//                throw IllegalArgumentException("Index out of bounds: " + index)
//            }
//        }
//
//        return vector
//    }
//
//    @JvmStatic
//    fun main(args: Array<String>) {
//        val m = 3 // 行数
//        val n = 4 // 列数
//        val indices = intArrayOf(1, 5, 9) // 给定的索引
//
//
//        val resultVector = generateVector(m, n, indices)
//    }
//
//    /**
//     * 每隔k set为1
//     * @param def
//     * @param k
//     * @return [Stream]<[int[]]>
//     */
//    fun generateStream(def: Int, k: Int): Stream<IntArray?> {
//        val times = def / k
//        return IntStream.range(0, times)
//            .mapToObj<IntArray?>(IntFunction { i: Int -> IntStream.range(i * k, (i + 1) * k).toArray() })
//    }
//}
