package com.gdtopway.operatelog.entity;


import com.gdtopway.operatelog.DataName;
import com.gdtopway.operatelog.util.ModifyName;
import lombok.Data;
import lombok.ToString;

import java.util.Date;


/**
 * @author WWMXD
 */
@Data
@ToString
public class OperateLog {

    @DataName(name = "员工编码")
    private Long ygbm;
    @DataName(name = "业务ID")
    private Long ywid;
    @DataName(name = "模块类型")
    private Long modifyType;
    @DataName(name = "业务功能")
    private String businessFunction;
    @DataName(name = "操作人")
    private String username;
    @DataName(name = "操作日期")
    private Date modifyDate;

    //操作名词
    @DataName(name = "操作名词")
    private ModifyName modifyName;

    //操作对象
    @DataName(name = "操作对象")
    private String modifyObject;

    //操作内容
    @DataName(name = "操作内容")
    private String modifyContent;

    //ip
    @DataName(name = "IP")
    private String modifyIp;

    private Object oldObject;

    private Object newObject;



}