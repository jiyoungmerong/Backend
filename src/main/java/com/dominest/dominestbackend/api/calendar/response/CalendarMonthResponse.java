package com.dominest.dominestbackend.api.calendar.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CalendarMonthResponse {
    private int day;
    private boolean content; // 내용 유무
}
