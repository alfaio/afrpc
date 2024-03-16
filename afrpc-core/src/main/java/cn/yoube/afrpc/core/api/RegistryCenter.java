package cn.yoube.afrpc.core.api;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
public interface RegistryCenter {

    void start();

    void stop();

    //provider侧
    void register(String service, String instance);

    void unRegister(String service, String instance);

    //consumer侧
    List<String> fetchAll(String service);

    List<String> subscribe();

    class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(String service, String instance) {

        }

        @Override
        public void unRegister(String service, String instance) {

        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }

        @Override
        public List<String> subscribe() {
            return null;
        }
    }

}