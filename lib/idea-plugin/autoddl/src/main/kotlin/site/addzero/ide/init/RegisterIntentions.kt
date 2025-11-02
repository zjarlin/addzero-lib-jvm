package site.addzero.ide.init

import com.intellij.codeInsight.intention.IntentionManager

 fun registerIntentions() {
    // 在插件启动时注册所有意图动作
    val intentionManager = IntentionManager.getInstance()


    //注册java意图
//    intentionManager.addAction(AddSwaggerAnnotationJavaAction())
//    intentionManager.addAction(AddExcelPropertyAnnotationJavaAction())
//    intentionManager.addAction(AddCusTomAnnotationJavaAction())
//    intentionManager.addAction(GenEnumByFieldCommentIntention())


    //注册kt意图
//    intentionManager.addAction(AddSwaggerAnnotationAction())
//    intentionManager.addAction(AddExcelPropertyAnnotationAction())
//    intentionManager.addAction(AddCusTomAnnotationAction())
//    intentionManager.addAction(ConvertToVersionCatalogIntention())

//    intentionManager.addAction(RemoveShitCodeIntention())
//    intentionManager.addAction(KotlinLambdaIntentionAction())


//    intentionManager.addAction(GenEnumByFieldCommentIntention())
}
