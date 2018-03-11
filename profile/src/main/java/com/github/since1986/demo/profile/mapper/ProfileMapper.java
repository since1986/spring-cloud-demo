package com.github.since1986.demo.profile.mapper;

import com.github.since1986.demo.profile.model.Profile;

public interface ProfileMapper {

    Profile get(long id);

    void save(Profile profile);
}
