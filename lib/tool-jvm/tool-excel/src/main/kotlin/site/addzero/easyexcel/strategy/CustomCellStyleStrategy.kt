package site.addzero.easyexcel.strategy

import cn.idev.excel.metadata.Head
import cn.idev.excel.write.metadata.style.WriteCellStyle
import cn.idev.excel.write.style.AbstractVerticalCellStyleStrategy

class CustomCellStyleStrategy : AbstractVerticalCellStyleStrategy() {
    // 重写定义表头样式的方法
    //    @Override
    //    protected WriteCellStyle headCellStyle(Head head) {
    //        WriteCellStyle writeCellStyle = new WriteCellStyle();
    //        writeCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
    //        writeCellStyle.setFillForegroundColor(closestColor.getIndex());
    //        WriteFont writeFont = new WriteFont();
    //        writeFont.setColor(IndexedColors.BLACK.getIndex());
    //        writeFont.setBold(true);
    //        writeCellStyle.setWriteFont(writeFont);
    //        return writeCellStyle;
    //    }
    // 重写定义内容部分样式的方法
    protected override fun contentCellStyle(head: Head?): WriteCellStyle {
        val writeCellStyle: WriteCellStyle = WriteCellStyle()
        writeCellStyle.setHorizontalAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER)
        writeCellStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER)
        return writeCellStyle
    }
}
