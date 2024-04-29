package io.github.alfaio.afrpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    Long id;
    Float amount;
}
