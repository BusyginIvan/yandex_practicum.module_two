package ru.yandex.practicum.market.configuration;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration(proxyBeanMethods = false)
public class TransactionManagersConfiguration implements TransactionManagementConfigurer, BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override @NonNull
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return beanFactory.getBean("transactionManager", PlatformTransactionManager.class);
    }

    @Bean
    public TransactionalOperator r2dbcTransactionalOperator(
        @Qualifier("connectionFactoryTransactionManager") ReactiveTransactionManager r2dbcTransactionManager
    ) {
        return TransactionalOperator.create(r2dbcTransactionManager);
    }
}
