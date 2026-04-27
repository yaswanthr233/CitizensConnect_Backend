package com.citizensconnect.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "politicians")
public class Politician extends User {
    
    private String partyName;
    private String constituency;
    private String govtIdProof;

}
