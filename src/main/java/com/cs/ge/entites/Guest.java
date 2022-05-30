package com.cs.ge.entites;

import com.cs.ge.enums.Civility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Guest {
    private String id;
    private Civility civility;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String classe;
    private String place;
    private String typeBillet;

}
