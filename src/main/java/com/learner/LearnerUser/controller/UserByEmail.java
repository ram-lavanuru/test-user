package com.learner.LearnerUser.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserByEmail {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
}
