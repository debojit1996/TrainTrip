package com.debo.traintrip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Passenger {
    private String email; // can be used as an identifier
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
}
