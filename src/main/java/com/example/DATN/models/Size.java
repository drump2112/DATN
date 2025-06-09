package com.example.DATN.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Sizes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Size {

}
