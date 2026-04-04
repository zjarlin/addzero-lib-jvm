package site.addzero.example

import kotlin.test.Test
import kotlin.test.assertEquals

class SpreadPackExampleTest {

    @Test
    fun generated_overloads_compile_and_run() {
        assertEquals(
            "TextProps[text,color,maxLines,softWrap,onTextLayout]=(hello,blue,2,false,callback-fixed)|" +
                "Text(text=[MyText] world,color=red,maxLines=3,softWrap=true,layout=wrapped-layout)",
            invokeSpreadPackExample(),
        )
    }
}
