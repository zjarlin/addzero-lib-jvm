package site.addzero.apt.dict.processor.test;


import lombok.Data;

import java.util.Date;


import java.io.Serializable;


/**
 * <p>
 *
 * </p>
 *
 * @author tll
 * @since 2022-10-25
 */
@Data

public class SysUpload implements Serializable {

    private static final long serialVersionUID = 1L;


    private String fileName;

    private String newName;

    private Long fileLength;

    private String stateid;

    private String createuser;

    private Date createDate;

    private Integer fileState;


}
