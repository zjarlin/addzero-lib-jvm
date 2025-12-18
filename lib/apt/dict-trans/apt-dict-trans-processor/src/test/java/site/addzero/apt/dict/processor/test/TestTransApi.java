package site.addzero.apt.dict.processor.test;

import org.jetbrains.annotations.NotNull;
import site.addzero.apt.dict.trans.inter.TransApi;
import site.addzero.apt.dict.trans.model.out.SystemDictModelResult;
import site.addzero.apt.dict.trans.model.out.TableDictModelResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 测试用的TransApi实现
 */
public class TestTransApi implements TransApi {
    
    @Override
    public List<SystemDictModelResult> translateDictBatchCode2name(String dictCodes, String keys) {
        List<SystemDictModelResult> results = new ArrayList<>();
        
        // 模拟字典翻译
        if ("USER_STATUS".equals(dictCodes)) {
            if (keys != null) {
                String[] keyArray = keys.split(",");
                for (String key : keyArray) {
                    switch (key.trim()) {
                        case "1":
                            results.add(new SystemDictModelResult("USER_STATUS", "1", "启用"));
                            break;
                        case "0":
                            results.add(new SystemDictModelResult("USER_STATUS", "0", "禁用"));
                            break;
                    }
                }
            }
        } else if ("ROLE_TYPE".equals(dictCodes)) {
            if (keys != null) {
                String[] keyArray = keys.split(",");
                for (String key : keyArray) {
                    switch (key.trim()) {
                        case "ADMIN":
                            results.add(new SystemDictModelResult("ROLE_TYPE", "ADMIN", "管理员"));
                            break;
                        case "USER":
                            results.add(new SystemDictModelResult("ROLE_TYPE", "USER", "普通用户"));
                            break;
                    }
                }
            }
        } else if ("DEPT_STATUS".equals(dictCodes)) {
            if (keys != null) {
                String[] keyArray = keys.split(",");
                for (String key : keyArray) {
                    switch (key.trim()) {
                        case "1":
                            results.add(new SystemDictModelResult("DEPT_STATUS", "1", "正常"));
                            break;
                        case "0":
                            results.add(new SystemDictModelResult("DEPT_STATUS", "0", "停用"));
                            break;
                    }
                }
            }
        }
        
        return results;
    }
    
    @Override
    public List<TableDictModelResult> translateTableBatchCode2name(String table, String text, String code, String keys) {
        List<TableDictModelResult> results = new ArrayList<>();
        
        // 模拟表字典翻译
        if ("t_other".equals(table) && keys != null) {
            String[] keyArray = keys.split(",");
            for (String key : keyArray) {
                switch (key.trim()) {
                    case "CODE1":
                        if ("xx_name".equals(text)) {
                            results.add(new TableDictModelResult("t_other", "CODE1", "名称1"));
                        } else if ("xx_other_name".equals(text)) {
                            results.add(new TableDictModelResult("t_other", "CODE1", "其他名称1"));
                        }
                        break;
                    case "CODE2":
                        if ("xx_name".equals(text)) {
                            results.add(new TableDictModelResult("t_other", "CODE2", "名称2"));
                        } else if ("xx_other_name".equals(text)) {
                            results.add(new TableDictModelResult("t_other", "CODE2", "其他名称2"));
                        }
                        break;
                }
            }
        }
        
        return results;
    }

    @Override
    public List<SystemDictModelResult> translateDictBatchName2code(String dictCodes, String names) {
        List<SystemDictModelResult> results = new ArrayList<>();

        // 模拟反向字典翻译
        if ("USER_STATUS".equals(dictCodes)) {
            if (names != null) {
                String[] nameArray = names.split(",");
                for (String name : nameArray) {
                    switch (name.trim()) {
                        case "启用":
                            results.add(new SystemDictModelResult("USER_STATUS", "1", "启用"));
                            break;
                        case "禁用":
                            results.add(new SystemDictModelResult("USER_STATUS", "0", "禁用"));
                            break;
                    }
                }
            }
        } else if ("ROLE_TYPE".equals(dictCodes)) {
            if (names != null) {
                String[] nameArray = names.split(",");
                for (String name : nameArray) {
                    switch (name.trim()) {
                        case "管理员":
                            results.add(new SystemDictModelResult("ROLE_TYPE", "ADMIN", "管理员"));
                            break;
                        case "普通用户":
                            results.add(new SystemDictModelResult("ROLE_TYPE", "USER", "普通用户"));
                            break;
                    }
                }
            }
        } else if ("DEPT_STATUS".equals(dictCodes)) {
            if (names != null) {
                String[] nameArray = names.split(",");
                for (String name : nameArray) {
                    switch (name.trim()) {
                        case "正常":
                            results.add(new SystemDictModelResult("DEPT_STATUS", "1", "正常"));
                            break;
                        case "停用":
                            results.add(new SystemDictModelResult("DEPT_STATUS", "0", "停用"));
                            break;
                    }
                }
            }
        }

        return results;
    }

    @Override
    public List<TableDictModelResult> translateTableBatchName2code(String table, String text, String code, String names) {
        List<TableDictModelResult> results = new ArrayList<>();

        // 模拟表反向字典翻译
        if ("t_other".equals(table) && names != null) {
            String[] nameArray = names.split(",");
            for (String name : nameArray) {
                switch (name.trim()) {
                    case "名称1":
                        results.add(new TableDictModelResult("t_other", "CODE1", "名称1"));
                        break;
                    case "其他名称1":
                        results.add(new TableDictModelResult("t_other", "CODE1", "其他名称1"));
                        break;
                    case "名称2":
                        results.add(new TableDictModelResult("t_other", "CODE2", "名称2"));
                        break;
                    case "其他名称2":
                        results.add(new TableDictModelResult("t_other", "CODE2", "其他名称2"));
                        break;
                }
            }
        }

        return results;
    }
}