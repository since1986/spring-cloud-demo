package com.github.since1986.demo.profile.mapper;

import com.github.since1986.demo.profile.model.Profile;
import org.apache.ibatis.annotations.Param;

public interface ProfileMapper {

    Profile get(long id);

    void save(@Param("profile") Profile profile);
}
