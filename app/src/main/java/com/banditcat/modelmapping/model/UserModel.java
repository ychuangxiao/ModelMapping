package com.banditcat.modelmapping.model;

import com.banditcat.annotations.Filed;
import com.banditcat.annotations.MappedClass;
import com.banditcat.annotations.Parse;
import com.banditcat.convert.StringToStringConvert;
import com.banditcat.modelmapping.entity.UserEntity;

/**
 * 文件名称：{@link UserModel}
 * <br/>
 * 功能描述：
 * <br/>
 * 创建作者：banditcat
 * <br/>
 * 创建时间：2018/1/19 11:42
 * <br/>
 * 修改作者：banditcat
 * <br/>
 * 修改时间：2018/1/19 11:42
 * <br/>
 * 修改备注：
 */
@MappedClass(with = UserEntity.class)
public class UserModel {

    @Filed(toField = "userName2")
    @Parse(toModelWith = StringToStringConvert.class, toEntityWith = StringToStringConvert.class)
    private String userName;

    @Filed(toField = "address2")
    @Parse(toModelWith = StringToStringConvert.class, toEntityWith = StringToStringConvert.class)
    private String address;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
