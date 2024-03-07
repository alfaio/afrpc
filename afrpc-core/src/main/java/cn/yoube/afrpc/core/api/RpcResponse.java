package cn.yoube.afrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {

    Integer code;
    Object data;
}
