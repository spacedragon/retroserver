package com.getfsc.retroserver.aop;

import com.getfsc.retroserver.annotation.GlobalAop;
import com.getfsc.retroserver.util.H;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午7:58
 */
@Singleton
public class AopFactoryHub {

    @Inject
    public AopFactoryHub() {
    }

    @Inject
    Set<AopFactory> factories;


    @GlobalAop
    @Inject
    Set<AopFactory> globalFactories;


    public AopFactory newFactory(String name, Object... params) {
        AopFactory aopFactory = factories.stream().filter(f -> f.name().equals(name)).findFirst()
                .orElseThrow(() -> H.rte("can't find AopFactory " + name));

        return aopFactory.setFactoryParams(params);
    }


    public List<AopFactory> globalFactories() {
        return globalFactories.stream().sorted((o1, o2) -> o1.priority().compareTo(o2.priority()))
                .collect(Collectors.toList());
    }
}
