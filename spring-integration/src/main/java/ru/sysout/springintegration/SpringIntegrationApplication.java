package ru.sysout.springintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;

import ru.sysout.springintegration.model.Animal;

@SpringBootApplication
public class SpringIntegrationApplication {
    @Bean
    DirectChannel outputChannel() {
        return new DirectChannel();
    }

    @MessagingGateway
    public interface I {

        @Gateway(requestChannel = "personFlow.input")
        void process(Animal animal);

    }

    // канал DirectChannel с именем personFlow.input создается автоматически
    @Bean
    public IntegrationFlow personFlow() {
        return flow -> flow.handle("aService", "process")
            .handle("bService", "process")
            .handle("cService", "process")
            .channel("outputChannel");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringIntegrationApplication.class, args);
        ConfigurableApplicationContext ctx = SpringApplication.run(SpringIntegrationApplication.class, args);

        DirectChannel outputChannel = ctx.getBean("outputChannel", DirectChannel.class);
        // обработчик внутри subscribe выполнится как только занокнчится выполнение flow
        outputChannel.subscribe(x -> System.out.println(x));
        // запускаем выполнение flow
        ctx.getBean(I.class).process(new Animal("cat"));

        // можно было запустить flow отправкой сообщения во входной канал input:
        // MessageChannel inputChannel = ctx.getBean("personFlow.input", MessageChannel.class);
        // inputChannel.send(MessageBuilder.withPayload(new Animal("cat")).build());
        ctx.close();
    }
}
