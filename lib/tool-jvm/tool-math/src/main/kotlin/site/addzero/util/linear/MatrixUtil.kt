//package site.addzero.util.linear
//
//import org.apache.commons.math3.util.Pair
//
//object MatrixUtil {
//    /**
//     * 计算并返回给定矩阵的转置矩阵
//     *
//     * @param matrix 原始矩阵
//     * @return double[][] 转置后的矩阵
//     */
//    fun transposeMatrix(matrix: kotlin.Array<kotlin.DoubleArray?>): kotlin.Array<kotlin.DoubleArray?> {
//        val m = matrix.size // 获取原始矩阵的行数
//        val n = matrix[0]!!.size // 获取原始矩阵的列数
//        val transposedMatrix = kotlin.Array<kotlin.DoubleArray?>(n) { kotlin.DoubleArray(m) }  // 初始化转置矩阵
//
//        // 计算转置矩阵
//        for (i in 0 until m) {
//            for (j in 0 until n) {
//                transposedMatrix[j]!![i] = matrix[i]!![j]
//            }
//        }
//
//        return transposedMatrix
//    }
//
//    // 通用方法：将任意维度的Integer数组转换为一维double数组
//    // 辅助方法：将一维Integer数组转换为double数组
//    private fun <T : kotlin.Number?> convert2double(numberArray: kotlin.Array<T?>): kotlin.DoubleArray? {
//        return java.util.Arrays.stream<T?>(numberArray)
//            .mapToDouble { obj: T? -> obj!!.toDouble() }
//            .toArray()
//    }
//
//
//    fun <T : kotlin.Number?> convert2double(numberArray2D: kotlin.Array<kotlin.Array<T?>?>): kotlin.Array<kotlin.DoubleArray?> {
//        return java.util.Arrays.stream<kotlin.Array<T?>?>(numberArray2D).map<kotlin.DoubleArray?> { ts: kotlin.Array<T?>? ->
//            java.util.Arrays.stream<T?>(ts)
//                .mapToDouble { obj: T? -> obj!!.toDouble() }
//                .toArray()
//        }
//            .toArray<kotlin.DoubleArray?> { size -> arrayOfNulls<kotlin.DoubleArray>(size) }
//    }
//
//
//    fun <T> createMatrix(m: kotlin.Int, n: kotlin.Int, initValue: T?): kotlin.Array<kotlin.Array<T?>?> {
//        val matrix = kotlin.Array<kotlin.Array<T?>?>(m) { arrayOfNulls<T>(n) }
//        for (i in 0 until m) {
//            val row = kotlin.Array<T?>(n) { initValue }
//            matrix[i] = row
//        }
//        return matrix
//    }
//
//    fun <T> createMatrixWithNthElementsOne(m: kotlin.Int, n: kotlin.Int, initValue: T?, nth: kotlin.Int): kotlin.Array<kotlin.Array<T?>?> {
//        val originalMatrix = MatrixUtil.createMatrix<T?>(m, n, initValue)
//        val newMatrix = kotlin.Array<kotlin.Array<T?>?>(m) { arrayOfNulls<T>(n) }
//        for (i in 0 until m) {
//            for (j in 0 until n) {
//                if ((i * n + j) % nth == 0) {
//                    newMatrix[i]!![j] = 1 as T
//                } else {
//                    newMatrix[i]!![j] = originalMatrix[i]!![j]
//                }
//            }
//        }
//        return newMatrix
//    }
//
//    fun <T> getMatrixElement(matrix: kotlin.Array<kotlin.Array<T?>?>, i: kotlin.Int, j: kotlin.Int): T? {
//        kotlin.require(!(matrix == null || i < 0 || j < 0 || i >= matrix.size || j >= matrix[0]!!.size)) { "Invalid matrix coordinates: (" + i + ", " + j + ")" }
//        return matrix[i]!![j]
//    }
//
//
//    fun <T> setMatrixElements(matrix: kotlin.Array<kotlin.Array<T?>?>, vararg setConfig: Pair<kotlin.Int?, kotlin.Int?, T?>?): kotlin.Array<kotlin.Array<T?>?> {
//        // 确保传入的配置不为空
//
//
//        // 创建一个新的矩阵副本，避免修改原矩阵
//
//
//        val ret = kotlin.Array<kotlin.Array<T?>?>(matrix.size) { arrayOfNulls<T>(matrix[0]!!.size) }
//        java.util.stream.IntStream.range(0, matrix.size).forEach(java.util.function.IntConsumer { i: kotlin.Int ->
//            ret[i] = kotlin.Array<T?>(matrix[i]!!.size) { matrix[i]!![it] }
//        })
//
//        // 创建一个Map来存储要修改的索引-值对，避免多次遍历
//        val modifications: kotlin.collections.MutableMap<kotlin.Int?, kotlin.collections.MutableMap<kotlin.Int?, T?>?> = java.util.Arrays.stream<Pair<kotlin.Int?, kotlin.Int?, T?>?>(setConfig)
//            .collect(java.util.stream.Collectors.groupingBy(Pair::getFirst, java.util.stream.Collectors.toMap(Pair::getSecond, Pair::getThird)))
//
//        modifications.forEach { (rowIndex: kotlin.Int?, colModifications: kotlin.collections.MutableMap<kotlin.Int?, T?>?) ->
//            colModifications!!.forEach { (colIndex: kotlin.Int?, value: T?) ->
//                if (colIndex!! >= 0 && colIndex < ret[rowIndex!!]!!.size) {
//                    ret[rowIndex]!![colIndex] = value
//                }
//            }
//        }
//
//        return ret
//    }
//
//    /**
//     * 返回每隔开k修改矩阵
//     */
//    fun <T> setIntervalElementsFromTriples(matrix: kotlin.Array<kotlin.Array<T?>?>, k: kotlin.Int): kotlin.collections.MutableList<kotlin.Array<T?>?> {
//        // 确保原矩阵不为空
//        kotlin.require(!(matrix == null || matrix.size == 0 || matrix[0]!!.size == 0)) { "Matrix cannot be null or empty." }
//        val m = matrix.size
//        val n = matrix[0]!!.size
//        val def = m * n
//        val stream = VectorGenerator.generateStream(def, k)
//
//
//        val collect = stream.map<kotlin.Array<T?>?> { e: kotlin.IntArray? ->
//            val array: kotlin.Array<Pair<kotlin.Int?, kotlin.Int?, T?>?> = java.util.Arrays.stream(e).mapToObj<kotlin.Any?>(java.util.function.IntFunction { el: kotlin.Int ->
//                val integerIntegerIntegerTriple: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, el, 1 as T)
//                integerIntegerIntegerTriple
//            }).toArray<Pair?> { size -> arrayOfNulls<Pair<*, *, *>>(size) }
//            val ts = MatrixUtil.setMatrixElements<T?>(matrix, *array)
//            val t = ts[0]
//            t
//        }.collect(java.util.stream.Collectors.toList())
//        return collect
//    }
//
//
//    @kotlin.jvm.JvmStatic
//    fun main(args: kotlin.Array<kotlin.String>) {
//        val matrix = MatrixUtil.createMatrix<kotlin.Int?>(1, 12, 0)
//
//        val integerIntegerIntegerTriple: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 1, 1)
//        val integerIntegerIntegerTriple2: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 4, 1)
//        val integerIntegerIntegerTriple23: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 8, 1)
//
//        val integerIntegerIntegerTriple2222: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 4, 1)
//        val integerIntegerIntegerTriple22: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 5, 1)
//        val integerIntegerIntegerTriple223: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 6, 1)
//        val integerIntegerIntegerTriple42: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 7, 1)
//
//        val jdoais: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 8, 1)
//        val saodijosa: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 9, 1)
//        val osajdoa: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 10, 1)
//        val asoidjoasi: Pair<kotlin.Int?, kotlin.Int?, kotlin.Int?>? = Pair(0, 11, 1)
//
//
//        //        List<Integer[]> integers2 = setIntervalElementsFromTriples(matrix, 4);
////        System.out.println(integers2);
//    }
//}
