package cn.yoube.afrpc.core.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/19
 **/
@Data
@AllArgsConstructor
public class Event {

    List<String> data;
}
