package com.gisroad.business.util.easyexcel.strategy

//package org.jeecg.common.util.easyexcel.strategy;
//
//import com.alibaba.excel.metadata.Head;
//import com.alibaba.excel.write.metadata.style.WriteCellStyle;
//import com.alibaba.excel.write.metadata.style.WriteFont;
//import com.alibaba.excel.write.style.AbstractVerticalCellStyleStrategy;
//import org.apache.poi.ss.usermodel.FillPatternType;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.ss.usermodel.IndexedColors;
//
//public class CustomVerticalCellStyleStrategy extends AbstractVerticalCellStyleStrategy {
//
//    // 重写定义表头样式的方法
//    @Override
//    protected WriteCellStyle headCellStyle(Head head) {
//        WriteCellStyle writeCellStyle = new WriteCellStyle();
//        writeCellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
//        WriteFont writeFont = new WriteFont();
//        writeFont.setColor(IndexedColors.RED.getIndex());
//        writeFont.setBold(false);
//        writeFont.setFontHeightInPoints(Short.valueOf((short)15));
//        writeCellStyle.setWriteFont(writeFont);
//        return writeCellStyle;
//    }
//
//    // 重写定义内容部分样式的方法
//    @Override
//    protected WriteCellStyle contentCellStyle(Head head) {
//        WriteCellStyle writeCellStyle = new WriteCellStyle();
//        writeCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
//        writeCellStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
//        writeCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
//        return writeCellStyle;
//    }
//}
//
