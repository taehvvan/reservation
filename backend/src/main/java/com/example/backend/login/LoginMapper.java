package com.example.backend.login;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.example.backend.register.UserEntity;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;

@Mapper
public interface LoginMapper {

    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "u_id", id = true), // u_id 컬럼을 id 필드에 매핑
        @Result(property = "name", column = "name"),
        @Result(property = "email", column = "email"),
        @Result(property = "password", column = "password"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "birth", column = "birth"),
        @Result(property = "social", column = "social"),
        @Result(property = "role", column = "role"),
        @Result(property = "refresh_token", column = "refreshToken"),
    })
    @Select("SELECT * FROM users WHERE email = #{email}")
    UserEntity findByEmail(@Param("email") String email);

}
