//package site.addzero.util.linear
//
//import org.apache.commons.math3.linear.Array2DRowRealMatrix
//import org.apache.commons.math3.linear.RealMatrix
//import org.apache.commons.math3.optim.PointValuePair
//import org.apache.commons.math3.optim.linear.*
//import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
//
//object LinearProgrammingSolver {
//    @kotlin.jvm.JvmStatic
//    fun main(args: kotlin.Array<kotlin.String>) {
//        val zbs = kotlin.doubleArrayOf(180.0, 20.0, 500.0, 200.0, 600.0) // 库存需求
//        val capacities = kotlin.arrayOf<kotlin.DoubleArray?>(
//            kotlin.doubleArrayOf(3.0, 2.0, 0.0, 0.0, 50.0),
//            kotlin.doubleArrayOf(6.0, 0.0, 5.0, 0.0, 10.0),
//            kotlin.doubleArrayOf(0.0, 0.0, 0.0, 6.0, 20.0),
//            kotlin.doubleArrayOf(0.0, 0.0, 0.0, 7.0, 30.0)
//        )
//
//
//        //四种类型的船再三条作业线各自的价格(广义理解为权重即可)
//        val 定义价格矩阵 = kotlin.arrayOf<kotlin.DoubleArray?>(
//            kotlin.doubleArrayOf(5000.0, 7000.0, 6500.0, 8000.0),
//            kotlin.doubleArrayOf(5000.0, 7000.0, 6500.0, 8000.0),
//            kotlin.doubleArrayOf(5000.0, 7000.0, 6500.0, 8000.0),
//        )
//
//
//        val lineConfigurations = kotlin.doubleArrayOf(60.0, 70.0, 40.0) // 载具数量约束
//
//        //        double[] doubles = solveLinearProgramming(zbs, capacities, lineConfigurations,定义价格矩阵);
//        val doubles = LinearProgrammingSolver.solveLinearProgramming(zbs, capacities, lineConfigurations)
//    }
//
//    fun solveLinearProgramming(zbs: kotlin.DoubleArray, capacities: kotlin.Array<kotlin.DoubleArray?>, lineConfigurations: kotlin.DoubleArray): kotlin.DoubleArray? {
//        val n = capacities.size
//        val m = lineConfigurations.size
//        val dimension = n * m // 总变量数
//        val zbTypeSize = zbs.size
//        val numConstraints = zbTypeSize + m // 总约束数
//
//        // 默认价格矩阵为全0，表示最小化运输成本
//        val defaultPriceMatrix = kotlin.Array<kotlin.DoubleArray?>(m) { kotlin.DoubleArray(n) }
//        for (i in 0 until m) {
//            for (j in 0 until n) {
//                defaultPriceMatrix[i]!![j] = 0.0
//            }
//        }
//        return LinearProgrammingSolver.solveLinearProgramming(zbs, capacities, lineConfigurations, defaultPriceMatrix)
//    }
//
//    fun solveLinearProgramming(zbs: kotlin.DoubleArray, capacities: kotlin.Array<kotlin.DoubleArray?>, lineConfigurations: kotlin.DoubleArray, priceMatrix: kotlin.Array<kotlin.DoubleArray?>): kotlin.DoubleArray? {
//        //行
////        int m = lineConfigurations.length;
////
////        //列
////        int n = capacities.length;
////
////        int zbTypeSize = zbs.length;
////
////        //纬度
////        int dimension = m * n;
//
//
//        val n = capacities.size
//        val m = lineConfigurations.size
//        val dimension = n * m // 总变量数
//        val zbTypeSize = zbs.size
//        val numConstraints = zbTypeSize + m // 总约束数
//
//
//        // 构造约束条件
//
////       初始化矩阵
////        Integer[][] matrix = MatrixUtil.createMatrix(m, n, 1);
////        double[][] doubles1 = MatrixUtil.convert2double(matrix);
//        // 构建约束条件
////        List<LinearConstraint> constraints = new ArrayList<>();
//        //初始解空间基底
//
//
//        //       初始化矩阵
//        val matrix: kotlin.Array<kotlin.Array<kotlin.Int?>?>? = MatrixUtil.createMatrix<kotlin.Int?>(1, m * n, 0)
//        // 构建约束条件
//        val constraints: kotlin.collections.MutableList<LinearConstraint?> = java.util.ArrayList<LinearConstraint?>()
//        //        x1,x2,x3,x4
//        val 特征行变量: kotlin.collections.MutableList<kotlin.Array<kotlin.Int?>?> = MatrixUtil.setIntervalElementsFromTriples<kotlin.Int?>(matrix, n)
//
//
//        val doubleList = 特征行变量.stream()
//            .map<kotlin.DoubleArray?> { arr: kotlin.Array<kotlin.Int?>? ->
//                java.util.Arrays.stream<kotlin.Int?>(arr)
//                    .mapToDouble { obj: kotlin.Int? -> obj!!.toDouble() }
//                    .toArray()
//            }
//            .collect(java.util.stream.Collectors.toList())
//
//
//        //        作业线船数量约束
//        val collect: kotlin.collections.MutableList<LinearConstraint?> = java.util.stream.Stream.iterate<kotlin.Int?>(0) { i: kotlin.Int? ->
//            var i = i
//            i = i!! + 1
//        }.limit(m.toLong()).map<kotlin.Any?> { i: kotlin.Int? ->
//            val doubles = doubleList.get(i!!)
//            val linearConstraint: LinearConstraint = LinearConstraint(doubles, Relationship.EQ, lineConfigurations[i])
//            linearConstraint
//        }.collect(java.util.stream.Collectors.toList())
//        constraints.addAll(collect)
//
//
//        //运力约束
//        for (k in 0 until zbTypeSize) {
//            val capacityConstraintCoefficients = kotlin.DoubleArray(m * n)
//            for (i in 0 until m) {
//                for (j in 0 until n) {
//                    val index = i * n + j // 计算索引
//                    capacityConstraintCoefficients[index] = capacities[j]!![k] // 赋值系数
//                }
//            }
//            val capacityConstraint: LinearConstraint = LinearConstraint(capacityConstraintCoefficients, Relationship.GEQ, zbs[k])
//            constraints.add(capacityConstraint)
//        }
//
//
//        // 将非负约束添加到约束集合中
//        val nonNegConstraints: kotlin.collections.MutableList<LinearConstraint?> = LinearProgrammingSolver.getLinearConstraints(dimension)
//        constraints.addAll(nonNegConstraints)
//
//
//        // 定义目标函数（这里可以是任意合法目标，但根据问题描述，我们更关心约束满足）
//        // 初始化决策变量系数（目标函数系数，这里假设目标是无特定优化，全为0）
////        double[] objectiveCoefficients = new double[dimension];
////        Arrays.fill(objectiveCoefficients, 0);
////        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(objectiveCoefficients, 0);
//
//
//        // 这里是原有的求解逻辑，但是目标函数的系数现在应该包含价格矩阵
//        val objectiveCoefficients = kotlin.DoubleArray(dimension)
//        // 将价格矩阵的元素添加到目标函数的系数中
//        for (i in 0 until m) {
//            for (j in 0 until n) {
//                val index = i * n + j // 计算索引
//                objectiveCoefficients[index] = priceMatrix[i]!![j] // 负号表示我们想最小化成本
//            }
//        }
//
//        // 重新定义目标函数
//        val objectiveFunction: LinearObjectiveFunction = LinearObjectiveFunction(objectiveCoefficients, 0)
//
//
//        // 定义优化问题
//        // 创建约束条件集
//        val constraintSet: LinearConstraintSet = LinearConstraintSet(constraints)
//        //        // 使用SimplexSolver求解
//        val solver: SimplexSolver = SimplexSolver()
//        val solution: PointValuePair = solver.optimize(
//            objectiveFunction,
//            constraintSet,
//            GoalType.MINIMIZE // 我们的目标是最小化
//        )
//
//        //打印结果
//        kotlin.io.println("决策变量为：" + java.util.Arrays.toString(solution.point))
//        kotlin.io.println("最优值为：" + solution.value)
//        val point: kotlin.DoubleArray? = solution.point
//
//        return point
//    }
//
//
//    private fun createDecisionVariableMatrix(m: kotlin.Int, n: kotlin.Int): RealMatrix {
//        val coefficients = kotlin.Array<kotlin.DoubleArray?>(m) { kotlin.DoubleArray(n) }
//        for (i in 0 until m) {
//            for (j in 0 until n) {
//                coefficients[i]!![j] = 1.0
//            }
//        }
//        return Array2DRowRealMatrix(coefficients)
//    }
//
//    /**
//     * 非负约束列表
//     *
//     * @param dimension
//     * @return [List]<[LinearConstraint]>
//     */
//    private fun getLinearConstraints(dimension: kotlin.Int): kotlin.collections.MutableList<LinearConstraint?> {
//        // 非负约束列表
//        val nonNegConstraints: kotlin.collections.MutableList<LinearConstraint?> = java.util.ArrayList<LinearConstraint?>()
//
//        // 为每个变量 xi 创建非负约束 xi >= 0
//        for (i in 0 until dimension) {
//            // 创建一个新数组，其中第 i 个元素是 1（表示变量 xi），其余元素是 0
//            val coeffs = kotlin.DoubleArray(dimension)
//            coeffs[i] = 1.0
//
//            // 添加约束 xi >= 0
//            nonNegConstraints.add(LinearConstraint(coeffs, Relationship.GEQ, 0.0))
//        }
//        return nonNegConstraints
//    }
//}
