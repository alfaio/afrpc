package io.github.alfaio.afrpc.core.transport;

import io.github.alfaio.afrpc.core.api.RpcRequest;
import io.github.alfaio.afrpc.core.api.RpcResponse;
import io.github.alfaio.afrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * transport for spring boot
 *
 * @author LimMF
 * @since 2024/4/16
 **/
@RestController
public class SpringBootTransport {

    @Autowired
    ProviderInvoker providerInvoker;

    @PostMapping(value = "/afrpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
