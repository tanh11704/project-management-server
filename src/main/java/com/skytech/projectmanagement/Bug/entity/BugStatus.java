package com.skytech.projectmanagement.Bug.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum BugStatus {

    OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED;


}
